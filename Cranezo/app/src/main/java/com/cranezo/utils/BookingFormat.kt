package com.cranezo.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** 32000.0 -> "₹32,000". null -> "-". */
fun rupees(value: Double?): String = if (value == null) "-" else "₹${formatAmount(value)}"

private fun parseServerTime(iso: String?): Date? {
    if (iso.isNullOrBlank()) return null
    val patterns = listOf("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'")
    for (pattern in patterns) {
        val date = runCatching {
            SimpleDateFormat(pattern, Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.parse(iso)
        }.getOrNull()
        if (date != null) return date
    }
    return null
}

/** Server UTC time -> phone time zone, e.g. "05 Sep 2026, 09:00 AM". */
fun formatServerDateTime(iso: String?): String {
    val date = parseServerTime(iso) ?: return iso.orEmpty()
    return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(date)
}

/** Server UTC time -> phone time zone, time only, e.g. "09:18 AM". */
fun formatServerTime(iso: String?): String {
    val date = parseServerTime(iso) ?: return iso.orEmpty()
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
}

/** "2026-09-05" + "09:00" -> "2026-09-05T09:00:00+05:30" (the phone's time zone offset). */
fun toIsoWithOffset(date: String, time: String): String {
    val local = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).parse("$date $time") ?: Date()
    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(local)
}
