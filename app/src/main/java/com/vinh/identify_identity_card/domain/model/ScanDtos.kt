package com.vinh.identify_identity_card.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class IdentityCardScanDto(
    val idNumber: String? = null,
    val fullName: String? = null,
    val dateOfBirth: String? = null,
    val gender: Gender? = null,
    val nationality: String? = null,
    val placeOfOrigin: String? = null,
    val placeOfResidence: String? = null,
    val expiryDate: String? = null,
    val issueDate: String? = null,
    val issuePlace: String? = null
)

@Serializable
data class PassportScanDto(
    val idNumber: String? = null,
    val fullName: String? = null,
    val dateOfBirth: String? = null,
    val gender: Gender? = null,
    val nationality: String? = null,
    val passportNumber: String? = null,
    val expiryDate: String? = null,
    val issueDate: String? = null,
    val issuingAuthority: String? = null
)
