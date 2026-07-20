package com.example.myapplication.controller.booth

import android.graphics.Bitmap
import com.example.myapplication.engine.image.ImageProcessor
import com.example.myapplication.engine.printer.PrintTemplateSettings
import com.example.myapplication.engine.printer.PrinterManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PrinterController(
    private val scope: CoroutineScope
) {
    private var printerManager: PrinterManager? = null
    private var originalBitmapToPrint: Bitmap? = null
    private var previewJob: Job? = null

    private val _isPrinting = MutableStateFlow(false)
    val isPrinting = _isPrinting.asStateFlow()

    private val _printingPreview = MutableStateFlow<Bitmap?>(null)
    val printingPreview = _printingPreview.asStateFlow()

    private val _printQueue = MutableStateFlow<List<Pair<Bitmap, Int>>>(emptyList())
    val printQueue = _printQueue.asStateFlow()

    fun initPrinter(manager: PrinterManager) {
        this.printerManager = manager
    }

    fun updatePrinterSettings(templateSettings: PrintTemplateSettings) {
        printerManager?.let {
            it.settings = it.settings.copy(templateSettings = templateSettings)
        }
        updatePrintPreview()
    }

    fun prepareForPrint(bitmap: Bitmap) {
        originalBitmapToPrint = bitmap
        _printQueue.value = listOf(bitmap to 1)
        updatePrintPreview()
    }

    fun addToPrintQueue(bitmap: Bitmap) {
        val currentQueue = _printQueue.value.toMutableList()
        if (currentQueue.size < 5) {
            currentQueue.add(bitmap to 1)
            _printQueue.value = currentQueue
            updatePrintPreview()
        }
    }

    fun removeFromPrintQueue(index: Int) {
        val currentQueue = _printQueue.value.toMutableList()
        if (index in currentQueue.indices) {
            currentQueue.removeAt(index)
            _printQueue.value = currentQueue
            updatePrintPreview()
        }
    }

    fun updateQuantityInQueue(index: Int, quantity: Int) {
        val currentQueue = _printQueue.value.toMutableList()
        if (index in currentQueue.indices) {
            currentQueue[index] = currentQueue[index].first to quantity.coerceIn(1, 5)
            _printQueue.value = currentQueue
            updatePrintPreview()
        }
    }

    private fun updatePrintPreview() {
        previewJob?.cancel()
        val queue = _printQueue.value
        if (queue.isEmpty()) {
            _printingPreview.value = null
            _isPrinting.value = false
            return
        }

        previewJob = scope.launch {
            _isPrinting.value = true
            try {
                val processed = withContext(Dispatchers.Default) {
                    val bitmapsToCombine = mutableListOf<Bitmap>()
                    queue.forEach { (bitmap, qty) ->
                        repeat(qty) { bitmapsToCombine.add(bitmap) }
                    }

                    val combined = if (bitmapsToCombine.size > 1) {
                        ImageProcessor.combineForPrinting(bitmapsToCombine, spacing = 40)
                    } else {
                        bitmapsToCombine.first()
                    }
                    
                    val settings = printerManager?.settings ?: com.example.myapplication.engine.printer.PrintSettings()
                    
                    // 1. Add Template (Logo, Header, Footer)
                    val withTemplate = ImageProcessor.applyPrintTemplate(
                        source = combined,
                        template = settings.templateSettings,
                        targetWidth = settings.paperWidthDots,
                        borderSize = settings.borderSizeDots
                    )
                    
                    // 2. Process for Thermal (Grayscale + Dithering)
                    val result = ImageProcessor.processForThermal(withTemplate, targetWidth = settings.paperWidthDots)
                    
                    // Cleanup intermediate bitmaps
                    if (withTemplate != combined) withTemplate.recycle()
                    if (bitmapsToCombine.size > 1) combined.recycle()
                    
                    result
                }
                _printingPreview.value = processed
            } finally {
                _isPrinting.value = false
            }
        }
    }

    fun cancelPrint() {
        _printingPreview.value = null
        _isPrinting.value = false
        _printQueue.value = emptyList()
        originalBitmapToPrint = null
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
