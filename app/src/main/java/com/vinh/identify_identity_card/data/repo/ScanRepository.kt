package com.vinh.identify_identity_card.data.repo

import com.vinh.identify_identity_card.data.remote.GeminiClient
import com.vinh.identify_identity_card.data.remote.PromptTemplates
import com.vinh.identify_identity_card.domain.model.DocumentType
import java.io.File
import android.util.Log
private const val TAG = "ScanRepo"

class ScanRepository(private val gemini: GeminiClient) {
//    fun scan(docType: DocumentType, file: File, mimeType: String): String {
//        Log.d(TAG, "scan() docType=$docType fileSize=${file.length()} path=${file.absolutePath}")
//        val prompt = PromptTemplates.prompt(docType)
//        Log.d(TAG, "promptLen=${prompt.length}")
//        return gemini.scan(prompt, file, mimeType)
//    }
fun scanCccdFrontBack(front: File, frontMime: String, back: File, backMime: String): String {

    Log.d("ScanRepo", "📥 scanCccdFrontBack")
    Log.d("ScanRepo", "Front: ${front.name} size=${front.length()} mime=$frontMime")
    Log.d("ScanRepo", "Back : ${back.name} size=${back.length()} mime=$backMime")

    val prompt = PromptTemplates.promptCccdFrontBack()

    return gemini.scanMulti(
        prompt,
        listOf(
            front to frontMime,
            back to backMime
        )
    )
}
    fun scanPassportOne(file: File, mime: String): String {
        val prompt = PromptTemplates.prompt(DocumentType.PASSPORT)
        return gemini.scanMulti(prompt, listOf(file to mime))
    }


}
