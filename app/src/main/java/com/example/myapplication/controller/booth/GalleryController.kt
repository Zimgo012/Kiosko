package com.example.myapplication.controller.booth

import android.graphics.Bitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GalleryController {
    private val _bitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    val bitmaps = _bitmaps.asStateFlow()

    fun addBitmap(bitmap: Bitmap) {
        _bitmaps.value = listOf(bitmap) + _bitmaps.value
    }

    fun setBitmaps(bitmaps: List<Bitmap>) {
        _bitmaps.value = bitmaps
    }
}
