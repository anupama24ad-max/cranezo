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

private fun serverDate(iso: String?): Date? = parseServerTime(iso)

/** Day of the month for the date block of a job card, e.g. "10". */
fun serverDay(iso: String?): String =
    serverDate(iso)?.let { SimpleDateFormat("dd", Locale.getDefault()).format(it) }.orEmpty()

/** "SEP" over "2026" for the date block of a job card. */
fun serverMonth(iso: String?): String =
    serverDate(iso)?.let { SimpleDateFormat("MMM", Locale.getDefault()).format(it).uppercase() }.orEmpty()

fun serverYear(iso: String?): String =
    serverDate(iso)?.let { SimpleDateFormat("yyyy", Locale.getDefault()).format(it) }.orEmpty()

/** "05 Sep 2026" from a server time. */
fun formatServerDate(iso: String?): String =
    serverDate(iso)?.let { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it) } ?: iso.orEmpty()

/** 28800 -> "8h 00m". */
fun formatSeconds(seconds: Long?): String {
    val total = (seconds ?: 0L).coerceAtLeast(0)
    return "${total / 3600}h %02dm".format((total % 3600) / 60)
}

/** 9342 -> "02:35:42". */
fun formatClock(seconds: Long): String {
    val total = seconds.coerceAtLeast(0)
    return "%02d:%02d:%02d".format(total / 3600, (total % 3600) / 60, total % 60)
}

/** 135 -> "2h 15m", 45 -> "45m", 120 -> "2h", 3000 -> "2d 2h". */
fun compactMinutes(minutes: Int?): String {
    val m = (minutes ?: 0).coerceAtLeast(0)
    return when {
        m < 60 -> "${m}m"
        m < 1440 -> if (m % 60 == 0) "${m / 60}h" else "${m / 60}h ${m % 60}m"
        else -> if ((m % 1440) / 60 == 0) "${m / 1440}d" else "${m / 1440}d ${(m % 1440) / 60}h"
    }
}

/** "September" from a server time. */
fun serverMonthName(iso: String?): String =
    serverDate(iso)?.let { SimpleDateFormat("MMMM", Locale.getDefault()).format(it) }.orEmpty()
