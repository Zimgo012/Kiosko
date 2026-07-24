package com.example.myapplication.engine.printer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.Log
import com.epson.epos2.Epos2Exception
import com.epson.epos2.printer.Printer
import com.epson.epos2.printer.PrinterStatusInfo
import com.epson.epos2.printer.ReceiveListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Adjustable Printing Settings
 */
data class PrintSettings(
    val paperWidthDots: Int = 512, // Standard for 80mm paper (can be 512 or 576)
    val borderSizeDots: Int = 24,  // Size of the white border around the photo
    val halftoneMode: Int = Printer.HALFTONE_THRESHOLD, // Manual Floyd-Steinberg dithering used
    val brightness: Double = 1.0, // Keeping at 1.0 as ImageProcessor handles the sync
    val templateSettings: PrintTemplateSettings = PrintTemplateSettings()
)

class PrinterManager(private val context: Context) : ReceiveListener {

    private var printer: Printer? = null
    
    // Easy to modify settings
    var settings = PrintSettings()

    suspend fun printBitmap(bitmap: Bitmap, quantity: Int = 1): Boolean = withContext(Dispatchers.IO) {
        try {
            initializePrinter()
            
            printer?.let { p ->
                // 1. Process image: Add border and resize to fit paper
                val processedBitmap = prepareBitmapForPrint(bitmap)
                
                repeat(quantity) {
                    // 2. Add image to buffer
                    p.addTextAlign(Printer.ALIGN_CENTER)
                    p.addImage(
                        processedBitmap, 
                        0, 0,
                        processedBitmap.width,
                        processedBitmap.height,
                        Printer.COLOR_1,
                        Printer.MODE_MONO,
                        settings.halftoneMode,
                        settings.brightness,
                        Printer.COMPRESS_AUTO
                    )
                    
                    // 3. Feed and Cut
                    p.addFeedLine(2)
                    p.addCut(Printer.CUT_FEED)
                }
                
                // 4. Connect and Send
                p.connect("USB:", Printer.PARAM_DEFAULT)
                p.sendData(Printer.PARAM_DEFAULT)
                
                return@withContext true
            }
        } catch (e: Epos2Exception) {
            Log.e("PrinterManager", "Epson Error: ${e.errorStatus}", e)
        } catch (e: Exception) {
            Log.e("PrinterManager", "General Error", e)
        }
        return@withContext false
    }

    /**
     * Resizes the bitmap to fit the paper width if necessary.
     * Assumes the bitmap is already prepared with template and dithering.
     */
    private fun prepareBitmapForPrint(source: Bitmap): Bitmap {
        if (source.width == settings.paperWidthDots) return source
        
        val scale = settings.paperWidthDots.toFloat() / source.width
        val targetHeight = (source.height * scale).toInt()
        return Bitmap.createScaledBitmap(source, settings.paperWidthDots, targetHeight, true)
    }

    private fun initializePrinter() {
        if (printer != null) {
            try {
                printer?.clearCommandBuffer()
                printer?.disconnect()
            } catch (e: Exception) {
                Log.e("PrinterManager", "Cleanup error", e)
            }
        }
        
        try {
            printer = Printer(Printer.TM_T88, Printer.MODEL_ANK, context)
            printer?.setReceiveEventListener(this)
        } catch (e: Epos2Exception) {
            Log.e("PrinterManager", "Init Error: ${e.errorStatus}")
        }
    }

    override fun onPtrReceive(printerObj: Printer?, code: Int, status: PrinterStatusInfo?, printJobId: String?) {
        try {
            printer?.disconnect()
            printer?.clearCommandBuffer()
        } catch (e: Exception) {
            Log.e("PrinterManager", "Receive error", e)
        }
    }
}
