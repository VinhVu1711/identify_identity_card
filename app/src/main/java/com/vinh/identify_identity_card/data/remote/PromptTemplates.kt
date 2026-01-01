package com.vinh.identify_identity_card.data.remote

import com.vinh.identify_identity_card.domain.model.DocumentType

object PromptTemplates {

    private fun rules() = """
You are an OCR system.
Extract information from the Vietnamese Citizen ID card image.

Return ONLY valid JSON.
Do NOT include markdown, explanation, or extra text.

Fields:
- idNumber
- fullName
- dateOfBirth (dd/MM/yyyy)
- gender (MALE | FEMALE | OTHER)
- nationality
- placeOfOrigin
- placeOfResidence
- issueDate (dd/MM/yyyy)
- expiryDate (dd/MM/yyyy)
- issuePlace

If a field is missing, set it to null.

""".trimIndent()

    fun prompt(docType: DocumentType): String = when (docType) {
        DocumentType.IDENTITYCARD -> """
${rules()}
Trích xuất CCCD Việt Nam từ ảnh. JSON schema:
{
  "idNumber": string|null,
  "fullName": string|null,
  "dateOfBirth": "dd/MM/yyyy"|null,
  "gender": "MALE"|"FEMALE"|"OTHER"|null,
  "nationality": string|null,
  "placeOfOrigin": string|null,
  "placeOfResidence": string|null,
  "expiryDate": "dd/MM/yyyy"|null,
  "issueDate": "dd/MM/yyyy"|null,
  "issuePlace": string|null
}
""".trimIndent()

        DocumentType.PASSPORT -> """
${rules()}
Trích xuất Hộ chiếu từ ảnh. JSON schema:
{
  "idNumber": string|null,
  "fullName": string|null,
  "dateOfBirth": "dd/MM/yyyy"|null,
  "gender": "MALE"|"FEMALE"|"OTHER"|null,
  "nationality": string|null,
  "passportNumber": string|null,
  "expiryDate": "dd/MM/yyyy"|null,
  "issueDate": "dd/MM/yyyy"|null,
  "issuingAuthority": string|null
}
""".trimIndent()
    }
}
