package com.b0966031908gmail.happypacker.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object FileHelper {

    private const val ARTWORKS_DIR = "artworks"

    fun saveArtwork(context: Context, bitmap: Bitmap): String? {
        try {
            val artworksDir = File(context.filesDir, ARTWORKS_DIR)
            if (!artworksDir.exists()) {
                artworksDir.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(Date())
            val fileName = "artwork_$timestamp.png"
            val file = File(artworksDir, fileName)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun saveArtworkWithName(context: Context, bitmap: Bitmap, fileName: String): String? {
        try {
            val artworksDir = File(context.filesDir, ARTWORKS_DIR)
            if (!artworksDir.exists()) {
                artworksDir.mkdirs()
            }

            // 清理檔名（移除特殊字元）
            val cleanFileName = fileName.replace(Regex("[^a-zA-Z0-9\\u4e00-\\u9fa5_\\-]"), "_")
            val file = File(artworksDir, "$cleanFileName.png")

            // 如果檔案存在，加上編號
            var finalFile = file
            var counter = 1
            while (finalFile.exists()) {
                finalFile = File(artworksDir, "${cleanFileName}_$counter.png")
                counter++
            }

            FileOutputStream(finalFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            return finalFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun loadArtwork(filePath: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(filePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getAllArtworks(context: Context): List<File> {
        val artworksDir = File(context.filesDir, ARTWORKS_DIR)
        if (!artworksDir.exists()) {
            return emptyList()
        }

        return artworksDir.listFiles()
            ?.filter { it.extension == "png" }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    fun deleteArtwork(filePath: String): Boolean {
        return try {
            File(filePath).delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}