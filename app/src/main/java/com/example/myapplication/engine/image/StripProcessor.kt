package com.example.myapplication.engine.image

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color

/**
 * Handles combining multiple photos into vertical strips or grid layouts.
 */
object StripProcessor {

    /**
     * Combines multiple bitmaps vertically with spacing for printing.
     */
    fun combineForPrinting(photos: List<Bitmap>, spacing: Int = 20): Bitmap {
        if (photos.isEmpty()) throw IllegalArgumentException("Photo list is empty")
        
        val targetWidth = 1024 // High res base for combining
        val scale = targetWidth.toFloat() / photos[0].width
        val itemHeight = (photos[0].height * scale).toInt()
        
        val totalHeight = (itemHeight * photos.size) + (spacing * (photos.size - 1))
        
        val combined = Bitmap.createBitmap(targetWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(combined)
        canvas.drawColor(Color.WHITE) // Background for spacing
        
        for (i in photos.indices) {
            val scaled = Bitmap.createScaledBitmap(photos[i], targetWidth, itemHeight, true)
            canvas.drawBitmap(scaled, 0f, (i * (itemHeight + spacing)).toFloat(), null)
            if (scaled != photos[i]) scaled.recycle()
        }

        return combined
    }

    /**
     * Combines photos into a grid based on rows and columns.
     */
    fun combineBitmaps(photos: List<Bitmap>, rows: Int, cols: Int): Bitmap {
        if (photos.isEmpty()) throw IllegalArgumentException("Photo list is empty")
        
        val targetCombinedWidth = 1024
        val itemWidth = targetCombinedWidth / cols
        val aspectRatio = photos[0].height.toFloat() / photos[0].width.toFloat()
        val itemHeight = (itemWidth * aspectRatio).toInt()
        
        val combined = Bitmap.createBitmap(itemWidth * cols, itemHeight * rows, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(combined)
        
        var index = 0
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (index < photos.size) {
                    val scaled = Bitmap.createScaledBitmap(photos[index], itemWidth, itemHeight, true)
                    canvas.drawBitmap(scaled, (c * itemWidth).toFloat(), (r * itemHeight).toFloat(), null)
                    if (scaled != photos[index]) scaled.recycle()
                    index++
                }
            }
        }
        return combined
    }
}
