package com.example.myapplication.engine.printer

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.epson.epos2.Epos2Exception
import com.epson.epos2.printer.Printer
import com.epson.epos2.printer.PrinterStatusInfo
import com.epson.epos2.printer.ReceiveListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PrinterManager(private val context: Context) : ReceiveListener {

    private var printer: Printer? = null

    suspend fun printBitmap(bitmap: Bitmap): Boolean = withContext(Dispatchers.IO) {
        try {
            initializePrinter()
            
            printer?.let { p ->
                // Add image data
                p.addTextAlign(Printer.ALIGN_CENTER)
                p.addImage(
                    bitmap, 0, 0,
                    bitmap.width,
                    bitmap.height,
                    Printer.COLOR_1,
                    Printer.MODE_MONO,
                    Printer.HALFTONE_DITHER,
                    Printer.PARAM_DEFAULT.toDouble(),
                    Printer.COMPRESS_AUTO,
                )
                p.addFeedLine(1)
                p.addCut(Printer.CUT_FEED)
                
                // Connect and send
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
            // TM-T88V is usually TM_T88
            printer = Printer(Printer.TM_T88, Printer.MODEL_ANK, context)
            printer?.setReceiveEventListener(this)
        } catch (e: Epos2Exception) {
            Log.e("PrinterManager", "Init Error: ${e.errorStatus}")
        }
    }

    override fun onPtrReceive(printerObj: Printer?, code: Int, status: PrinterStatusInfo?, printJobId: String?) {
        // Handle result of sendData
        try {
            printer?.disconnect()
            printer?.clearCommandBuffer()
        } catch (e: Exception) {
            Log.e("PrinterManager", "Receive error", e)
        }
    }
}
