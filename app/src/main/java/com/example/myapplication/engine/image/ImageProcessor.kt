package com.example.myapplication.engine.image

import android.graphics.Bitmap
import com.example.myapplication.engine.printer.PrintTemplateSettings

/**
 * Main orchestrator for image processing in the Photo Booth.
 * Delegates specific tasks to specialized processors.
 */
object ImageProcessor {

    /**
     * Environment-aware enhancement pipeline using Atkinson dithering.
     */
    fun processForThermal(source: Bitmap, targetWidth: Int = 512, autoAdjust: Boolean = true): Bitmap {
        return ThermalProcessor.processForThermal(source, targetWidth, autoAdjust)
    }

    /**
     * Combines multiple bitmaps vertically with spacing for printing.
     */
    fun combineForPrinting(photos: List<Bitmap>, spacing: Int = 20): Bitmap {
        return StripProcessor.combineForPrinting(photos, spacing)
    }

    /**
     * Combines photos into a grid based on rows and columns.
     */
    fun combineBitmaps(photos: List<Bitmap>, rows: Int, cols: Int): Bitmap {
        return StripProcessor.combineBitmaps(photos, rows, cols)
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
        return TemplateProcessor.applyPrintTemplate(source, template, targetWidth, borderSize)
    }
}
