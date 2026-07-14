package com.example.myapplication.engine.image

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

object ImageProcessor {

    /**
     * Combines multiple bitmaps into a grid, with resizing to prevent OOM
     * and enhancements for thermal printing.
     */
    fun combineBitmaps(photos: List<Bitmap>, rows: Int, cols: Int): Bitmap {
        if (photos.isEmpty()) throw IllegalArgumentException("Photo list is empty")
        
        // Target a reasonable size for the final combined image to avoid OOM
        // We'll target a width of 1024. For a 2-column grid, each photo will be 512px wide.
        val targetCombinedWidth = 1024
        val itemWidth = targetCombinedWidth / cols
        
        // Use aspect ratio of first photo to determine item height
        val aspectRatio = photos[0].height.toFloat() / photos[0].width.toFloat()
        val itemHeight = (itemWidth * aspectRatio).toInt()
        
        val combined = Bitmap.createBitmap(
            itemWidth * cols, 
            itemHeight * rows, 
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(combined)
        val paint = Paint()

        var index = 0
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (index < photos.size) {
                    // Resize first to save memory before enhancement
                    val scaled = Bitmap.createScaledBitmap(photos[index], itemWidth, itemHeight, true)
                    
                    // Enhance for thermal (grayscale + contrast)
                    val enhanced = enhanceForThermal(scaled)
                    
                    canvas.drawBitmap(
                        enhanced,
                        (c * itemWidth).toFloat(),
                        (r * itemHeight).toFloat(),
                        paint
                    )
                    
                    // Cleanup intermediate bitmaps
                    if (scaled != photos[index]) scaled.recycle()
                    enhanced.recycle()
                    
                    index++
                }
            }
        }

        return combined
    }

    /**
     * Enhances an image for thermal printing by increasing contrast and converting to grayscale.
     */
    fun enhanceForThermal(source: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()

        val cm = ColorMatrix().apply {
            setSaturation(0f) // Convert to grayscale
            
            // Increase contrast (Scale the RGB values)
            val contrast = 1.5f
            val brightness = -10f
            val matrix = floatArrayOf(
                contrast, 0f, 0f, 0f, brightness,
                0f, contrast, 0f, 0f, brightness,
                0f, 0f, contrast, 0f, brightness,
                0f, 0f, 0f, 1f, 0f
            )
            postConcat(ColorMatrix(matrix))
        }

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(source, 0f, 0f, paint)
        
        return output
    }
}
