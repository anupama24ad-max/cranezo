package com.cranezo.utils

import android.net.Uri
import com.cranezo.BuildConfig
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The APIs return file paths like "/public/uploads/abc.png", relative to the server root (not /api/).
 * Returns the full url to display or open, or null when there is no file.
 */
fun fileUrl(path: String?): String? {
    if (path.isNullOrBlank()) return null
    if (path.startsWith("http")) return path
    val server = Uri.parse(BuildConfig.SERVER_URL)
    return "${server.scheme}://${server.authority}${if (path.startsWith("/")) "" else "/"}$path"
}

/** 5000.0 -> "5,000" (Indian grouping, no needless decimals). */
fun formatAmount(value: Double?): String {
    if (value == null) return "-"
    return NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }.format(value)
}

private fun parseDate(value: String?): Date? {
    if (value.isNullOrBlank()) return null
    // "2027-08-12" or an ISO timestamp such as "2027-08-12T10:00:00.000Z"
    return runCatching {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(value.take(10))
    }.getOrNull()
}

/** "2027-08-12" -> "12 Aug 2027". Anything else is shown as it is. */
fun formatDisplayDate(value: String?): String {
    val date = parseDate(value) ?: return value.orEmpty()
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
}

/** True when the yyyy-MM-dd date is before today. */
fun isDateExpired(value: String?): Boolean {
    val date = parseDate(value) ?: return false
    val today = parseDate(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) ?: return false
    return date.before(today)
}
