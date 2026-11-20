package com.cranezo.ui.operator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cranezo.R
import com.cranezo.ui.booking.components.BookingScaffold
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.Textview
import com.cranezo.ui.theme.*

private val SAFETY_LINES = listOf(
    R.string.safety_1, R.string.safety_2, R.string.safety_3, R.string.safety_4, R.string.safety_5,
    R.string.safety_6, R.string.safety_7, R.string.safety_8, R.string.safety_9
)

/** The safety rules and what the cards mean, in the operator's language. Shown once (until read) and from the profile. */
@Composable
fun SafetyRulesPage(onDone: () -> Unit, onBack: () -> Unit) {
    BookingScaffold(
        onBack = onBack,
        title = stringResource(R.string.safety_title),
        bottom = { CommonGradientButton(text = stringResource(R.string.safety_read_btn), onClick = onDone) }
    ) {
        Textview(stringResource(R.string.safety_intro), size = font_14, color = appColors.textSecondary)
        GlassCard(contentPadding = dimen_14) {
            SAFETY_LINES.forEachIndexed { i, res ->
                Textview("${i + 1}. " + stringResource(res), size = font_14, color = appColors.textPrimary, modifier = androidx.compose.ui.Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(dimen_8))
            }
        }
        GlassCard(contentPadding = dimen_14, borderColor = AmberPrimary) {
            Textview(stringResource(R.string.cards_title), size = font_15, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(dimen_6))
            Textview(stringResource(R.string.cards_text), size = font_13, color = appColors.textSecondary)
        }
        Spacer(modifier = Modifier.height(dimen_8))
    }
}

/** A short reminder (the first points) shown before the operator starts a job */
@Composable
fun SafetyReminderCard() {
    GlassCard(contentPadding = dimen_14, borderColor = AmberPrimary) {
        Textview(stringResource(R.string.safety_before_job), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(dimen_6))
        listOf(R.string.safety_2, R.string.safety_3, R.string.safety_4, R.string.safety_6).forEach {
            Textview("• " + stringResource(it), size = font_12, color = appColors.textSecondary)
        }
    }
}
