package com.example.myapplication.engine.image

import android.graphics.*
import com.example.myapplication.engine.printer.EventNameStyle
import com.example.myapplication.engine.printer.PrintTemplateSettings
import kotlin.math.*

object ImageProcessor {

    /**
     * Atkinson Dithering - 6-cell error diffusion kernel.
     * Provides a "cleaner" look for thermal printers by not diffusing the full error,
     * which reduces "scattering" artifacts and keeps highlights/shadows crisp.
     * Kernel:
     *           *   1   1
     *   1   1   1
     *       1
     *   (each neighbor gets 1/8 of the error)
     */
    fun applyAtkinsonDithering(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val gray = IntArray(pixels.size) { i ->
            val p = pixels[i]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            
            // Perceptual grayscale
            val luma = (0.2126 * r + 0.7152 * g + 0.0722 * b)
            
            // Gamma 0.85 and "compress" levels slightly (0.05 - 0.95 range) 
            // to eliminate sparse dots in very light/dark areas.
            val normalized = (luma / 255.0).pow(0.85)
            val clipped = ((normalized - 0.05) / 0.90).coerceIn(0.0, 1.0)
            (clipped * 255.0).toInt()
        }

        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val oldPixel = gray[index]
                
                val newPixel = if (oldPixel > 127) 255 else 0
                val error = oldPixel - newPixel
                
                gray[index] = newPixel
                
                // Atkinson distribution (Error / 8)
                val e8 = error / 8
                if (e8 == 0) continue
                
                distributeError(gray, x + 1, y, width, height, e8)
                distributeError(gray, x + 2, y, width, height, e8)
                distributeError(gray, x - 1, y + 1, width, height, error / 8)
                distributeError(gray, x,     y + 1, width, height, error / 8)
                distributeError(gray, x + 1, y + 1, width, height, error / 8)
                distributeError(gray, x,     y + 2, width, height, error / 8)
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
     * Environment-aware enhancement pipeline using Atkinson dithering.
     * Atkinson is used specifically to reduce "scattering" dot artifacts common in 
     * Floyd-Steinberg, resulting in a cleaner print on thermal paper.
     */
    fun processForThermal(source: Bitmap, targetWidth: Int = 512, autoAdjust: Boolean = true): Bitmap {
        val scale = targetWidth.toFloat() / source.width
        val targetHeight = (source.height * scale).toInt()
        val smallBitmap = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)

        val brightness = if (autoAdjust) analyzeBrightness(smallBitmap) else 0.5f
        
        // High contrast helps "compress" the dot patterns into solid shapes
        val contrast = if (brightness < 0.3f) 2.1f else 1.8f
        // Lift brightness to ensure backgrounds are clean white
        val shift = if (brightness > 0.7f) 30f else 20f
        
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
            totalBrightness += (Color.red(pixel) * 0.2126f + Color.green(pixel) * 0.7152f + Color.blue(pixel) * 0.0722f) / 255f
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
     * Adds a header (Event Name, Description) and footer (Booth Name, Contacts) to the photo.
     */
    fun applyPrintTemplate(
        source: Bitmap,
        template: PrintTemplateSettings,
        targetWidth: Int,
        borderSize: Int
    ): Bitmap {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
        }

        // Font Sizes
        val eventNameSize = 48f      // Base size increased
        val eventDescSize = 20f
        val boothNameSize = 26f
        val contactSize = 18f

        // Calculate Header Height based on Style
        val headerPadding = 40       // More base padding
        var headerHeight = (eventNameSize + eventDescSize + headerPadding).toInt()
        if (template.eventStyle != EventNameStyle.NORMAL) {
            // Split styles take more space because of two lines
            headerHeight = (eventNameSize * 2.2f + eventDescSize + headerPadding).toInt()
        }

        // Calculate Footer Height
        val footerPadding = 30
        val footerHeight = (boothNameSize + contactSize + footerPadding).toInt()

        // Calculate content dimensions
        val contentWidth = targetWidth - (borderSize * 2)
        val scale = contentWidth.toFloat() / source.width
        val contentHeight = (source.height * scale).toInt()
        
        // Extra vertical spacing constants
        val extraSpacingHeaderPhoto = 20f
        val extraSpacingPhotoFooter = 20f
        val extraSpacingBottom = 20f
        
        // Create final canvas
        val finalHeight = contentHeight + (borderSize * 2) + headerHeight + footerHeight + 
                          extraSpacingHeaderPhoto + extraSpacingPhotoFooter + extraSpacingBottom
        
        val output = Bitmap.createBitmap(targetWidth, finalHeight.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawColor(Color.WHITE)
        
        // --- DRAW HEADER (TOP) ---
        var currentY = 15f
        
        when (template.eventStyle) {
            EventNameStyle.RETRO -> {
                paint.textSkewX = -0.25f // SLANTED
                
                // Top Line
                paint.textSize = eventNameSize * 0.9f
                paint.typeface = Typeface.DEFAULT_BOLD
                canvas.drawText(template.topText.uppercase(), targetWidth / 2f, currentY + eventNameSize * 0.9f, paint)
                currentY += eventNameSize * 0.85f
                
                // Bottom Line
                paint.textSize = eventNameSize * 1.2f // Even Bigger
                paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                canvas.drawText(template.bottomText.uppercase(), targetWidth / 2f, currentY + eventNameSize * 1.2f, paint)
                currentY += eventNameSize * 1.2f + 10f
                
                paint.textSkewX = 0f // Reset slant
            }
            EventNameStyle.CURSIVE -> {
                // Top Line
                paint.textSize = eventNameSize
                paint.typeface = Typeface.create("serif", Typeface.ITALIC)
                canvas.drawText(template.topText, targetWidth / 2f, currentY + eventNameSize, paint)
                currentY += eventNameSize * 0.9f

                // Bottom Line
                paint.textSize = eventNameSize * 1.2f
                paint.typeface = Typeface.create("serif", Typeface.BOLD_ITALIC)
                canvas.drawText(template.bottomText, targetWidth / 2f, currentY + eventNameSize * 1.2f, paint)
                currentY += eventNameSize * 1.2f + 10f
            }
            EventNameStyle.MODERN -> {
                // Top Line
                paint.textSize = eventNameSize * 0.9f
                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                canvas.drawText(template.topText.uppercase(), targetWidth / 2f, currentY + eventNameSize * 0.9f, paint)
                currentY += eventNameSize * 0.85f

                // Bottom Line
                paint.textSize = eventNameSize * 1.3f
                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                canvas.drawText(template.bottomText.uppercase(), targetWidth / 2f, currentY + eventNameSize * 1.3f, paint)
                currentY += eventNameSize * 1.3f + 10f
            }
            EventNameStyle.NORMAL -> {
                paint.textSize = eventNameSize - 4f
                paint.typeface = Typeface.DEFAULT
                val combinedText = "${template.topText} ${template.bottomText}"
                canvas.drawText(combinedText, targetWidth / 2f, currentY + eventNameSize, paint)
                currentY += eventNameSize + 10f
            }
        }
        
        // Event Description
        paint.textSize = eventDescSize
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(template.eventDescription, targetWidth / 2f, currentY + eventDescSize, paint)
        
        // --- DRAW PHOTO ---
        // Extra space between header and photo
        val photoTop = headerHeight + borderSize.toFloat() + extraSpacingHeaderPhoto
        val scaledBitmap = Bitmap.createScaledBitmap(source, contentWidth, contentHeight, true)
        canvas.drawBitmap(scaledBitmap, borderSize.toFloat(), photoTop, null)
        scaledBitmap.recycle()
        
        // --- DRAW FOOTER (BOTTOM) ---
        currentY = photoTop + contentHeight + borderSize + extraSpacingPhotoFooter
        
        // Booth Name
        paint.textSize = boothNameSize
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText(template.boothName, targetWidth / 2f, currentY + boothNameSize, paint)
        currentY += boothNameSize + 4f
        
        // Contacts
        paint.textSize = contactSize
        paint.typeface = Typeface.DEFAULT
        val contactText = "${template.phoneNumber}  |  ${template.email}"
        canvas.drawText(contactText, targetWidth / 2f, currentY + contactSize, paint)

        return output
    }
}
