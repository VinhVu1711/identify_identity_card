package com.vinh.identify_identity_card.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeminiGenerateRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null
)

@Serializable
data class GeminiGenerationConfig(
    val temperature: Double? = 0.0,
    val maxOutputTokens: Int? = 1024
)

@Serializable
data class GeminiContent(
    val role: String = "user",
    val parts: List<GeminiPart>
)

/**
 * ✅ KHÔNG sealed class nữa để tránh kotlinx tự sinh "type"
 * Part hợp lệ của Gemini:
 * - { "text": "..." }
 * - { "inline_data": { "mime_type": "...", "data": "..." } }
 */
@Serializable
data class GeminiPart(
    val text: String? = null,

    @SerialName("inline_data")
    val inlineData: InlineData? = null
) {
    @Serializable
    data class InlineData(
        @SerialName("mime_type") val mimeType: String,
        val data: String
    )

    companion object {
        fun text(t: String) = GeminiPart(text = t)
        fun inline(mime: String, b64: String) =
            GeminiPart(inlineData = InlineData(mimeType = mime, data = b64))
    }
}

@Serializable
data class GeminiGenerateResponse(
    val candidates: List<GeminiCandidate> = emptyList()
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null
)
