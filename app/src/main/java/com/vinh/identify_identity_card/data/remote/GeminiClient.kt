package com.vinh.identify_identity_card.data.remote

import android.util.Base64
import android.util.Log
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class GeminiClient(
    private val apiKey: String,
    private val model: String = "gemini-2.5-flash"
) {
    private val http = OkHttpClient()
    private  val TAG = "GeminiClient"
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
        explicitNulls = false
    }
    fun compressImageToJpeg(file: File, maxSizeKB: Int = 1024): ByteArray {
        val bmp = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
        val out = java.io.ByteArrayOutputStream()
        var quality = 90

        do {
            out.reset()
            bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, out)
            quality -= 10
        } while (out.size() / 1024 > maxSizeKB && quality > 30)

        return out.toByteArray()
    }

    fun scan(prompt: String, imageFile: File, mimeType: String = "image/jpeg"): String {
        val bytes = compressImageToJpeg(imageFile, maxSizeKB = 1024)
//        val base64 = Base64.encodeToString(imageFile.readBytes(), Base64.NO_WRAP)
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        val req = GeminiGenerateRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(
                        GeminiPart(text = prompt),
                        GeminiPart(
                            inlineData = InlineData(
                                mimeType = mimeType,
                                data = base64
                            )
                        )
                    )
                )
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.0,
                maxOutputTokens = 1024
            )
        )



        val url =
            "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val body = json.encodeToString(GeminiGenerateRequest.serializer(), req)
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder().url(url).post(body).build()

//        http.newCall(request).execute().use { resp ->
//            if (!resp.isSuccessful) throw IllegalStateException("Gemini error ${resp.code}: ${resp.message}")
//            val raw = resp.body?.string().orEmpty()
//            val parsed = json.decodeFromString(GeminiGenerateResponse.serializer(), raw)
//
//            val text = parsed.candidates.firstOrNull()
//                ?.content?.parts
//                ?.firstOrNull { it is GeminiPart.TextPart }
//                ?.let { (it as GeminiPart.TextPart).text }
//                .orEmpty()
//
//            return stripMarkdown(text)
//        }


        http.newCall(request).execute().use { resp ->
            val rawBody = resp.body?.string().orEmpty()

            // ✅ safe URL (che key)
            val safeUrl = url.replace(Regex("key=[^&]+"), "key=***")

            // ✅ log meta
            Log.d(TAG, "➡️ REQUEST: $safeUrl")
            Log.d(TAG, "   model=$model mimeType=$mimeType imageBytes=${imageFile.length()} base64Len=${base64.length}")

            // ✅ log request JSON preview (nếu bạn có bodyString)
            // Log.d(TAG, "   requestJson=${bodyString.take(400)}")

            Log.d(TAG, "⬅️ RESPONSE: HTTP ${resp.code} ${resp.message}")

            if (!resp.isSuccessful) {
                // ✅ in body lỗi (cực quan trọng)
                Log.e(TAG, "❌ ERROR BODY (first 1500 chars):\n${rawBody.take(1500)}")
                throw IllegalStateException(
                    "Gemini error ${resp.code}: ${resp.message}\n${rawBody.take(3000)}"
                )
            }

            // ✅ body OK preview
            Log.d(TAG, "✅ OK BODY (first 600 chars):\n${rawBody.take(600)}")

            val parsed = json.decodeFromString(GeminiGenerateResponse.serializer(), rawBody)

            // ✅ extract text theo model mới (GeminiPart.text)
            val text = parsed.candidates.firstOrNull()
                ?.content?.parts
                ?.firstOrNull { !it.text.isNullOrBlank() }
                ?.text
                .orEmpty()

            if (text.isBlank()) {
                Log.w(TAG, "⚠️ No text found in response candidates.")
            }

            return stripMarkdown(text)
        }


    }

    private fun stripMarkdown(text: String): String {
        val t = text.trim()
        if (t.startsWith("```")) {
            return t.removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
        }
        return t
    }
}
