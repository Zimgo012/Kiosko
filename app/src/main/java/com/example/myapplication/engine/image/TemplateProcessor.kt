package com.example.myapplication.engine.image

import android.graphics.*
import com.example.myapplication.engine.printer.EventNameStyle
import com.example.myapplication.engine.printer.PrintTemplateSettings

/**
 * Handles drawing the print template (header and footer) onto photos.
 */
object TemplateProcessor {

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
