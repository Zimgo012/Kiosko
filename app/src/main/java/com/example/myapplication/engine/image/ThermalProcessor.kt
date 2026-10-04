package com.example.myapplication.engine.image

import android.graphics.*
import kotlin.math.*

/**
 * Handles image processing specifically for thermal printing,
 * including contrast enhancement and Atkinson dithering.
 */
object ThermalProcessor {

    /**
     * Environment-aware enhancement pipeline using Atkinson dithering.
     */
    fun processForThermal(
        source: Bitmap,
        targetWidth: Int = 512,
        autoAdjust: Boolean = true,
        brightnessShiftOffset: Float = 0f,
        contrastOverride: Float? = null,
        gammaOverride: Float? = null
    ): Bitmap {
        val scale = targetWidth.toFloat() / source.width
        val targetHeight = (source.height * scale).toInt()
        val smallBitmap = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)

        val brightness = if (autoAdjust) analyzeBrightness(smallBitmap) else 0.5f
        
        // Dynamic contrast and shift based on image brightness:
        val baseContrast = (1.5f - brightness * 0.3f).coerceIn(1.2f, 1.6f)
        val contrast = contrastOverride ?: baseContrast
        val shift = 15f + (brightness * 35f) + brightnessShiftOffset
        
        val enhanced = enhanceForThermal(smallBitmap, contrast, shift)
        val dithered = applyAtkinsonDithering(enhanced, brightness, gammaOverride)
        
        if (enhanced != smallBitmap) enhanced.recycle()
        smallBitmap.recycle()
        
        return dithered
    }

    /**
     * Enhances contrast around the midpoint (128) and applies a brightness shift.
     * Formula: P_out = (P_in - 128) * contrast + 128 + shift
     * Matrix offset = 128 * (1 - contrast) + shift
     */
    private fun enhanceForThermal(source: Bitmap, contrast: Float, brightnessShift: Float): Bitmap {
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()

        val offset = 128f * (1f - contrast) + brightnessShift

        val cm = ColorMatrix().apply {
            setSaturation(0f)
            val matrix = floatArrayOf(
                contrast, 0f, 0f, 0f, offset,
                0f, contrast, 0f, 0f, offset,
                0f, 0f, contrast, 0f, offset,
                0f, 0f, 0f, 1f, 0f
            )
            postConcat(ColorMatrix(matrix))
        }

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return output
    }

    /**
     * Atkinson Dithering - 6-cell error diffusion kernel.
     * Incorporates gamma compensation and dynamic highlight cutoff based on image brightness.
     */
    private fun applyAtkinsonDithering(
        source: Bitmap,
        avgBrightness: Float = 0.5f,
        gammaOverride: Float? = null
    ): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val baseGamma = (0.85f - (avgBrightness - 0.5f) * 0.2f).coerceIn(0.70f, 0.90f)
        val gamma = gammaOverride ?: baseGamma
        val whiteCutoff = (0.92f - (avgBrightness - 0.5f) * 0.05f).coerceIn(0.85f, 0.95f)

        val gray = IntArray(pixels.size) { i ->
            val p = pixels[i]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            
            val luma = (0.2126 * r + 0.7152 * g + 0.0722 * b)
            val normalized = (luma / 255.0).pow(gamma.toDouble())
            val clipped = ((normalized - 0.03) / (whiteCutoff - 0.03)).coerceIn(0.0, 1.0)
            (clipped * 255.0).toInt()
        }

        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val oldPixel = gray[index]
                val newPixel = if (oldPixel > 127) 255 else 0
                val error = oldPixel - newPixel
                gray[index] = newPixel
                
                val e8 = error / 8
                if (e8 == 0) continue
                
                distributeError(gray, x + 1, y, width, height, e8)
                distributeError(gray, x + 2, y, width, height, e8)
                distributeError(gray, x - 1, y + 1, width, height, e8)
                distributeError(gray, x,     y + 1, width, height, e8)
                distributeError(gray, x + 1, y + 1, width, height, e8)
                distributeError(gray, x,     y + 2, width, height, e8)
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
     * Deterministic grid sampling of luminance across the bitmap.
     */
    private fun analyzeBrightness(bitmap: Bitmap): Float {
        val width = bitmap.width
        val height = bitmap.height
        val stepX = (width / 20).coerceAtLeast(1)
        val stepY = (height / 20).coerceAtLeast(1)
        var totalLuma = 0f
        var count = 0

        for (y in 0 until height step stepY) {
            for (x in 0 until width step stepX) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                totalLuma += (0.2126f * r + 0.7152f * g + 0.0722f * b) / 255f
                count++
            }
        }
        return if (count > 0) totalLuma / count else 0.5f
    }
}

