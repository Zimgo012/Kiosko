package com.example.myapplication.controller.booth

import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GalleryController {
    private val _items = MutableStateFlow<List<Pair<Bitmap, Uri?>>>(emptyList())
    val items = _items.asStateFlow()

    fun addItem(bitmap: Bitmap, uri: Uri? = null) {
        _items.value = listOf(bitmap to uri) + _items.value
    }

    fun setItems(items: List<Pair<Bitmap, Uri?>>) {
        _items.value = items
    }
}
