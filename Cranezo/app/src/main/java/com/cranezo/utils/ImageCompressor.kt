package com.cranezo.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.File
import java.io.FileOutputStream

/**
 * Every picture is shrunk before it is uploaded: at most 1600 px on the long side and at most 1 MB, as a JPEG.
 * A 3 to 10 MB camera picture ends up around 0.3 to 0.9 MB, which uploads fast on a mobile network and still
 * shows every detail of a crane, a plate or a document. A picture that is already small enough is sent as it
 * is. PDFs are not touched.
 */
object ImageCompressor {
    const val MAX_BYTES = 1L * 1024 * 1024
    private const val MAX_SIDE = 1600

    /** Returns the file to upload: the same file when it is already small, or a new smaller JPEG */
    fun shrink(context: Context, source: File): File {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(source.path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return source // not a picture we can read
        val longest = maxOf(bounds.outWidth, bounds.outHeight)
        if (source.length() <= MAX_BYTES && longest <= MAX_SIDE) return source

        // read it at a size close to Full HD (memory friendly), then scale exactly
        var sample = 1
        while (longest / (sample * 2) >= MAX_SIDE) sample *= 2
        val decoded = BitmapFactory.decodeFile(source.path, BitmapFactory.Options().apply { inSampleSize = sample }) ?: return source
        var bitmap = turnUpright(source, decoded)
        val scale = MAX_SIDE.toFloat() / maxOf(bitmap.width, bitmap.height)
        if (scale < 1f) {
            bitmap = Bitmap.createScaledBitmap(
                bitmap, (bitmap.width * scale).toInt().coerceAtLeast(1), (bitmap.height * scale).toInt().coerceAtLeast(1), true
            )
        }
        bitmap = onWhite(bitmap) // transparent parts of a PNG become white in a JPEG

        val out = File.createTempFile("upload_" + System.currentTimeMillis(), ".jpg", context.cacheDir)
        android.util.Log.d("ImageCompressor", "${source.length() / 1024} KB, ${bounds.outWidth}x${bounds.outHeight} -> shrinking")
        var current = bitmap
        repeat(6) {
            for (quality in intArrayOf(85, 75, 65, 55, 45)) {
                FileOutputStream(out).use { current.compress(Bitmap.CompressFormat.JPEG, quality, it) }
                if (out.length() <= MAX_BYTES) {
                    android.util.Log.d("ImageCompressor", "done: ${out.length() / 1024} KB at quality $quality")
                    source.delete()
                    return out
                }
            }
            // still too big: a smaller picture, then try the qualities again
            current = Bitmap.createScaledBitmap(
                current, (current.width * 0.8f).toInt().coerceAtLeast(1), (current.height * 0.8f).toInt().coerceAtLeast(1), true
            )
        }
        return out
    }

    private fun turnUpright(file: File, bitmap: Bitmap): Bitmap {
        val degrees = runCatching {
            when (ExifInterface(file.path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        }.getOrDefault(0f)
        if (degrees == 0f) return bitmap
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(degrees) }, true)
    }

    private fun onWhite(bitmap: Bitmap): Bitmap {
        if (!bitmap.hasAlpha()) return bitmap
        val flat = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        Canvas(flat).apply {
            drawColor(Color.WHITE)
            drawBitmap(bitmap, 0f, 0f, null)
        }
        return flat
    }
}
