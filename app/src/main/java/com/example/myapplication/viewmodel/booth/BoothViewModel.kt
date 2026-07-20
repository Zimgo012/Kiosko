package com.example.myapplication.viewmodel.booth

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.controller.booth.BoothController
import com.example.myapplication.controller.booth.FrameType
import com.example.myapplication.controller.booth.GalleryController
import com.example.myapplication.controller.booth.PrinterController
import com.example.myapplication.controller.booth.SettingsController
import com.example.myapplication.engine.printer.PrintTemplateSettings
import com.example.myapplication.engine.printer.PrinterManager
import kotlinx.coroutines.flow.StateFlow

class BoothViewModel : ViewModel() {

    private val settingsController = SettingsController()
    private val galleryController = GalleryController()
    private val printerController = PrinterController(viewModelScope)
    
    private val boothController = BoothController(
        scope = viewModelScope,
        settingsController = settingsController,
        galleryController = galleryController
    )

    // States exposed to the UI
    val bitmaps: StateFlow<List<Bitmap>> = galleryController.bitmaps
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
    val printQueue: StateFlow<List<Pair<Bitmap, Int>>> = printerController.printQueue

    // UI Actions
    fun initPrinter(manager: PrinterManager) {
        printerController.initPrinter(manager)
    }

    fun addToPrintQueue(bitmap: Bitmap) {
        printerController.addToPrintQueue(bitmap)
    }

    fun removeFromPrintQueue(index: Int) {
        printerController.removeFromPrintQueue(index)
    }

    fun updateQuantityInQueue(index: Int, quantity: Int) {
        printerController.updateQuantityInQueue(index, quantity)
    }

    fun setFrameType(frameType: FrameType) {
        settingsController.setFrameType(frameType)
        boothController.resetSession()
    }

    fun toggleAutoStart() {
        settingsController.toggleAutoStart()
    }

    fun updatePrintTemplate(settings: PrintTemplateSettings) {
        settingsController.updatePrintTemplate(settings)
        printerController.updatePrinterSettings(settings)
    }

    fun toggleGalleryMaximize() {
        boothController.toggleGalleryMaximize()
    }

    fun setPhotoForPreview(bitmap: Bitmap?) {
        boothController.setPhotoForPreview(bitmap)
    }

    fun prepareForPrint(bitmap: Bitmap) {
        printerController.prepareForPrint(bitmap)
    }

    fun cancelPrint() {
        printerController.cancelPrint()
    }

    fun confirmPrint() {
        printerController.confirmPrint()
    }

    fun startCaptureCycle(onCapture: () -> Unit) {
        boothController.startCaptureCycle(onCapture)
    }

    fun onTakePhoto(bitmap: Bitmap) {
        boothController.onTakePhoto(bitmap)
    }
}
