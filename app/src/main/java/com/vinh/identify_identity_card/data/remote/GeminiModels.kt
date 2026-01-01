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
    val maxOutputTokens: Int? = 1024,
    val responseMimeType: String? = "application/json" // ✅ BẮT BUỘC
)

@Serializable
data class GeminiContent(
    val role: String = "user",
    val parts: List<GeminiPart>
)

//@Serializable
//sealed class GeminiPart {
//    @Serializable
//    data class TextPart(val text: String) : GeminiPart()
//
//    @Serializable
//    data class InlineDataPart(
//        @SerialName("inlineData") val inlineData: InlineData
//    ) : GeminiPart()
//
//    @Serializable
//    data class InlineData(
//        @SerialName("mimeType") val mimeType: String,
//        val data: String
//    )
//}
@Serializable
data class GeminiPart(
    val text: String? = null,
    @SerialName("inlineData") val inlineData: InlineData? = null
)

@Serializable
data class InlineData(
    @SerialName("mimeType") val mimeType: String,
    val data: String
)

@Serializable
data class GeminiGenerateResponse(
    val candidates: List<GeminiCandidate> = emptyList()
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null
)
