package com.example.myapplication.engine.camera

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.engine.image.ImageProcessor
import com.example.myapplication.engine.printer.PrinterManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class FrameType(val title: String, val rows: Int, val cols: Int) {
    SINGLE("1 x 1", 1, 1),
    QUAD("2 x 2", 2, 2),
    SIX("3 x 2", 3, 2);

    val photoCount: Int get() = rows * cols
}

class CameraViewModel : ViewModel() {

    private val _bitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    val bitmaps = _bitmaps.asStateFlow()

    private val _selectedFrame = MutableStateFlow(FrameType.SINGLE)
    val selectedFrame = _selectedFrame.asStateFlow()

    private val _currentSessionPhotos = MutableStateFlow<List<Bitmap>>(emptyList())
    val currentSessionPhotos = _currentSessionPhotos.asStateFlow()

    private val _countdown = MutableStateFlow<Int?>(null)
    val countdown = _countdown.asStateFlow()

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing = _isCapturing.asStateFlow()

    private val _isAutoStartEnabled = MutableStateFlow(true)
    val isAutoStartEnabled = _isAutoStartEnabled.asStateFlow()

    private val _isPrinting = MutableStateFlow(false)
    val isPrinting = _isPrinting.asStateFlow()

    private var printerManager: PrinterManager? = null

    fun initPrinter(manager: PrinterManager) {
        this.printerManager = manager
    }

    fun setFrameType(frameType: FrameType) {
        _selectedFrame.value = frameType
        _currentSessionPhotos.value = emptyList()
    }

    fun toggleAutoStart() {
        _isAutoStartEnabled.value = !_isAutoStartEnabled.value
    }

    fun printPhoto(bitmap: Bitmap) {
        if (_isPrinting.value) return
        
        viewModelScope.launch {
            _isPrinting.value = true
            printerManager?.printBitmap(bitmap)
            _isPrinting.value = false
        }
    }

    fun startCaptureCycle(onCapture: () -> Unit) {
        if (_isCapturing.value) return
        _isCapturing.value = true
        
        viewModelScope.launch {
            val totalPhotos = _selectedFrame.value.photoCount
            val startFrom = _currentSessionPhotos.value.size
            
            for (photoIndex in startFrom until totalPhotos) {
                runCountdown()
                onCapture()
                
                if (!_isAutoStartEnabled.value && photoIndex < totalPhotos - 1) {
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
        val frameType = _selectedFrame.value
        val newSessionPhotos = _currentSessionPhotos.value + bitmap
        
        viewModelScope.launch {
            if (newSessionPhotos.size >= frameType.photoCount) {
                // Frame is complete - Process in background
                val combinedBitmap = withContext(Dispatchers.Default) {
                    if (frameType != FrameType.SINGLE) {
                        ImageProcessor.combineBitmaps(newSessionPhotos, frameType.rows, frameType.cols)
                    } else {
                        // For single, we still enhance it for the printer
                        ImageProcessor.enhanceForThermal(bitmap)
                    }
                }
                _bitmaps.value = _bitmaps.value + combinedBitmap
                _currentSessionPhotos.value = emptyList()
            } else {
                // More photos needed for this frame
                _currentSessionPhotos.value = newSessionPhotos
            }
        }
    }
}
