package com.example.myapplication.engine.image

import android.graphics.*
import kotlin.math.*

object ImageProcessor {

    /**
     * Atkinson Dithering - A fast and high-quality alternative to Riemersma.
     * Riemersma can be extremely slow on large images due to Hilbert curve traversal.
     */
    fun applyAtkinsonDithering(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val gray = IntArray(pixels.size) { i ->
            val p = pixels[i]
            (0.299 * Color.red(p) + 0.587 * Color.green(p) + 0.114 * Color.blue(p)).toInt()
        }

        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val oldPixel = gray[index]
                val newPixel = if (oldPixel > 128) 255 else 0
                val error = (oldPixel - newPixel) / 8
                
                gray[index] = newPixel
                
                // Atkinson distribution
                distributeError(gray, x + 1, y, width, height, error)
                distributeError(gray, x + 2, y, width, height, error)
                distributeError(gray, x - 1, y + 1, width, height, error)
                distributeError(gray, x, y + 1, width, height, error)
                distributeError(gray, x + 1, y + 1, width, height, error)
                distributeError(gray, x, y + 2, width, height, error)
            }
        }

        val outPixels = IntArray(pixels.size)
        for (i in pixels.indices) {
            val g = gray[i].coerceIn(0, 255)
            outPixels[i] = Color.rgb(g, g, g)
        }

        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        output.setPixels(outPixels, 0, width, 0, 0, width, height)
        return output
    }

    private fun distributeError(data: IntArray, x: Int, y: Int, width: Int, height: Int, error: Int) {
        if (x in 0 until width && y in 0 until height) {
            val index = y * width + x
            data[index] = (data[index] + error).coerceIn(0, 255)
        }
    }

    /**
     * Environment-aware enhancement pipeline.
     */
    fun processForThermal(source: Bitmap, autoAdjust: Boolean = true): Bitmap {
        // Downscale before processing to speed up dithering and avoid OOM
        val targetWidth = 512
        val scale = targetWidth.toFloat() / source.width
        val targetHeight = (source.height * scale).toInt()
        val smallBitmap = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)

        val brightness = if (autoAdjust) analyzeBrightness(smallBitmap) else 0.5f
        
        val contrast = if (brightness < 0.3f) 1.6f else 1.3f
        val shift = if (brightness > 0.7f) -25f else -5f
        
        val enhanced = enhanceForThermal(smallBitmap, contrast, shift)
        val dithered = applyAtkinsonDithering(enhanced)
        
        if (enhanced != smallBitmap) enhanced.recycle()
        smallBitmap.recycle()
        
        return dithered
    }

    fun enhanceForThermal(source: Bitmap, contrast: Float, brightness: Float): Bitmap {
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()

        val cm = ColorMatrix().apply {
            setSaturation(0f)
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

    private fun analyzeBrightness(bitmap: Bitmap): Float {
        var totalBrightness = 0f
        val sampleSize = 100
        val width = bitmap.width
        val height = bitmap.height
        
        repeat(sampleSize) {
            val x = (Math.random() * width).toInt()
            val y = (Math.random() * height).toInt()
            val pixel = bitmap.getPixel(x, y)
            totalBrightness += (Color.red(pixel) * 0.299f + Color.green(pixel) * 0.587f + Color.blue(pixel) * 0.114f) / 255f
        }
        return totalBrightness / sampleSize
    }

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
