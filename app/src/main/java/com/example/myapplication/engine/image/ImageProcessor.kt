package com.example.myapplication.engine.image

import android.graphics.Bitmap
import android.graphics.Canvas

object ImageProcessor {

    fun combineBitmaps(photos: List<Bitmap>, rows: Int, cols: Int): Bitmap {
        if (photos.isEmpty()) throw IllegalArgumentException("Photo list is empty")
        if (photos.size < rows * cols) return photos.last()

        val itemWidth = photos[0].width
        val itemHeight = photos[0].height
        val config = photos[0].config ?: Bitmap.Config.ARGB_8888

        val combined = Bitmap.createBitmap(itemWidth * cols, itemHeight * rows, config)
        val canvas = Canvas(combined)

        var index = 0
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                if (index < photos.size) {
                    canvas.drawBitmap(
                        photos[index],
                        (col * itemWidth).toFloat(),
                        (row * itemHeight).toFloat(),
                        null
                    )
                    index++
                }
            }
        }

        return combined
    }
}
