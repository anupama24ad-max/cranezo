package com.cranezo.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cranezo.R
import com.cranezo.ui.booking.components.BookingScaffold
import com.cranezo.ui.booking.components.BookingTag
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.GlassInputField
import com.cranezo.ui.components.Textview
import com.cranezo.ui.finance.model.BillLine
import com.cranezo.ui.finance.model.Charge
import com.cranezo.ui.finance.model.IdName
import com.cranezo.ui.finance.model.SettlementItem
import com.cranezo.ui.finance.model.TransactionItem
import com.cranezo.ui.theme.*
import com.cranezo.utils.formatServerDate
import com.cranezo.utils.formatServerDateTime
import com.cranezo.utils.rupees
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import com.cranezo.ui.mainhome.components.IconBadge

// =============================================================================================
// Shared page pieces: plain sections, label/amount rows, chips and inline notices. No popups.
// =============================================================================================

val FinGreen = Color(0xFF22C55E)
val FinRed = Color(0xFFEF4444)
val FinBlue = Color(0xFF3B82F6)

fun amountText(v: Double?): String = rupees(v)

@Composable
fun FinPage(title: String, onBack: () -> Unit, subtitle: String? = null, content: @Composable ColumnScope.() -> Unit) {
    BookingScaffold(onBack = onBack, title = title, subtitle = subtitle, content = content)
}

@Composable
fun FinSection(title: String? = null, content: @Composable ColumnScope.() -> Unit) {
    GlassCard(contentPadding = dimen_14, minHeight = dimen_40) {
        if (title != null) {
            Textview(title, size = font_15, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(dimen_12))
        }
        content()
    }
}

/** One "label ......... ₹amount" line. Totals are bold; negative amounts are deductions. */
@Composable
fun MoneyRow(label: String, amount: Double?, bold: Boolean = false, color: Color? = null) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = dimen_4), verticalAlignment = Alignment.CenterVertically) {
        Textview(
            label, size = if (bold) font_15 else font_14,
            color = if (bold) appColors.textPrimary else appColors.textSecondary,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        val negative = (amount ?: 0.0) < 0
        Textview(
            if (negative) "- ${amountText(-(amount ?: 0.0))}" else amountText(amount),
            size = if (bold) font_16 else font_14,
            color = color ?: if (negative) FinRed else appColors.textPrimary,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}

@Composable
fun TextRow(label: String, value: String?) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = dimen_3)) {
        Textview(label, size = font_13, color = appColors.textSecondary, modifier = Modifier.weight(1f))
        Textview(value?.takeIf { it.isNotBlank() } ?: "-", size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun BillLines(lines: List<BillLine>) {
    lines.forEach { l ->
        if (l.total == true) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = dimen_4).height(dimen_1).background(appColors.subtleBorder))
        }
        MoneyRow(l.label.orEmpty(), l.amount, bold = l.total == true)
    }
}

/** Big tappable choices, one per row option. Easier than a dropdown. */
@Composable
fun ChoiceChips(options: List<IdName>, selected: String?, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(dimen_8)) {
        options.forEach { o ->
            val on = o.id == selected
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(dimen_12))
                    .background(if (on) AmberPrimary.copy(alpha = 0.18f) else appColors.subtleFill)
                    .clickable { o.id?.let(onSelect) }
                    .padding(horizontal = dimen_14, vertical = dimen_12),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(dimen_18).clip(RoundedCornerShape(dimen_10))
                        .background(if (on) AmberPrimary else Color.Transparent)
                        .padding(dimen_2)
                )
                Spacer(modifier = Modifier.width(dimen_10))
                Textview(o.name.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = if (on) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

@Composable
fun Notice(text: String?) {
    if (text.isNullOrBlank()) return
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(dimen_12))
            .background(FinGreen.copy(alpha = 0.15f)).padding(dimen_12)
    ) { Textview(text, size = font_14, color = FinGreen, fontWeight = FontWeight.SemiBold) }
}

@Composable
fun EmptyText(text: String) {
    Textview(
        text, size = font_14, color = appColors.textSecondary, textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = dimen_32)
    )
}

@Composable
fun FinMenuRow(icon: ImageVector, title: String, subtitle: String? = null, badge: String? = null, onClick: () -> Unit) {
    GlassCard(contentPadding = dimen_14, minHeight = dimen_64, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(dimen_40).clip(RoundedCornerShape(dimen_12)).background(AmberPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(dimen_22)) }
            Spacer(modifier = Modifier.width(dimen_12))
            Column(modifier = Modifier.weight(1f)) {
                Textview(title, size = font_15, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                if (subtitle != null) Textview(subtitle, size = font_12, color = appColors.textSecondary)
            }
            if (badge != null) BookingTag(badge, AmberPrimary)
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = appColors.textHint)
        }
    }
}

fun statusTone(status: String?): Color = when (status) {
    "success", "settled", "resolved", "accepted" -> FinGreen
    "failed", "open", "rejected" -> FinRed
    else -> AmberPrimary
}

@Composable
fun SettlementRow(s: SettlementItem) {
    GlassCard(contentPadding = dimen_14, minHeight = dimen_64) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon = Icons.Filled.CheckCircle, accent = FinGreen, size = dimen_40)
            Spacer(modifier = Modifier.width(dimen_12))
            Column(modifier = Modifier.weight(1f)) {
                Textview(s.direction_label.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold, maxLines = 2)
                Textview(
                    listOfNotNull(formatServerDate(s.settled_at), s.method_label, s.reference).joinToString(" · "),
                    size = font_12, color = appColors.textSecondary, maxLines = 1
                )
                Textview(stringResource(R.string.fin_jobs_count, s.jobs_count ?: 0), size = font_11, color = appColors.textHint)
            }
            Spacer(modifier = Modifier.width(dimen_10))
            Textview(amountText(s.amount), size = font_16, color = appColors.textPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
fun TransactionRow(t: TransactionItem, onOpen: ((TransactionItem) -> Unit)? = null) {
    GlassCard(contentPadding = dimen_14, minHeight = dimen_64, onClick = onOpen?.let { open -> { open(t) } }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // the round mark on the left says at a glance: paid, failed or waiting
            IconBadge(
                icon = when (t.status) { "success" -> Icons.Filled.CheckCircle; "failed" -> Icons.Filled.Close; else -> Icons.Filled.Schedule },
                accent = statusTone(t.status), size = dimen_40
            )
            Spacer(modifier = Modifier.width(dimen_12))
            Column(modifier = Modifier.weight(1f)) {
                Textview(t.booking_code ?: t.transaction_code.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Textview(listOfNotNull(t.method_name, formatServerDateTime(t.created_at)).joinToString(" · "), size = font_12, color = appColors.textSecondary, maxLines = 1)
                Textview(t.transaction_code.orEmpty(), size = font_11, color = appColors.textHint, maxLines = 1)
            }
            Spacer(modifier = Modifier.width(dimen_10))
            // amount and status share the same right edge
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(dimen_4)) {
                Textview(amountText(t.amount), size = font_16, color = appColors.textPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
                BookingTag(t.status_label.orEmpty(), statusTone(t.status))
            }
        }
    }
}

// =============================================================================================
// Owner: GST, additional charges and the default operator commission
// =============================================================================================

@Composable
fun OwnerBillingScreen(vm: FinanceViewModel, onBack: () -> Unit) {
    val state by vm.ui.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.loadBilling() }
    val b = state.billing

    var gst by rememberSaveable { mutableStateOf("") }
    var gstNumber by rememberSaveable { mutableStateOf("") }
    var commissionType by rememberSaveable { mutableStateOf("percent") }
    var commissionValue by rememberSaveable { mutableStateOf("") }
    var advance by rememberSaveable { mutableStateOf("") }
    var perKm by rememberSaveable { mutableStateOf("") }
    val charges = remember { mutableStateListOf<Charge>() }
    var filled by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(b) {
        if (b != null && !filled) {
            advance = b.advance_percent?.toString().orEmpty()
            perKm = b.per_km_rate?.let { trimNumber(it) }.orEmpty()
            gst = b.gst_percent?.let { trimNumber(it) }.orEmpty()
            gstNumber = b.gst_number.orEmpty()
            commissionType = b.default_commission_type?.takeIf { it != "per_km" } ?: "percent"
            commissionValue = b.default_commission_value?.let { trimNumber(it) }.orEmpty()
            charges.clear()
            charges.addAll(b.charges.orEmpty())
            filled = true
        }
    }

    FinPage(stringResource(R.string.fin_billing_title), onBack, subtitle = stringResource(R.string.fin_billing_subtitle)) {
        Notice(state.notice)
        // what the customer pays online when you accept a request, and your charge per km after the free distance
        FinSection(stringResource(R.string.rq_advance_and_distance)) {
            GlassInputField(
                label = stringResource(R.string.rq_advance_percent_label), value = advance,
                onValueChange = { advance = it.filter { c -> c.isDigit() }.take(3) }, placeholder = "20", keyboardType = KeyboardType.Number
            )
            Spacer(modifier = Modifier.height(dimen_10))
            GlassInputField(
                label = stringResource(R.string.rq_per_km_label), value = perKm,
                onValueChange = { perKm = it.filter { c -> c.isDigit() || c == '.' } }, placeholder = "50", keyboardType = KeyboardType.Decimal
            )
            Textview(stringResource(R.string.rq_advance_hint), size = font_12, color = appColors.textSecondary, modifier = Modifier.padding(top = dimen_6))
        }
        // GST and additional charges: hidden for now, the admin sets them (they keep the values already saved).
        // To let owners edit them again, remove the comment marks around this block.
        /*
        FinSection(stringResource(R.string.fin_gst)) {
            GlassInputField(
                label = stringResource(R.string.fin_gst_percent), value = gst, onValueChange = { gst = it.filter { c -> c.isDigit() || c == '.' } },
                placeholder = "18", keyboardType = KeyboardType.Decimal
            )
            Spacer(modifier = Modifier.height(dimen_10))
            GlassInputField(label = stringResource(R.string.fin_gst_number), value = gstNumber, onValueChange = { gstNumber = it.uppercase() }, placeholder = "22AAAAA0000A1Z5")
            Textview(stringResource(R.string.fin_gst_hint), size = font_12, color = appColors.textSecondary, modifier = Modifier.padding(top = dimen_6))
        }

        FinSection(stringResource(R.string.fin_charges)) {
            Textview(stringResource(R.string.fin_charges_hint), size = font_12, color = appColors.textSecondary)
            charges.forEachIndexed { i, c ->
                Spacer(modifier = Modifier.height(dimen_10))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Textview(stringResource(R.string.fin_charge_n, i + 1), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Filled.Close, contentDescription = stringResource(R.string.fin_remove), tint = FinRed,
                        modifier = Modifier.size(dimen_28).clickable { charges.removeAt(i) }
                    )
                }
                GlassInputField(label = stringResource(R.string.fin_charge_name), value = c.name.orEmpty(), onValueChange = { charges[i] = c.copy(name = it) }, placeholder = stringResource(R.string.fin_charge_name_hint))
                Spacer(modifier = Modifier.height(dimen_8))
                ChoiceChips(
                    listOf(IdName("fixed", stringResource(R.string.fin_type_fixed)), IdName("percent", stringResource(R.string.fin_type_percent))),
                    c.type ?: "fixed"
                ) { charges[i] = c.copy(type = it) }
                Spacer(modifier = Modifier.height(dimen_8))
                GlassInputField(
                    label = if (c.type == "percent") stringResource(R.string.fin_value_percent) else stringResource(R.string.fin_value_amount),
                    value = c.value?.let { trimNumber(it) }.orEmpty(),
                    onValueChange = { v -> charges[i] = c.copy(value = v.filter { ch -> ch.isDigit() || ch == '.' }.toDoubleOrNull()) },
                    placeholder = "0", keyboardType = KeyboardType.Decimal
                )
            }
            Spacer(modifier = Modifier.height(dimen_10))
            if (charges.size < 10) {
                Textview(
                    "+ " + stringResource(R.string.fin_add_charge), size = font_14, color = AmberPrimary, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { charges.add(Charge(name = "", type = "fixed", value = null)) }.padding(vertical = dimen_6)
                )
            }
        }

        */

        FinSection(stringResource(R.string.fin_default_commission)) {
            Textview(stringResource(R.string.fin_default_commission_hint), size = font_12, color = appColors.textSecondary)
            Spacer(modifier = Modifier.height(dimen_8))
            ChoiceChips(commissionOptions(b?.commission_types), commissionType) { commissionType = it }
            Spacer(modifier = Modifier.height(dimen_8))
            GlassInputField(
                label = commissionValueLabel(commissionType), value = commissionValue,
                onValueChange = { commissionValue = it.filter { c -> c.isDigit() || c == '.' } }, placeholder = "0", keyboardType = KeyboardType.Decimal
            )
        }

        b?.platform_fee_percent?.let {
            Textview(stringResource(R.string.fin_platform_fee_note, trimNumber(it)), size = font_12, color = appColors.textSecondary)
        }
        CommonGradientButton(
            text = if (state.saving) stringResource(R.string.fin_saving) else stringResource(R.string.fin_save),
            enabled = !state.saving,
            onClick = { vm.saveBilling(gst, gstNumber, charges.toList(), commissionType, commissionValue, advance, perKm) }
        )
        Spacer(modifier = Modifier.height(dimen_16))
    }
}

@Composable
fun commissionOptions(@Suppress("UNUSED_PARAMETER") server: List<IdName>?): List<IdName> = listOf(
    IdName("percent", stringResource(R.string.fin_commission_percent)),
    IdName("fixed", stringResource(R.string.fin_commission_fixed))
)

@Composable
fun commissionValueLabel(type: String): String = when (type) {
    "percent" -> stringResource(R.string.fin_value_percent)
    "per_km" -> stringResource(R.string.fin_value_per_km)
    else -> stringResource(R.string.fin_value_amount)
}

fun trimNumber(v: Double): String = if (v % 1.0 == 0.0) v.toLong().toString() else v.toString()

// =============================================================================================
// Owner: the bill of one job and the operator's commission (set / edit)
// =============================================================================================

@Composable
fun OwnerCommissionScreen(vm: FinanceViewModel, bookingId: String, onBack: () -> Unit) {
    val state by vm.ui.collectAsStateWithLifecycle()
    LaunchedEffect(bookingId) { vm.loadJobMoney(bookingId) }
    val job = state.jobMoney?.takeIf { it.booking_id == bookingId }
    val c = job?.operator_commission

    var type by rememberSaveable { mutableStateOf("percent") }
    var value by rememberSaveable { mutableStateOf("") }
    var filled by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(c) {
        if (c != null && !filled) {
            type = c.type?.takeIf { it != "per_km" } ?: "percent"
            value = c.value?.let { trimNumber(it) }.orEmpty()
            filled = true
        }
    }

    FinPage(stringResource(R.string.fin_job_money_title), onBack, subtitle = job?.booking_code) {
        Notice(state.notice)
        if (job == null) {
            if ("job_money" in state.loaded) EmptyText(stringResource(R.string.fin_not_found))
            return@FinPage
        }
        FinSection(stringResource(R.string.fin_bill_breakdown)) {
            BillLines(job.bill?.lines.orEmpty())
            if (job.bill?.isFinal != true) {
                Textview(stringResource(R.string.fin_bill_not_final), size = font_12, color = appColors.textSecondary, modifier = Modifier.padding(top = dimen_6))
            }
        }

        FinSection(stringResource(R.string.fin_operator_earning)) {
            TextRow(stringResource(R.string.fin_operator), job.assigned_operator?.full_name)
            if (c?.isSet == true) MoneyRow(stringResource(R.string.fin_operator_gets), c.amount, bold = true, color = FinGreen)
            if (c?.settlement_status == "settled") {
                Textview(stringResource(R.string.fin_commission_paid_on, formatServerDate(c.settled_at)), size = font_13, color = FinGreen)
            }
            if (c?.can_edit == true) {
                Spacer(modifier = Modifier.height(dimen_10))
                Textview(stringResource(R.string.fin_how_pay_operator), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(dimen_8))
                ChoiceChips(commissionOptions(c.types), type) { type = it }
                Spacer(modifier = Modifier.height(dimen_8))
                GlassInputField(
                    label = commissionValueLabel(type), value = value,
                    onValueChange = { value = it.filter { ch -> ch.isDigit() || ch == '.' } }, placeholder = "0", keyboardType = KeyboardType.Decimal
                )
                // updates while typing: what the operator will get for this job
                val entered = value.toDoubleOrNull()
                val preview = when {
                    entered == null -> null
                    type == "percent" -> c.job_value?.let { Math.round(it * entered) / 100.0 }
                    else -> entered
                }
                Spacer(modifier = Modifier.height(dimen_8))
                if (type == "percent" && c.job_value != null) {
                    TextRow(stringResource(R.string.cm_job_value), amountText(c.job_value))
                }
                MoneyRow(stringResource(R.string.cm_operator_will_get), preview, bold = true, color = FinGreen)
                Spacer(modifier = Modifier.height(dimen_12))
                CommonGradientButton(
                    text = if (state.saving) stringResource(R.string.fin_saving) else stringResource(R.string.fin_save_commission),
                    enabled = !state.saving && entered != null,
                    // saved: back to the job (it shows the operator payment)
                    onClick = { vm.saveCommission(bookingId, type, value, onSaved = onBack) }
                )
            } else if (job.assigned_operator == null) {
                Textview(stringResource(R.string.fin_assign_first), size = font_13, color = appColors.textSecondary)
            }
        }
        Spacer(modifier = Modifier.height(dimen_16))
    }
}

// =============================================================================================
// Owner: pay operators
// =============================================================================================

@Composable
fun OperatorDuesScreen(vm: FinanceViewModel, onBack: () -> Unit, onOpen: (String, String) -> Unit) {
    val state by vm.ui.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.loadDues() }
    val dues = state.dues

    FinPage(stringResource(R.string.fin_pay_operators), onBack, subtitle = stringResource(R.string.fin_pay_operators_sub)) {
        Notice(state.notice)
        FinSection {
            MoneyRow(stringResource(R.string.fin_total_to_pay), dues?.total_due, bold = true, color = AmberPrimary)
        }
        val items = dues?.items.orEmpty()
        if ("dues" in state.loaded && items.isEmpty()) EmptyText(stringResource(R.string.fin_no_operator_dues))
        items.forEach { d ->
            GlassCard(contentPadding = dimen_14, minHeight = dimen_64, onClick = { d.operator?.let { o -> o.id?.let { onOpen(it, o.name.orEmpty()) } } }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(d.operator?.name.orEmpty(), size = font_15, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                        Textview(stringResource(R.string.fin_jobs_count, d.jobs ?: 0), size = font_12, color = appColors.textSecondary)
                        if ((d.jobs_without_commission ?: 0) > 0) {
                            Textview(stringResource(R.string.fin_jobs_no_commission, d.jobs_without_commission ?: 0), size = font_12, color = FinRed)
                        }
                    }
                    Textview(amountText(d.amount_due), size = font_16, color = AmberPrimary, fontWeight = FontWeight.Bold)
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = appColors.textHint)
                }
            }
        }
        Spacer(modifier = Modifier.height(dimen_16))
    }
}

@Composable
fun PayOperatorScreen(vm: FinanceViewModel, operatorId: String, operatorName: String, onBack: () -> Unit, onPaid: () -> Unit) {
    val state by vm.ui.collectAsStateWithLifecycle()
    LaunchedEffect(operatorId) { vm.loadDueJobs(operatorId) }
    val jobs = state.dueJobs?.items.orEmpty()
    val selected = remember { mutableStateListOf<String>() }
    var initialised by remember { mutableStateOf(false) }
    LaunchedEffect(jobs) {
        if (!initialised && jobs.isNotEmpty()) {
            selected.clear()
            selected.addAll(jobs.filter { it.commission_set == true }.mapNotNull { it.booking_id })
            initialised = true
        }
    }
    var method by rememberSaveable { mutableStateOf("cash") }
    var reference by rememberSaveable { mutableStateOf("") }
    val total = jobs.filter { it.booking_id in selected }.sumOf { it.operator_commission ?: 0.0 }
    val methods = state.dues?.payout_methods?.takeIf { it.isNotEmpty() } ?: listOf(
        IdName("cash", stringResource(R.string.fin_method_cash)), IdName("upi", "UPI"), IdName("bank_transfer", stringResource(R.string.fin_method_bank))
    )

    FinPage(stringResource(R.string.fin_pay_name, operatorName), onBack) {
        FinSection(stringResource(R.string.fin_step_choose_jobs)) {
            if ("due_jobs" in state.loaded && jobs.isEmpty()) EmptyText(stringResource(R.string.fin_no_operator_dues))
            jobs.forEach { j ->
                val canPay = j.commission_set == true
                val on = j.booking_id in selected
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(dimen_10))
                        .clickable(enabled = canPay) {
                            j.booking_id?.let { id -> if (on) selected.remove(id) else selected.add(id) }
                        }
                        .padding(vertical = dimen_8),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (on) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank, contentDescription = null,
                        tint = if (canPay) AmberPrimary else appColors.textHint, modifier = Modifier.size(dimen_26)
                    )
                    Spacer(modifier = Modifier.width(dimen_10))
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(j.booking_code.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
                        Textview(formatServerDate(j.completed_at), size = font_12, color = appColors.textSecondary)
                        if (!canPay) Textview(stringResource(R.string.fin_set_commission_first), size = font_12, color = FinRed)
                    }
                    Textview(if (canPay) amountText(j.operator_commission) else "-", size = font_15, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                }
            }
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = dimen_6).height(dimen_1).background(appColors.subtleBorder))
            MoneyRow(stringResource(R.string.fin_you_pay_now), total, bold = true, color = AmberPrimary)
        }

        FinSection(stringResource(R.string.fin_step_how_paid)) {
            ChoiceChips(methods, method) { method = it }
            if (method != "cash") {
                Spacer(modifier = Modifier.height(dimen_8))
                GlassInputField(label = stringResource(R.string.fin_reference), value = reference, onValueChange = { reference = it }, placeholder = stringResource(R.string.fin_reference_hint), isRequired = true)
            }
        }

        Textview(stringResource(R.string.fin_pay_operator_note), size = font_12, color = appColors.textSecondary)
        CommonGradientButton(
            text = if (state.saving) stringResource(R.string.fin_saving) else stringResource(R.string.fin_mark_paid, amountText(total)),
            enabled = !state.saving && selected.isNotEmpty(),
            onClick = { vm.payOperator(operatorId, selected.toList(), method, reference, onPaid) }
        )
        Spacer(modifier = Modifier.height(dimen_16))
    }
}

// =============================================================================================
// Owner: settlement with the platform, and payments received
// =============================================================================================

@Composable
fun OwnerPlatformScreen(vm: FinanceViewModel, onBack: () -> Unit) {
    val state by vm.ui.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.loadPlatform() }
    val bal = state.platformBalance
    val pending = state.platform

    FinPage(stringResource(R.string.fin_platform_title), onBack, subtitle = stringResource(R.string.fin_platform_sub)) {
        FinSection(stringResource(R.string.fin_balance_now)) {
            MoneyRow(stringResource(R.string.fin_you_owe_platform), bal?.you_owe_platform, color = AmberPrimary)
            MoneyRow(stringResource(R.string.fin_platform_owes_you), bal?.platform_owes_you, color = FinBlue)
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = dimen_6).height(dimen_1).background(appColors.subtleBorder))
            Textview(bal?.balance_label.orEmpty(), size = font_16, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            Textview(amountText(kotlin.math.abs(bal?.balance ?: 0.0)), size = font_24, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            Textview(stringResource(R.string.fin_platform_explain), size = font_12, color = appColors.textSecondary, modifier = Modifier.padding(top = dimen_6))
            if ((bal?.unpaid_jobs ?: 0) > 0) {
                Textview(stringResource(R.string.fin_unpaid_jobs, bal?.unpaid_jobs ?: 0), size = font_12, color = FinRed, modifier = Modifier.padding(top = dimen_4))
            }
        }

        FinSection(stringResource(R.string.fin_jobs_not_settled)) {
            val items = pending?.items.orEmpty()
            if ("platform" in state.loaded && items.isEmpty()) EmptyText(stringResource(R.string.fin_all_settled))
            items.forEach { j ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = dimen_6), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(j.booking_code.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
                        Textview(
                            if (j.payment_status != "paid") stringResource(R.string.fin_customer_not_paid)
                            else if (j.collected_by == "platform") stringResource(R.string.fin_paid_online) else stringResource(R.string.fin_paid_cash),
                            size = font_12, color = if (j.payment_status != "paid") FinRed else appColors.textSecondary
                        )
                    }
                    val bal2 = j.platform_balance ?: 0.0
                    Textview(
                        if (bal2 < 0) stringResource(R.string.fin_you_pay_x, amountText(-bal2)) else if (bal2 > 0) stringResource(R.string.fin_you_get_x, amountText(bal2)) else "-",
                        size = font_13, color = if (bal2 < 0) AmberPrimary else FinBlue, fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        FinSection(stringResource(R.string.fin_history)) {
            if ("settlements" in state.loaded && state.settlements.isEmpty()) EmptyText(stringResource(R.string.fin_no_history))
        }
        state.settlements.forEach { SettlementRow(it) }
        Spacer(modifier = Modifier.height(dimen_16))
    }
}

@Composable
fun TransactionsScreen(vm: FinanceViewModel, onBack: () -> Unit, onOpen: (TransactionItem) -> Unit = {}) {
    val state by vm.ui.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.loadTransactions() }
    FinPage(stringResource(R.string.fin_payments_title), onBack, subtitle = stringResource(R.string.fin_payments_sub)) {
        if ("transactions" in state.loaded && state.transactions.isEmpty()) EmptyText(stringResource(R.string.fin_no_payments))
        // tap a payment for all its details
        state.transactions.forEach { TransactionRow(it, onOpen) }
        Spacer(modifier = Modifier.height(dimen_16))
    }
}

// =============================================================================================
// Owner: operators asking for open jobs
// =============================================================================================

@Composable
fun OwnerJobRequestsScreen(vm: FinanceViewModel, onBack: () -> Unit, onCall: (String) -> Unit) {
    val state by vm.ui.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.loadRequests() }
    var rejecting by rememberSaveable { mutableStateOf<String?>(null) }
    var reason by rememberSaveable { mutableStateOf("") }

    FinPage(stringResource(R.string.fin_job_requests_title), onBack, subtitle = stringResource(R.string.fin_job_requests_sub)) {
        Notice(state.notice)
        if ("requests" in state.loaded && state.requests.isEmpty()) EmptyText(stringResource(R.string.fin_no_requests))
        state.requests.forEach { r ->
            val op = r.operator
            FinSection {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(op?.full_name.orEmpty(), size = font_16, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                        Textview(
                            listOfNotNull(
                                op?.experience,
                                op?.rating?.let { "★ ${trimNumber(it)} (${op?.reviews_count ?: 0})" },
                                if (op?.license_verified == true) stringResource(R.string.fin_license_ok) else stringResource(R.string.fin_license_not_checked)
                            ).joinToString(" · "),
                            size = font_12, color = appColors.textSecondary
                        )
                    }
                    if (!op?.mobile_number.isNullOrBlank()) {
                        Icon(
                            Icons.Filled.Phone, contentDescription = stringResource(R.string.fin_call), tint = FinGreen,
                            modifier = Modifier.size(dimen_32).clickable { onCall("${op?.country_code.orEmpty()}${op?.mobile_number.orEmpty()}") }.padding(dimen_4)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(dimen_6))
                TextRow(stringResource(R.string.fin_job), r.job?.booking_code)
                TextRow(stringResource(R.string.fin_where), r.job?.location)
                TextRow(stringResource(R.string.fin_when), formatServerDateTime(r.job?.start_at))
                TextRow(stringResource(R.string.fin_duration), r.job?.duration_label)
                if (!r.message.isNullOrBlank()) Textview("\"${r.message}\"", size = font_13, color = appColors.textPrimary, modifier = Modifier.padding(vertical = dimen_4))
                Spacer(modifier = Modifier.height(dimen_8))
                if (rejecting == r.id) {
                    GlassInputField(label = stringResource(R.string.fin_reason_optional), value = reason, onValueChange = { reason = it }, placeholder = stringResource(R.string.fin_reason_hint))
                    Spacer(modifier = Modifier.height(dimen_8))
                    Row(horizontalArrangement = Arrangement.spacedBy(dimen_10)) {
                        CommonGradientButton(
                            text = stringResource(R.string.fin_cancel), modifier = Modifier.weight(1f), height = dimen_44,
                            gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = appColors.textPrimary,
                            onClick = { rejecting = null }
                        )
                        CommonGradientButton(
                            text = stringResource(R.string.fin_reject_now), modifier = Modifier.weight(1f), height = dimen_44,
                            gradientColors = listOf(FinRed, FinRed), textColor = Color.White, enabled = !state.saving,
                            onClick = { r.id?.let { vm.respondRequest(it, accept = false, reason = reason) }; rejecting = null; reason = "" }
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(dimen_10)) {
                        CommonGradientButton(
                            text = stringResource(R.string.fin_reject), modifier = Modifier.weight(1f), height = dimen_44,
                            gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = FinRed,
                            onClick = { rejecting = r.id; reason = "" }
                        )
                        CommonGradientButton(
                            text = stringResource(R.string.fin_give_job), modifier = Modifier.weight(1f), height = dimen_44, enabled = !state.saving,
                            onClick = { r.id?.let { vm.respondRequest(it, accept = true) } }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(dimen_16))
    }
}

/** Opens the phone dialler (no call is placed automatically) */
fun dialIntent(number: String) = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:$number"))

@Composable
fun rememberDialer(): (String) -> Unit {
    val context = LocalContext.current
    return { number -> runCatching { context.startActivity(dialIntent(number)) } }
}

/** One payment in full: amount, status, what it was for, ids (to quote to support or the bank) and the booking */
@Composable
fun TransactionDetailScreen(t: TransactionItem?, isOwner: Boolean, onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    FinPage(stringResource(R.string.tx_title), onBack, subtitle = t?.transaction_code) {
        if (t == null) {
            EmptyText(stringResource(R.string.fin_no_payments))
            return@FinPage
        }
        GlassCard(contentPadding = dimen_16) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Textview(amountText(t.amount), size = font_26, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(dimen_6))
                BookingTag(t.status_label.orEmpty(), statusTone(t.status))
                Spacer(modifier = Modifier.height(dimen_6))
                Textview(formatServerDateTime(t.created_at), size = font_12, color = appColors.textSecondary)
            }
        }
        FinSection(stringResource(R.string.tx_section_payment)) {
            TextRow(stringResource(R.string.tx_for), t.kind_label)
            TextRow(stringResource(R.string.tx_booking), t.booking_code)
            TextRow(stringResource(R.string.tx_method), t.method_name)
            TextRow(stringResource(R.string.tx_received_by), t.collected_by_label)
            if (isOwner) TextRow(stringResource(R.string.tx_customer), t.customer?.name) else TextRow(stringResource(R.string.tx_owner), t.owner?.name)
            if (!t.failure_reason.isNullOrBlank()) TextRow(stringResource(R.string.tx_reason), t.failure_reason)
        }
        FinSection(stringResource(R.string.tx_section_ids)) {
            TextRow(stringResource(R.string.tx_id), t.transaction_code)
            TextRow(stringResource(R.string.tx_gateway_id), t.gateway_payment_id)
            TextRow(stringResource(R.string.tx_order_id), t.gateway_order_id)
            Textview(stringResource(R.string.tx_ids_note), size = font_11, color = appColors.textHint, modifier = Modifier.padding(top = dimen_4))
        }
        t.booking_id?.let { id ->
            CommonGradientButton(
                text = stringResource(R.string.tx_view_booking),
                onClick = {
                    context.startActivity(
                        if (isOwner) com.cranezo.ui.ownerjobs.OwnerJobActivity.intent(context, id)
                        else com.cranezo.ui.booking.BookingActivity.bookingIntent(context, id)
                    )
                }
            )
        }
        Spacer(modifier = Modifier.height(dimen_16))
    }
}
