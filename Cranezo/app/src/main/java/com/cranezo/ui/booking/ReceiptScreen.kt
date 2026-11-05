package com.cranezo.ui.booking

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cranezo.R
import com.cranezo.ui.booking.components.BookingScaffold
import com.cranezo.ui.booking.model.ReceiptInfo
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.Textview
import com.cranezo.ui.mainhome.components.SuccessGreen
import com.cranezo.ui.theme.*
import com.cranezo.utils.formatServerDateTime
import com.cranezo.utils.rupees

/** The customer's receipt for a paid booking: who, what, every bill line, each payment, total paid. Can be shared. */
@Composable
fun ReceiptScreen(vm: BookingViewModel, onBack: () -> Unit) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val r = state.booking?.receipt
    val context = LocalContext.current
    BookingScaffold(
        onBack = onBack,
        title = stringResource(R.string.rc_title),
        subtitle = r?.receipt_no,
        bottom = {
            if (r != null) {
                val text = receiptText(r, context)
                CommonGradientButton(
                    text = stringResource(R.string.rc_share), leftIcon = Icons.Default.Share,
                    onClick = {
                        val send = Intent(Intent.ACTION_SEND).setType("text/plain")
                            .putExtra(Intent.EXTRA_SUBJECT, r.receipt_no.orEmpty())
                            .putExtra(Intent.EXTRA_TEXT, text)
                        runCatching { context.startActivity(Intent.createChooser(send, null)) }
                    }
                )
            }
        }
    ) {
        if (r == null) {
            Textview(stringResource(R.string.rc_not_ready), size = font_14, color = appColors.textSecondary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = dimen_40))
            return@BookingScaffold
        }
        // header: total paid with a PAID stamp
        GlassCard(contentPadding = dimen_16) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Textview("Cranezo", size = font_16, color = AmberPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(dimen_6))
                Textview(stringResource(R.string.rc_total_paid), size = font_12, color = appColors.textSecondary)
                Textview(rupees(r.total_paid), size = font_26, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(dimen_6))
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(dimen_8)).background(SuccessGreen.copy(alpha = 0.15f))
                        .padding(horizontal = dimen_12, vertical = dimen_4)
                ) { Textview(stringResource(R.string.rc_paid_full), size = font_12, color = SuccessGreen, fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.height(dimen_8))
                Textview(stringResource(R.string.rc_issued, formatServerDateTime(r.issued_at ?: r.paid_at)), size = font_11, color = appColors.textSecondary)
            }
        }
        // parties
        GlassCard(contentPadding = dimen_14) {
            Row(horizontalArrangement = Arrangement.spacedBy(dimen_12)) {
                Party(stringResource(R.string.rc_billed_to), r.billed_to?.name, r.billed_to?.phone, null, Modifier.weight(1f))
                Party(stringResource(R.string.rc_billed_by), r.billed_by?.name, r.billed_by?.phone, r.billed_by?.gst_number, Modifier.weight(1f))
            }
        }
        // the job
        r.service?.let { s ->
            Section(stringResource(R.string.rc_service)) {
                Line(stringResource(R.string.rc_booking), r.booking_code)
                Line(stringResource(R.string.rc_crane), listOfNotNull(s.crane, s.registration_number).joinToString(" · "))
                Line(stringResource(R.string.rc_site), s.site)
                Line(stringResource(R.string.rc_started), formatServerDateTime(s.started_at))
                Line(stringResource(R.string.rc_completed), formatServerDateTime(s.completed_at))
                Line(stringResource(R.string.rc_booked_hours), s.booked_hours?.let { com.cranezo.ui.finance.trimNumber(it) })
                s.worked_seconds?.let { sec -> Line(stringResource(R.string.rc_worked), hoursMinutes(sec)) }
                if ((s.extra_minutes ?: 0) > 0) Line(stringResource(R.string.rc_extra), "${s.extra_minutes} min")
                Line(stringResource(R.string.rc_operator), s.operator)
            }
        }
        // the bill
        Section(stringResource(R.string.rc_bill)) {
            val lines = r.lines.orEmpty().filter { it.key != "platform_fee" && it.key != "owner_receives" && it.key != "operator_commission" }
            if (lines.isEmpty()) {
                Amount(stringResource(R.string.rc_total), r.total_amount, bold = true)
            } else {
                lines.forEach { l -> Amount(l.label.orEmpty(), l.amount, bold = l.total == true) }
            }
        }
        // the payments
        Section(stringResource(R.string.rc_payments)) {
            r.payments.orEmpty().forEach { p ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = dimen_4), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(p.label.orEmpty(), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
                        Textview(listOfNotNull(p.method, formatServerDateTime(p.paid_at)).joinToString(" · "), size = font_11, color = appColors.textSecondary)
                        p.reference?.let { Textview(it, size = font_10, color = appColors.textHint) }
                    }
                    Textview(rupees(p.amount), size = font_14, color = SuccessGreen, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(dimen_4))
            Amount(stringResource(R.string.rc_total_paid), r.total_paid, bold = true)
        }
        Textview(stringResource(R.string.rc_footer), size = font_11, color = appColors.textHint, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(dimen_16))
    }
}

@Composable
private fun Party(title: String, name: String?, phone: String?, gst: String?, modifier: Modifier) {
    Column(modifier = modifier) {
        Textview(title, size = font_11, color = appColors.textSecondary)
        Textview(name ?: "-", size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
        phone?.takeIf { it.isNotBlank() }?.let { Textview(it, size = font_11, color = appColors.textSecondary) }
        gst?.let { Textview("GSTIN $it", size = font_10, color = appColors.textSecondary) }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    GlassCard(contentPadding = dimen_14) {
        Textview(title, size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(dimen_10))
        content()
    }
}

@Composable
private fun Line(label: String, value: String?) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = dimen_3)) {
        Textview(label, size = font_12, color = appColors.textSecondary, modifier = Modifier.weight(0.4f))
        Textview(value?.takeIf { it.isNotBlank() } ?: "-", size = font_12, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End, modifier = Modifier.weight(0.6f))
    }
}

@Composable
private fun Amount(label: String, amount: Double?, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = dimen_3), verticalAlignment = Alignment.CenterVertically) {
        Textview(label, size = if (bold) font_14 else font_13, color = if (bold) appColors.textPrimary else appColors.textSecondary, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
        Textview(rupees(amount), size = if (bold) font_15 else font_13, color = appColors.textPrimary, fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold)
    }
}

private fun hoursMinutes(seconds: Long): String {
    val m = seconds / 60
    return if (m >= 60) "${m / 60} h ${m % 60} min" else "$m min"
}

/** Plain-text copy for sharing (WhatsApp, email) */
private fun receiptText(r: ReceiptInfo, context: android.content.Context): String = buildString {
    appendLine("Cranezo - ${context.getString(R.string.rc_title)} ${r.receipt_no.orEmpty()}")
    appendLine("${context.getString(R.string.rc_booking)}: ${r.booking_code.orEmpty()}")
    r.service?.crane?.let { appendLine("${context.getString(R.string.rc_crane)}: $it") }
    r.service?.site?.let { appendLine("${context.getString(R.string.rc_site)}: $it") }
    appendLine()
    r.lines.orEmpty().filter { it.key != "platform_fee" && it.key != "owner_receives" && it.key != "operator_commission" }
        .forEach { appendLine("${it.label}: ${rupees(it.amount)}") }
    appendLine()
    r.payments.orEmpty().forEach { appendLine("${it.label} (${it.method.orEmpty()}): ${rupees(it.amount)}  ${it.reference.orEmpty()}") }
    appendLine("${context.getString(R.string.rc_total_paid)}: ${rupees(r.total_paid)}")
}
