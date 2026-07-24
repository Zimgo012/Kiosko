package com.example.myapplication.engine.storage

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StorageManager(private val context: Context) {

    fun saveCapturedPhoto(bitmap: Bitmap, clientFolder: String): Uri? {
        val fileName = "IMG_${getTimestamp()}.jpg"
        val relativePath = "Pictures/RollieBooth/$clientFolder/"
        return saveBitmapToMediaStore(bitmap, fileName, relativePath)
    }

    fun savePrintedStrip(bitmap: Bitmap, clientFolder: String): Uri? {
        val fileName = "STRIP_${getTimestamp()}.jpg"
        val relativePath = "Pictures/RollieBooth/$clientFolder/recent-print"
        return saveBitmapToMediaStore(bitmap, fileName, relativePath)
    }

    fun getAvailableFolders(): List<String> {
        val folders = mutableSetOf<String>()
        val projection = arrayOf(MediaStore.Images.Media.RELATIVE_PATH)
        val selection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf("Pictures/RollieBooth/%")

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            val relativePathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)
            while (cursor.moveToNext()) {
                val path = cursor.getString(relativePathColumn)
                val parts = path.split("/")
                if (parts.size >= 3) {
                    folders.add(parts[2])
                }
            }
        }
        return folders.toList().sorted()
    }

    fun loadBitmapsFromFolder(clientFolder: String, isRecentPrint: Boolean = false): List<Pair<Bitmap, Uri>> {
        val results = mutableListOf<Pair<Bitmap, Uri>>()
        val projection = arrayOf(MediaStore.Images.Media._ID)
        
        val folderPath = if (isRecentPrint) {
            "Pictures/RollieBooth/$clientFolder/recent-print/"
        } else {
            "Pictures/RollieBooth/$clientFolder/"
        }
        
        val selection = "${MediaStore.Images.Media.RELATIVE_PATH} = ?"
        val selectionArgs = arrayOf(folderPath)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            var count = 0
            while (cursor.moveToNext() && count < 50) {
                val id = cursor.getLong(idColumn)
                val contentUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
                try {
                    context.contentResolver.openInputStream(contentUri)?.use { inputStream ->
                        val options = BitmapFactory.Options().apply {
                            inSampleSize = if (isRecentPrint) 1 else 4 // Full res for prints
                        }
                        BitmapFactory.decodeStream(inputStream, null, options)?.let { 
                            results.add(it to contentUri) 
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                count++
            }
        }
        return results
    }

    fun loadFullBitmap(uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { 
                BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun saveBitmapToMediaStore(
        bitmap: Bitmap,
        fileName: String,
        relativePath: String
    ): Uri? {
        val resolver = context.contentResolver
        
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        
        imageUri?.let { uri ->
            try {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
            } catch (e: Exception) {
                resolver.delete(uri, null, null)
                return null
            }
        }
        
        return imageUri
    }

    private fun getTimestamp(): String {
        return SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    }
}
