package com.vinh.identify_identity_card.data.remote

import com.vinh.identify_identity_card.domain.model.DocumentType

object PromptTemplates {

    private fun rules() = """
You are a strict OCR engine for Vietnamese IDs.
Read all visible text from the image and fill the JSON.

Return ONLY JSON, no markdown.

If the image is not an ID card OR text is unreadable, return:
{"error":"UNREADABLE_OR_NOT_ID"}

Otherwise, you MUST extract as many fields as possible.
Do not return all fields null.
Use best effort even if some fields are uncertain.

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

    fun promptCccdFrontBack(): String = """
You are a strict OCR engine for Vietnamese Citizen ID (CCCD).

You will receive TWO images in this order:
1) Front side of CCCD
2) Back side of CCCD

Extract information using BOTH sides.
IssuePlace is under issueDate start with "CỤC TRƯỞNG ..."

Return ONLY valid JSON. No markdown. No explanation.

Schema:
{
  "idNumber": string|null,
  "fullName": string|null,
  "dateOfBirth": "dd/MM/yyyy"|null,
  "gender": "MALE"|"FEMALE"|"OTHER"|null,
  "nationality": string|null,
  "placeOfOrigin": string|null,
  "placeOfResidence": string|null,
  "issueDate": "dd/MM/yyyy"|null,
  "expiryDate": "dd/MM/yyyy"|null,
  "issuePlace": string|null
}

Rules:
- Prefer values printed on the card.
- If a field is missing, set null.
""".trimIndent()

}
