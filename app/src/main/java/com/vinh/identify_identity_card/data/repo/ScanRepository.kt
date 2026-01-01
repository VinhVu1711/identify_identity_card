package com.vinh.identify_identity_card.data.repo

import com.vinh.identify_identity_card.data.remote.GeminiClient
import com.vinh.identify_identity_card.data.remote.PromptTemplates
import com.vinh.identify_identity_card.domain.model.DocumentType
import java.io.File

class ScanRepository(private val gemini: GeminiClient) {
    fun scan(docType: DocumentType, file: File): String {
        val prompt = PromptTemplates.prompt(docType)
        return gemini.scan(prompt, file)
    }
}
