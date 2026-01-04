package com.vinh.identify_identity_card.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

private const val TAG = "FileUtils"

//Create temporary Image File, used for prepare where to save the picture before take a picture
fun createTempImageFile(context: Context): File {
    val dir = File(context.cacheDir, "scan_images").apply { mkdirs() }
    return File(dir, "IMG_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg")
}

//Dùng khi chụp ảnh và lưu ảnh vào file
fun createTempImageUri(context: Context, file: File): Uri {
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

//Chuyển URI thành File
fun uriToTempFile(context: Context, uri: Uri): File {
    val resolver = context.contentResolver
    val mime = resolver.getType(uri) ?: "image/jpeg"
    val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime) ?: "jpg"

    // ✅ Log kích thước ảnh (width x height) mà không decode full bitmap
    try {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, opts)
        }
        val w = opts.outWidth
        val h = opts.outHeight
        Log.d(TAG, "uriToTempFile: mime=$mime ext=$ext size=${w}x$h uri=$uri")
    } catch (e: Exception) {
        Log.w(TAG, "uriToTempFile: cannot read image bounds, uri=$uri", e)
    }

    val out = File(context.cacheDir, "upload_${System.currentTimeMillis()}.$ext")

    resolver.openInputStream(uri).use { input ->
        requireNotNull(input) { "Cannot open input stream" }
        FileOutputStream(out).use { output ->
            input.copyTo(output)
        }
    }

    Log.d(TAG, "uriToTempFile: saved to ${out.absolutePath}, bytes=${out.length()}")

    return out
}
