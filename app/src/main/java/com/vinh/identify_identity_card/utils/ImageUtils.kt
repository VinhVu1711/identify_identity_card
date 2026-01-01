package com.vinh.identify_identity_card.utils

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

//Create temporary Image File, used for prepare where to save the picture before take a picture
fun createTempImageFile(context: Context): File {
    //create a sub folder scan_images, nếu chưa có thì mkdirs để tạo
    val dir = File(context.cacheDir, "scan_images").apply { mkdirs() }
    //Tạo 1 file ảnh mới với tên không trùng, trả về file(CHƯA CÓ DỮ LIỆU CHỈ LÀ ĐƯỜNG DẪN)
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
    //Công cụ đo dữ liệu
    val resolver = context.contentResolver
    //Lấy MIME type
    val mime = resolver.getType(uri) ?: "image/jpeg"
    //Dựa vào MIME lấy ra đuôi jpg
    val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime) ?: "jpg"

    //Tạo file output cho cache
    val out = File(context.cacheDir, "upload_${System.currentTimeMillis()}.$ext")
    //Mở luồng đọc dữ liệu ảnh từ Uri
    resolver.openInputStream(uri).use { input ->
        requireNotNull(input) { "Cannot open input stream" }
        FileOutputStream(out).use { output -> input.copyTo(output) }//Copy toàn bộ dữ liệu qua file mới
    }
    return out
}
