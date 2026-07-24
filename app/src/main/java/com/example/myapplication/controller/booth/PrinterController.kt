package com.example.myapplication.controller.booth

import android.graphics.Bitmap
import com.example.myapplication.engine.image.ImageProcessor
import com.example.myapplication.engine.printer.PrintTemplateSettings
import com.example.myapplication.engine.printer.PrinterManager
import com.example.myapplication.engine.storage.StorageManager
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
    private var storageManager: StorageManager? = null
    private var originalBitmapToPrint: Bitmap? = null
    private var previewJob: Job? = null

    private val _isPrinting = MutableStateFlow(false)
    val isPrinting = _isPrinting.asStateFlow()

    private val _printingPreview = MutableStateFlow<Bitmap?>(null)
    val printingPreview = _printingPreview.asStateFlow()

    private val _printQueue = MutableStateFlow<List<Bitmap>>(emptyList())
    val printQueue = _printQueue.asStateFlow()

    private val _printQuantity = MutableStateFlow(1)
    val printQuantity = _printQuantity.asStateFlow()

    private val _showQueueFullWarning = MutableStateFlow(false)
    val showQueueFullWarning = _showQueueFullWarning.asStateFlow()

    private var isRecentPrintJob = false

    fun initPrinter(manager: PrinterManager) {
        this.printerManager = manager
    }

    fun initStorage(manager: StorageManager) {
        this.storageManager = manager
    }

    fun updatePrinterSettings(templateSettings: PrintTemplateSettings) {
        printerManager?.let {
            it.settings = it.settings.copy(templateSettings = templateSettings)
        }
        updatePrintPreview()
    }

    fun prepareForPrint(bitmap: Bitmap, isRecentPrint: Boolean = false) {
        originalBitmapToPrint = bitmap
        isRecentPrintJob = isRecentPrint
        _printQueue.value = listOf(bitmap)
        _printQuantity.value = 1
        updatePrintPreview()
    }

    fun addToPrintQueue(bitmap: Bitmap) {
        if (isRecentPrintJob) return // Don't add more photos to a finished strip
        
        val currentQueue = _printQueue.value.toMutableList()
        if (currentQueue.size < 5) {
            currentQueue.add(bitmap)
            _printQueue.value = currentQueue
            updatePrintPreview()
        } else {
            _showQueueFullWarning.value = true
        }
    }

    fun dismissQueueFullWarning() {
        _showQueueFullWarning.value = false
    }

    fun removeFromPrintQueue(index: Int) {
        val currentQueue = _printQueue.value.toMutableList()
        if (index in currentQueue.indices) {
            currentQueue.removeAt(index)
            _printQueue.value = currentQueue
            if (currentQueue.isEmpty()) {
                cancelPrint()
            } else {
                updatePrintPreview()
            }
        }
    }

    fun setPrintQuantity(quantity: Int) {
        _printQuantity.value = quantity.coerceIn(1, 10)
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
                    val combined = if (queue.size > 1) {
                        ImageProcessor.combineForPrinting(queue, spacing = 40)
                    } else {
                        queue.first()
                    }
                    
                    // Only apply template if it's not already a finished strip
                    if (!isRecentPrintJob) {
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
                        
                        if (withTemplate != combined) withTemplate.recycle()
                        if (combined !in queue) combined.recycle()
                        
                        result
                    } else {
                        // It's already a finished strip (dithered with header/footer)
                        // Just ensure it fits the current paper width without re-dithering or re-templating
                        val settings = printerManager?.settings ?: com.example.myapplication.engine.printer.PrintSettings()
                        val targetWidth = settings.paperWidthDots
                        
                        if (combined.width == targetWidth) {
                            combined
                        } else {
                            val scale = targetWidth.toFloat() / combined.width
                            val targetHeight = (combined.height * scale).toInt()
                            Bitmap.createScaledBitmap(combined, targetWidth, targetHeight, true)
                        }
                    }
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
        _printQuantity.value = 1
        originalBitmapToPrint = null
        isRecentPrintJob = false
    }

    fun confirmPrint() {
        val bitmap = _printingPreview.value ?: return
        val quantity = _printQuantity.value
        scope.launch {
            val printSuccess = printerManager?.printBitmap(bitmap, quantity) ?: false
            if (printSuccess) {
                // Save the printed strip to the client's album (only if it's a new strip)
                if (!isRecentPrintJob) {
                    withContext(Dispatchers.IO) {
                        storageManager?.savePrintedStrip(
                            bitmap = bitmap,
                            clientFolder = printerManager?.settings?.templateSettings?.clientFolderName ?: "default"
                        )
                    }
                }
            }
            _printingPreview.value = null
            _isPrinting.value = false
            _printQueue.value = emptyList()
            isRecentPrintJob = false
        }
    }
}
