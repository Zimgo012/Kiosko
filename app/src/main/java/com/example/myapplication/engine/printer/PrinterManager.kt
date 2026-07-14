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
    val halftoneMode: Int = Printer.HALFTONE_DITHER, // DITHER, THRESHOLD, or ERROR_DIFFUSION
    val brightness: Double = 1.0, // 0.1 to 10.0
)

class PrinterManager(private val context: Context) : ReceiveListener {

    private var printer: Printer? = null
    
    // Easy to modify settings
    var settings = PrintSettings()

    suspend fun printBitmap(bitmap: Bitmap): Boolean = withContext(Dispatchers.IO) {
        try {
            initializePrinter()
            
            printer?.let { p ->
                // 1. Process image: Add border and resize to fit paper
                val processedBitmap = prepareBitmapForPrint(bitmap)
                
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
     * Resizes the bitmap to fit the paper width and adds a white border.
     */
    private fun prepareBitmapForPrint(source: Bitmap): Bitmap {
        // Calculate dimensions
        val contentWidth = settings.paperWidthDots - (settings.borderSizeDots * 2)
        val scale = contentWidth.toFloat() / source.width
        val contentHeight = (source.height * scale).toInt()
        
        // Create the final canvas with white background
        val finalWidth = settings.paperWidthDots
        val finalHeight = contentHeight + (settings.borderSizeDots * 2)
        
        val output = Bitmap.createBitmap(finalWidth, finalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawColor(Color.WHITE)
        
        // Draw the resized original photo in the center
        val scaledBitmap = Bitmap.createScaledBitmap(source, contentWidth, contentHeight, true)
        canvas.drawBitmap(
            scaledBitmap, 
            settings.borderSizeDots.toFloat(), 
            settings.borderSizeDots.toFloat(), 
            null
        )
        
        return output
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
