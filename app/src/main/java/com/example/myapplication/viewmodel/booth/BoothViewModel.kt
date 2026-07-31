package com.example.myapplication.viewmodel.booth

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.controller.booth.BoothController
import com.example.myapplication.controller.booth.FrameType
import com.example.myapplication.controller.booth.GalleryController
import com.example.myapplication.controller.booth.PrinterController
import com.example.myapplication.controller.booth.SettingsController
import com.example.myapplication.engine.printer.PrintTemplateSettings
import com.example.myapplication.engine.printer.PrinterManager
import com.example.myapplication.engine.storage.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BoothViewModel : ViewModel() {

    private val settingsController = SettingsController()
    private val galleryController = GalleryController()
    private val printerController = PrinterController(viewModelScope)
    private var storageManager: StorageManager? = null
    
    private val boothController = BoothController(
        scope = viewModelScope,
        settingsController = settingsController,
        galleryController = galleryController
    )

    // States exposed to the UI
    val bitmaps: StateFlow<List<Pair<Bitmap, Uri?>>> = galleryController.items
    val selectedFrame: StateFlow<FrameType> = settingsController.selectedFrame
    val isAutoStartEnabled: StateFlow<Boolean> = settingsController.isAutoStartEnabled
    val printTemplateSettings: StateFlow<PrintTemplateSettings> = settingsController.printTemplateSettings
    
    val currentSessionPhotos: StateFlow<List<Bitmap>> = boothController.currentSessionPhotos
    val countdown: StateFlow<Int?> = boothController.countdown
    val isCapturing: StateFlow<Boolean> = boothController.isCapturing

    val isGalleryMaximized: StateFlow<Boolean> = boothController.isGalleryMaximized
    val selectedPhotoForPreview: StateFlow<Bitmap?> = boothController.selectedPhotoForPreview

    val isPrinting: StateFlow<Boolean> = printerController.isPrinting
    val printingPreview: StateFlow<Bitmap?> = printerController.printingPreview
    val printQueue: StateFlow<List<Bitmap>> = printerController.printQueue
    val printQuantity: StateFlow<Int> = printerController.printQuantity
    val showQueueFullWarning: StateFlow<Boolean> = printerController.showQueueFullWarning

    private val _availableFolders = MutableStateFlow<List<String>>(emptyList())
    val availableFolders = _availableFolders.asStateFlow()

    private val _recentPrints = MutableStateFlow<List<Pair<Bitmap, android.net.Uri>>>(emptyList())
    val recentPrints = _recentPrints.asStateFlow()

    private val _showRecentPrints = MutableStateFlow(false)
    val showRecentPrints = _showRecentPrints.asStateFlow()

    // UI Actions
    fun initPrinter(manager: PrinterManager) {
        printerController.initPrinter(manager)
    }

    fun initStorage(manager: StorageManager) {
        this.storageManager = manager
        boothController.initStorage(manager)
        printerController.initStorage(manager)
        refreshFolders()
        loadGalleryFromFolder(settingsController.printTemplateSettings.value.clientFolderName)
    }

    fun loadGalleryFromFolder(folderName: String) {
        viewModelScope.launch {
            val items = withContext(Dispatchers.IO) {
                storageManager?.loadBitmapsFromFolder(folderName) ?: emptyList()
            }
            galleryController.setItems(items)
            
            // Also refresh recent prints when folder changes
            refreshRecentPrints()
        }
    }

    fun refreshRecentPrints() {
        val folderName = settingsController.printTemplateSettings.value.clientFolderName
        viewModelScope.launch {
            val prints = withContext(Dispatchers.IO) {
                storageManager?.loadBitmapsFromFolder(folderName, isRecentPrint = true) ?: emptyList()
            }
            _recentPrints.value = prints
        }
    }

    fun setShowRecentPrints(show: Boolean) {
        if (show) refreshRecentPrints()
        _showRecentPrints.value = show
    }

    fun refreshFolders() {
        storageManager?.let {
            _availableFolders.value = it.getAvailableFolders()
        }
    }

    fun addToPrintQueue(bitmap: Bitmap) {
        printerController.addToPrintQueue(bitmap)
    }

    fun dismissQueueFullWarning() {
        printerController.dismissQueueFullWarning()
    }

    fun removeFromPrintQueue(index: Int) {
        printerController.removeFromPrintQueue(index)
    }

    fun updateQuantityInQueue(index: Int, quantity: Int) {
        // No longer used per-photo, now used per-strip
    }

    fun setPrintQuantity(quantity: Int) {
        printerController.setPrintQuantity(quantity)
    }

    fun setFrameType(frameType: FrameType) {
        settingsController.setFrameType(frameType)
        boothController.resetSession()
    }

    fun toggleAutoStart() {
        settingsController.toggleAutoStart()
    }

    fun updatePrintTemplate(settings: PrintTemplateSettings) {
        val oldFolderName = settingsController.printTemplateSettings.value.clientFolderName
        settingsController.updatePrintTemplate(settings)
        printerController.updatePrinterSettings(settings)
        
        if (oldFolderName != settings.clientFolderName) {
            loadGalleryFromFolder(settings.clientFolderName)
        }
    }

    fun toggleGalleryMaximize() {
        boothController.toggleGalleryMaximize()
    }

    fun setPhotoForPreview(bitmap: Bitmap?) {
        boothController.setPhotoForPreview(bitmap)
    }

    fun prepareForPrint(bitmap: Bitmap, isRecentPrint: Boolean = false, uri: Uri? = null) {
        if (isRecentPrint && uri != null) {
            viewModelScope.launch {
                val fullBitmap = withContext(Dispatchers.IO) {
                    storageManager?.loadFullBitmap(uri)
                }
                fullBitmap?.let {
                    printerController.prepareForPrint(it, isRecentPrint = true)
                }
            }
        } else {
            printerController.prepareForPrint(bitmap, isRecentPrint)
        }
    }

    fun cancelPrint() {
        printerController.cancelPrint()
    }

    fun confirmPrint() {
        printerController.confirmPrint()
        // Refresh after a delay to ensure MediaStore is updated
        viewModelScope.launch {
            delay(1000)
            refreshRecentPrints()
        }
    }

    fun testPrint() {
        // Create a simple test bitmap
        val testBitmap = Bitmap.createBitmap(512, 200, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(testBitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 40f
            isFakeBoldText = true
        }
        canvas.drawText("PRINTER TEST", 100f, 80f, paint)
        canvas.drawText("OK", 220f, 150f, paint)
        
        printerController.addToPrintQueue(testBitmap)
        printerController.confirmPrint()
    }

    fun startCaptureCycle(onCapture: () -> Unit) {
        boothController.startCaptureCycle(onCapture)
    }

    fun onTakePhoto(bitmap: Bitmap) {
        boothController.onTakePhoto(bitmap)
    }
}
