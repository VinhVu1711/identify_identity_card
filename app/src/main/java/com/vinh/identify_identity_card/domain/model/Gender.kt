package com.vinh.identify_identity_card.domain.model

enum class Gender {
    MALE,
    FEMALE,
    OTHER
}

fun Gender.toDisplayVi() : String = when(this){
    Gender.FEMALE -> "Nữ"
    Gender.MALE -> "Nam"
    Gender.OTHER -> "Khác"
}