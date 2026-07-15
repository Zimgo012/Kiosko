package com.example.myapplication.controller.booth

import android.graphics.Bitmap
import com.example.myapplication.engine.image.ImageProcessor
import com.example.myapplication.engine.printer.PrinterManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PrinterController(
    private val scope: CoroutineScope
) {
    private var printerManager: PrinterManager? = null

    private val _isPrinting = MutableStateFlow(false)
    val isPrinting = _isPrinting.asStateFlow()

    private val _printingPreview = MutableStateFlow<Bitmap?>(null)
    val printingPreview = _printingPreview.asStateFlow()

    fun initPrinter(manager: PrinterManager) {
        this.printerManager = manager
    }

    fun prepareForPrint(bitmap: Bitmap) {
        scope.launch {
            _isPrinting.value = true
            val dithered = withContext(Dispatchers.Default) {
                ImageProcessor.processForThermal(bitmap)
            }
            _printingPreview.value = dithered
        }
    }

    fun cancelPrint() {
        _printingPreview.value = null
        _isPrinting.value = false
    }

    fun confirmPrint() {
        val bitmap = _printingPreview.value ?: return
        scope.launch {
            printerManager?.printBitmap(bitmap)
            _printingPreview.value = null
            _isPrinting.value = false
        }
    }
}
