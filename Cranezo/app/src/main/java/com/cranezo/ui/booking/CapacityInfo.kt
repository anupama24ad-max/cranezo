package com.cranezo.ui.booking

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cranezo.R
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.Textview
import com.cranezo.ui.theme.*
import com.cranezo.utils.formatAmount

/**
 * Helps a customer feel what a capacity means: "16" becomes "16 Ton", each size has a plain-words class
 * (Light ... Extra heavy), and the selected size gets what it is used for and a weight they know.
 */
enum class CapacityClass(val label: Int, val use: Int) {
    LIGHT(R.string.cap_light, R.string.cap_use_light),
    MEDIUM(R.string.cap_medium, R.string.cap_use_medium),
    HEAVY(R.string.cap_heavy, R.string.cap_use_heavy),
    VERY_HEAVY(R.string.cap_very_heavy, R.string.cap_use_very_heavy),
    EXTRA_HEAVY(R.string.cap_extra_heavy, R.string.cap_use_extra_heavy)
}

object CapacityInfo {
    private val leadingNumber = Regex("""^\s*(\d+(?:\.\d+)?)""")
    private val plainNumber = Regex("""^\s*\d+(?:\.\d+)?\s*$""")

    /** The number in a capacity name ("16", "16 Ton" -> 16.0), or null when there is none */
    fun tons(name: String?): Double? = leadingNumber.find(name.orEmpty())?.groupValues?.get(1)?.toDoubleOrNull()

    /** A name that is only a number still needs its unit (names that already have text are left as they are) */
    fun needsUnit(name: String?): Boolean = plainNumber.matches(name.orEmpty())

    fun classOf(tons: Double): CapacityClass = when {
        tons < 15 -> CapacityClass.LIGHT
        tons < 40 -> CapacityClass.MEDIUM
        tons < 100 -> CapacityClass.HEAVY
        tons < 250 -> CapacityClass.VERY_HEAVY
        else -> CapacityClass.EXTRA_HEAVY
    }

    /** A small car weighs about 1.5 ton */
    fun smallCars(tons: Double): Int = Math.round(tons / 1.5).toInt().coerceAtLeast(1)

    fun withUnit(name: String?, unit: String): String =
        name.orEmpty().let { if (needsUnit(it)) "${it.trim()} $unit" else it }
}

/** "16" -> "16 Ton" (in the app's language) */
@Composable
fun capacityLabel(name: String?): String = CapacityInfo.withUnit(name, stringResource(R.string.unit_ton))

/** The selected size: what it is for and how heavy it is in everyday terms */
@Composable
fun CapacityHint(name: String?, modifier: Modifier = Modifier) {
    val tons = CapacityInfo.tons(name) ?: return
    val cls = CapacityInfo.classOf(tons)
    GlassCard(modifier = modifier, contentPadding = dimen_12, minHeight = dimen_40) {
        Textview(
            text = capacityLabel(name) + " · " + stringResource(cls.label),
            size = font_13, color = appColors.primaryText, fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(dimen_2))
        Textview(text = stringResource(cls.use), size = font_12, color = appColors.textPrimary)
        Spacer(modifier = Modifier.height(dimen_4))
        Textview(
            text = stringResource(R.string.cap_weight_hint, formatAmount(tons * 1000), CapacityInfo.smallCars(tons)),
            size = font_11, color = appColors.textSecondary
        )
        Textview(text = stringResource(R.string.cap_note), size = font_10, color = appColors.textHint)
    }
}
