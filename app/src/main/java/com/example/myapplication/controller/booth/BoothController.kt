package com.example.myapplication.controller.booth

import android.graphics.Bitmap
import com.example.myapplication.engine.image.ImageProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BoothController(
    private val scope: CoroutineScope,
    private val settingsController: SettingsController,
    private val galleryController: GalleryController
) {
    private val _currentSessionPhotos = MutableStateFlow<List<Bitmap>>(emptyList())
    val currentSessionPhotos = _currentSessionPhotos.asStateFlow()

    private val _countdown = MutableStateFlow<Int?>(null)
    val countdown = _countdown.asStateFlow()

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing = _isCapturing.asStateFlow()

    // UI Logic States
    private val _isGalleryMaximized = MutableStateFlow(false)
    val isGalleryMaximized = _isGalleryMaximized.asStateFlow()

    private val _selectedPhotoForPreview = MutableStateFlow<Bitmap?>(null)
    val selectedPhotoForPreview = _selectedPhotoForPreview.asStateFlow()

    fun toggleGalleryMaximize() {
        _isGalleryMaximized.value = !_isGalleryMaximized.value
    }

    fun setPhotoForPreview(bitmap: Bitmap?) {
        _selectedPhotoForPreview.value = bitmap
    }

    fun startCaptureCycle(onCapture: () -> Unit) {
        if (_isCapturing.value) return
        _isCapturing.value = true
        
        scope.launch {
            val totalPhotos = settingsController.selectedFrame.value.photoCount
            val startFrom = _currentSessionPhotos.value.size
            
            for (photoIndex in startFrom until totalPhotos) {
                runCountdown()
                onCapture()
                
                if (!settingsController.isAutoStartEnabled.value && photoIndex < totalPhotos - 1) {
                    break
                }

                if (totalPhotos > 1 && photoIndex < totalPhotos - 1) {
                    delay(1000)
                }
            }
            _isCapturing.value = false
        }
    }

    private suspend fun runCountdown() {
        for (i in 3 downTo 1) {
            _countdown.value = i
            delay(1000)
        }
        _countdown.value = null
    }

    fun onTakePhoto(bitmap: Bitmap) {
        val frameType = settingsController.selectedFrame.value
        val newSessionPhotos = _currentSessionPhotos.value + bitmap
        
        if (newSessionPhotos.size >= frameType.photoCount) {
            scope.launch {
                val combinedBitmap = withContext(Dispatchers.Default) {
                    if (frameType != FrameType.SINGLE) {
                        ImageProcessor.combineBitmaps(newSessionPhotos, frameType.rows, frameType.cols)
                    } else {
                        bitmap
                    }
                }
                galleryController.addBitmap(combinedBitmap)
                _currentSessionPhotos.value = emptyList()
            }
        } else {
            _currentSessionPhotos.value = newSessionPhotos
        }
    }

    fun resetSession() {
        _currentSessionPhotos.value = emptyList()
    }
}
