package com.vinh.identify_identity_card.domain.model

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class IdentityCardInfo(
    val idNumber: String? = null,//số căn cước công dân
    val fullName: String? = null,// họ và tên
    val dateOfBirth: Long? = null,//ngày sinh
    val gender: Gender? = null,//giới tính
    val nationality: String? = null,//quốc tịch
    val placeOfOrigin: String? = null,//nơi sinh
    val placeOfResidence: String? = null,////nơi cư trú
    val expiryDate: Long? = null,//ngày hết hạn
    val issueDate: Long? = null,//ngày cấp
    val issuePlace: String? = null//nơi cấp
)
