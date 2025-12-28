package com.vinh.identify_identity_card.domain.model

import kotlinx.serialization.Serializable


@Serializable
data class PassportInfo(
    val idNumber: String? = null,
    val fullName: String? = null,
    val dateOfBirth: Long? = null,
    val gender: Gender? = null,
    val nationality: String? = null,
    val passportNumber: String? = null,
    val expiryDate: Long? = null,
    val issueDate: Long? = null,
    val issuingAuthority: String? = null
)
