package com.vinh.identify_identity_card.utils

import java.text.SimpleDateFormat
import java.util.Locale

private val parseSdf = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))

fun parseDdMmYyyyToMillis(s: String?): Long? {
    if (s.isNullOrBlank()) return null
    return runCatching { parseSdf.parse(s.trim())?.time }.getOrNull()
}
