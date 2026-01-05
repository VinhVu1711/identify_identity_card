package com.vinh.identify_identity_card.data.remote

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiClient(
    private val apiKey: String,
    private val model: String = "gemini-2.5-flash"
) {
    private val TAG = "GeminiClient"

    // ✅ tăng timeout để giảm lỗi timeout trên máy thật/mạng chậm
    private val http = OkHttpClient.Builder()
        .callTimeout(60, TimeUnit.SECONDS)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .eventListener(object : EventListener() {
            override fun callStart(call: Call) {
                Log.d("GeminiNet", "callStart: ${call.request().url}")
            }
            override fun dnsStart(call: Call, domainName: String) {
                Log.d("GeminiNet", "dnsStart: $domainName")
            }
            override fun connectStart(call: Call, inetSocketAddress: java.net.InetSocketAddress, proxy: java.net.Proxy) {
                Log.d("GeminiNet", "connectStart: $inetSocketAddress proxy=$proxy")
            }
            override fun secureConnectStart(call: Call) {
                Log.d("GeminiNet", "TLS start")
            }
            override fun secureConnectEnd(call: Call, handshake: Handshake?) {
                Log.d("GeminiNet", "TLS end: ${handshake?.tlsVersion}")
            }
            override fun callFailed(call: Call, ioe: IOException) {
                Log.e("GeminiNet", "callFailed: ${ioe.javaClass.simpleName} ${ioe.message}", ioe)
            }
            override fun callEnd(call: Call) {
                Log.d("GeminiNet", "callEnd")
            }
        })
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
        explicitNulls = false
    }

    /**
     * ✅ Nén ảnh về JPEG dưới maxSizeKB để tránh timeout/payload quá lớn
     * - Nếu decode fail -> fallback bytes gốc
     */
    fun compressImageToJpeg(file: File, maxSizeKB: Int = 1024): ByteArray {
        val bmp = BitmapFactory.decodeFile(file.absolutePath) ?: return file.readBytes()
        val out = ByteArrayOutputStream()
        var quality = 90

        do {
            out.reset()
            bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, out)
            quality -= 10
        } while (out.size() / 1024 > maxSizeKB && quality > 30)

        return out.toByteArray()
    }

    /**
     * ✅ PASSPORT: chỉ 1 ảnh
     */
    fun scan(prompt: String, imageFile: File, mimeType: String = "image/jpeg"): String {
        require(apiKey.isNotBlank()) { "API key is blank. Check local.properties -> GEMINI_API_KEY" }

        // ✅ rất nên nén để giảm timeout
        val bytes = compressImageToJpeg(imageFile, maxSizeKB = 900)
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

        val req = GeminiGenerateRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(
                        GeminiPart.text(prompt),
                        GeminiPart.inline("image/jpeg", base64)
                    )
                )
            ),
            generationConfig = GeminiGenerationConfig(temperature = 0.0, maxOutputTokens = 1024)
        )

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        val safeUrl = url.replace(Regex("key=[^&]+"), "key=***")
        val bodyString = json.encodeToString(GeminiGenerateRequest.serializer(), req)

        Log.d(TAG, "REQUEST -> $safeUrl")
        Log.d(TAG, "scan() apiKeyLen=${apiKey.length} model=$model mime=$mimeType origBytes=${imageFile.length()} jpegBytes=${bytes.size} base64Len=${base64.length}")
        Log.d(TAG, "REQUEST JSON first800:\n${bodyString.take(800)}")

        val request = Request.Builder()
            .url(url)
            .post(bodyString.toRequestBody("application/json".toMediaType()))
            .build()

        http.newCall(request).execute().use { resp ->
            val rawBody = resp.body?.string().orEmpty()
            Log.d(TAG, "HTTP ${resp.code} ${resp.message}")
            Log.d(TAG, "BODY first1200:\n${rawBody.take(1200)}")

            if (!resp.isSuccessful) {
                throw IllegalStateException("Gemini error ${resp.code}: ${resp.message}\n$rawBody")
            }

            val parsed = json.decodeFromString(GeminiGenerateResponse.serializer(), rawBody)
            val text = parsed.candidates.firstOrNull()
                ?.content?.parts
                ?.firstOrNull { it.text != null }
                ?.text
                .orEmpty()

            Log.d(TAG, "PARSED text first300=${text.take(300)}")
            return stripMarkdown(text)
        }
    }

    /**
     * ✅ CCCD: nhiều ảnh (front/back)
     */
    fun scanMulti(prompt: String, images: List<Pair<File, String>>): String {
        require(apiKey.isNotBlank()) { "API key is blank. Check local.properties -> GEMINI_API_KEY" }
        require(images.isNotEmpty()) { "images is empty" }

        val parts = mutableListOf<GeminiPart>()
        parts += GeminiPart.text(prompt)

        images.forEachIndexed { idx, (file, mime) ->
            // ✅ nén để giảm timeout/payload
            val bytes = compressImageToJpeg(file, maxSizeKB = 900)
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

            parts += GeminiPart.inline("image/jpeg", base64)

            Log.d(TAG, "scanMulti img[$idx] origBytes=${file.length()} mime=$mime jpegBytes=${bytes.size} base64Len=${base64.length}")
        }

        val req = GeminiGenerateRequest(
            contents = listOf(GeminiContent(role = "user", parts = parts)),
            generationConfig = GeminiGenerationConfig(temperature = 0.0, maxOutputTokens = 1024)
        )

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        val safeUrl = url.replace(Regex("key=[^&]+"), "key=***")
        val bodyString = json.encodeToString(GeminiGenerateRequest.serializer(), req)

        Log.d(TAG, "REQUEST -> $safeUrl")
        Log.d(TAG, "scanMulti() apiKeyLen=${apiKey.length} model=$model parts=${parts.size}")
        Log.d(TAG, "REQUEST JSON first800:\n${bodyString.take(800)}")

        val request = Request.Builder()
            .url(url)
            .post(bodyString.toRequestBody("application/json".toMediaType()))
            .build()

        http.newCall(request).execute().use { resp ->
            val rawBody = resp.body?.string().orEmpty()
            Log.d(TAG, "HTTP ${resp.code} ${resp.message}")
            Log.d(TAG, "BODY first1200:\n${rawBody.take(1200)}")

            if (!resp.isSuccessful) {
                throw IllegalStateException("Gemini error ${resp.code}: ${resp.message}\n$rawBody")
            }

            val parsed = json.decodeFromString(GeminiGenerateResponse.serializer(), rawBody)
            val text = parsed.candidates.firstOrNull()
                ?.content?.parts
                ?.firstOrNull { it.text != null }
                ?.text
                .orEmpty()

            Log.d(TAG, "PARSED text first300=${text.take(300)}")
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
