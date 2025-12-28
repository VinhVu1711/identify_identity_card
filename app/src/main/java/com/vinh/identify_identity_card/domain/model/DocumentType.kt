package com.vinh.identify_identity_card.domain.model

enum class DocumentType {
    IDENTITYCARD,
    PASSPORT,
}

fun DocumentType.toDisplayVi(): String = when(this){
    DocumentType.IDENTITYCARD -> "CCCD"
    DocumentType.PASSPORT -> "Hộ chiếu"
}