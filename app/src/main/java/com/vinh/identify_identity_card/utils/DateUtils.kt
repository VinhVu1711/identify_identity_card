package com.vinh.identify_identity_card.utils

import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date

private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))

fun Long.toDdMmYyyy(): String =sdf.format(Date(this))

fun normalizeUpper(v: String?): String? =
    v?.trim()?.takeIf { it.isNotBlank() }?.uppercase()