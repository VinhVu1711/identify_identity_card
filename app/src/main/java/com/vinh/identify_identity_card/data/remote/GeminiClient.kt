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
    private val http = OkHttpClient.Builder()
        .eventListener(object : okhttp3.EventListener() {
            override fun callStart(call: okhttp3.Call) {
                Log.d("GeminiNet", "callStart: ${call.request().url}")
            }
            override fun dnsStart(call: okhttp3.Call, domainName: String) {
                Log.d("GeminiNet", "dnsStart: $domainName")
            }
            override fun connectStart(call: okhttp3.Call, inetSocketAddress: java.net.InetSocketAddress, proxy: java.net.Proxy) {
                Log.d("GeminiNet", "connectStart: $inetSocketAddress proxy=$proxy")
            }
            override fun secureConnectStart(call: okhttp3.Call) {
                Log.d("GeminiNet", "TLS start")
            }
            override fun secureConnectEnd(call: okhttp3.Call, handshake: okhttp3.Handshake?) {
                Log.d("GeminiNet", "TLS end: ${handshake?.tlsVersion}")
            }
            override fun callFailed(call: okhttp3.Call, ioe: java.io.IOException) {
                Log.e("GeminiNet", "callFailed", ioe)
            }
            override fun callEnd(call: okhttp3.Call) {
                Log.d("GeminiNet", "callEnd")
            }
        })
        .build()

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

    fun scanMulti(prompt: String, images: List<Pair<File, String>>): String {
        val parts = mutableListOf<GeminiPart>()
        parts += GeminiPart.text(prompt)

        images.forEach { (file, mime) ->
            val base64 = Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
            parts += GeminiPart.inline(mime, base64)
        }

        val req = GeminiGenerateRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = parts
                )
            ),
            generationConfig = GeminiGenerationConfig(temperature = 0.0, maxOutputTokens = 1024)
        )

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        val bodyString = json.encodeToString(GeminiGenerateRequest.serializer(), req)

        Log.d(TAG, "REQUEST JSON first800:\n${bodyString.take(800)}")

        val request = Request.Builder()
            .url(url)
            .post(bodyString.toRequestBody("application/json".toMediaType()))
            .build()

        http.newCall(request).execute().use { resp ->
            val rawBody = resp.body?.string().orEmpty()
            Log.d(TAG, "HTTP ${resp.code} ${resp.message}")
            Log.d(TAG, "BODY first800:\n${rawBody.take(800)}")

            if (!resp.isSuccessful) {
                throw IllegalStateException("Gemini error ${resp.code}: ${resp.message}\n$rawBody")
            }

            val parsed = json.decodeFromString(GeminiGenerateResponse.serializer(), rawBody)

            val text = parsed.candidates.firstOrNull()
                ?.content?.parts
                ?.firstOrNull { it.text != null }
                ?.text
                .orEmpty()

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
