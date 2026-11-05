package com.cranezo.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cranezo.R
import com.cranezo.ui.booking.components.BookingTag
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.GlassInputField
import com.cranezo.ui.components.Textview
import com.cranezo.ui.finance.model.IdName
import com.cranezo.ui.theme.*
import com.cranezo.utils.formatServerDate
import com.cranezo.utils.formatServerDateTime

// =============================================================================================
// Hub: one page per role that lists the money and request pages
// =============================================================================================

@Composable
fun FinanceHubScreen(role: String, onBack: () -> Unit, onOpen: (String) -> Unit) {
    when (role) {
        "CRANE_OWNER" -> FinPage(stringResource(R.string.fin_hub_owner), onBack) {
            FinMenuRow(Icons.Filled.Search, stringResource(R.string.rq_owner_title), stringResource(R.string.rq_owner_card_sub)) { onOpen(FinanceRoutes.CRANE_REQUESTS) }
            FinMenuRow(Icons.Filled.Groups, stringResource(R.string.fin_pay_operators), stringResource(R.string.fin_pay_operators_sub)) { onOpen(FinanceRoutes.OPERATOR_DUES) }
            FinMenuRow(Icons.Filled.AccountBalance, stringResource(R.string.fin_platform_title), stringResource(R.string.fin_platform_sub)) { onOpen(FinanceRoutes.PLATFORM) }
            FinMenuRow(Icons.Filled.Payments, stringResource(R.string.fin_payments_title), stringResource(R.string.fin_payments_sub)) { onOpen(FinanceRoutes.TRANSACTIONS) }
            FinMenuRow(Icons.Filled.Percent, stringResource(R.string.fin_billing_title), stringResource(R.string.fin_billing_subtitle)) { onOpen(FinanceRoutes.BILLING) }
            FinMenuRow(Icons.Filled.ReportProblem, stringResource(R.string.fin_complaints_title), stringResource(R.string.fin_complaints_owner_sub)) { onOpen(FinanceRoutes.COMPLAINTS) }
        }
        "OPERATOR" -> FinPage(stringResource(R.string.fin_hub_operator), onBack) {
            // operators no longer browse jobs: owners give a job by the operator's ID
            FinMenuRow(Icons.Filled.Payments, stringResource(R.string.fin_my_payments), stringResource(R.string.fin_my_payments_sub)) { onOpen(FinanceRoutes.MY_PAYMENTS) }
        }
        else -> FinPage(stringResource(R.string.fin_hub_customer), onBack) {
            FinMenuRow(Icons.AutoMirrored.Filled.ReceiptLong, stringResource(R.string.fin_payments_title), stringResource(R.string.fin_customer_payments_sub)) { onOpen(FinanceRoutes.TRANSACTIONS) }
            FinMenuRow(Icons.Filled.ReportProblem, stringResource(R.string.fin_complaints_title), stringResource(R.string.fin_complaints_customer_sub)) { onOpen(FinanceRoutes.COMPLAINTS) }
        }
    }
}

// =============================================================================================
// Operator: find open jobs and ask the owner
// =============================================================================================

@Composable
fun FindJobsScreen(vm: FinanceViewModel, onBack: () -> Unit) {
    val state by vm.ui.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.loadAvailable() }
    var asking by rememberSaveable { mutableStateOf<String?>(null) }
    var message by rememberSaveable { mutableStateOf("") }

    FinPage(stringResource(R.string.fin_find_jobs), onBack, subtitle = stringResource(R.string.fin_find_jobs_sub)) {
        Notice(state.notice)
        if ("available" in state.loaded && state.available.isEmpty()) EmptyText(stringResource(R.string.fin_no_open_jobs))
        state.available.forEach { j ->
            FinSection {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(j.crane_label.orEmpty(), size = font_16, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                        Textview(j.owner_name.orEmpty(), size = font_12, color = appColors.textSecondary)
                    }
                    j.distance_km?.let { BookingTag(stringResource(R.string.fin_km_away, trimNumber(it)), FinBlue) }
                }
                Spacer(modifier = Modifier.height(dimen_6))
                TextRow(stringResource(R.string.fin_where), j.site?.name ?: j.site?.address)
                TextRow(stringResource(R.string.fin_when), formatServerDateTime(j.start_at))
                TextRow(stringResource(R.string.fin_duration), j.duration_label)
                Spacer(modifier = Modifier.height(dimen_6))
                // the most important line for the operator: what they earn
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(dimen_12))
                        .background(FinGreen.copy(alpha = 0.12f)).padding(dimen_12)
                ) {
                    Column {
                        Textview(stringResource(R.string.fin_you_earn), size = font_12, color = appColors.textSecondary)
                        Textview(
                            j.expected_earnings?.let { amountText(it) } ?: j.expected_earnings_label.orEmpty(),
                            size = if (j.expected_earnings != null) font_22 else font_14, color = FinGreen, fontWeight = FontWeight.Bold
                        )
                        if (j.expected_earnings != null) Textview(j.expected_earnings_label.orEmpty(), size = font_11, color = appColors.textSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(dimen_10))
                val mine = j.my_request
                when {
                    mine != null && j.can_request != true -> {
                        BookingTag(mine.status_label.orEmpty(), statusTone(if (mine.status == 2) "rejected" else if (mine.status == 1) "accepted" else null))
                    }
                    asking == j.booking_id -> {
                        GlassInputField(label = stringResource(R.string.fin_message_optional), value = message, onValueChange = { message = it.take(300) }, placeholder = stringResource(R.string.fin_message_hint))
                        Spacer(modifier = Modifier.height(dimen_8))
                        Row(horizontalArrangement = Arrangement.spacedBy(dimen_10)) {
                            CommonGradientButton(
                                text = stringResource(R.string.fin_cancel), modifier = Modifier.weight(1f), height = dimen_48,
                                gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = appColors.textPrimary,
                                onClick = { asking = null }
                            )
                            CommonGradientButton(
                                text = stringResource(R.string.fin_send_request), modifier = Modifier.weight(1f), height = dimen_48, enabled = !state.saving,
                                onClick = { j.booking_id?.let { vm.requestJob(it, message) }; asking = null; message = "" }
                            )
                        }
                    }
                    else -> CommonGradientButton(
                        text = stringResource(R.string.fin_ask_for_job), height = dimen_48,
                        onClick = { asking = j.booking_id; message = "" }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(dimen_16))
    }
}

@Composable
fun MyRequestsScreen(vm: FinanceViewModel, onBack: () -> Unit) {
    val state by vm.ui.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.loadMyRequests() }
    FinPage(stringResource(R.string.fin_my_requests), onBack, subtitle = stringResource(R.string.fin_my_requests_sub)) {
        Notice(state.notice)
        if ("my_requests" in state.loaded && state.myRequests.isEmpty()) EmptyText(stringResource(R.string.fin_no_my_requests))
        state.myRequests.forEach { r ->
            FinSection {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(r.booking_code.orEmpty(), size = font_15, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                        Textview(r.location.orEmpty(), size = font_12, color = appColors.textSecondary, maxLines = 1)
                        Textview(formatServerDateTime(r.start_at), size = font_12, color = appColors.textSecondary)
                    }
                    BookingTag(r.status_label.orEmpty(), statusTone(when (r.status) { 1 -> "accepted"; 2 -> "rejected"; else -> null }))
                }
                if (!r.reject_reason.isNullOrBlank()) Textview(r.reject_reason, size = font_12, color = FinRed, modifier = Modifier.padding(top = dimen_4))
                if (r.status == 0) {
                    Spacer(modifier = Modifier.height(dimen_8))
                    Textview(
                        stringResource(R.string.fin_withdraw), size = font_14, color = FinRed, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(enabled = !state.saving) { r.id?.let { vm.withdrawRequest(it) } }.padding(vertical = dimen_4)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(dimen_16))
    }
}

@Composable
fun MyPaymentsScreen(vm: FinanceViewModel, onBack: () -> Unit) {
    val state by vm.ui.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.loadOperatorPayments() }
    val e = state.opEarnings
    FinPage(stringResource(R.string.fin_my_payments), onBack, subtitle = stringResource(R.string.fin_my_payments_sub)) {
        FinSection(stringResource(R.string.fin_my_earnings)) {
            MoneyRow(stringResource(R.string.fin_total_earned), e?.total_earned, bold = true)
            MoneyRow(stringResource(R.string.fin_paid_to_you), e?.completed_settlement, color = FinGreen)
            MoneyRow(stringResource(R.string.fin_owner_yet_to_pay), e?.pending_settlement, color = AmberPrimary)
            if ((e?.jobs_without_commission ?: 0) > 0) {
                Textview(stringResource(R.string.fin_jobs_waiting_commission, e?.jobs_without_commission ?: 0), size = font_12, color = appColors.textSecondary, modifier = Modifier.padding(top = dimen_4))
            }
        }
        Textview(stringResource(R.string.fin_payments_received), size = font_15, color = appColors.textPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = dimen_8))
        if ("settlements" in state.loaded && state.settlements.isEmpty()) EmptyText(stringResource(R.string.fin_no_payments_yet))
        state.settlements.forEach { SettlementRow(it) }
        Spacer(modifier = Modifier.height(dimen_16))
    }
}

// =============================================================================================
// Complaints: customer raises and follows; owner replies and resolves
// =============================================================================================

@Composable
fun ComplaintsScreen(vm: FinanceViewModel, onBack: () -> Unit, onOpen: (String) -> Unit, onNew: (() -> Unit)?) {
    val state by vm.ui.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.loadComplaints() }
    FinPage(
        stringResource(R.string.fin_complaints_title), onBack,
        subtitle = stringResource(if (onNew != null) R.string.fin_complaints_customer_sub else R.string.fin_complaints_owner_sub)
    ) {
        if (onNew != null) CommonGradientButton(text = stringResource(R.string.fin_new_complaint), onClick = onNew)
        if ("complaints" in state.loaded && state.complaints.isEmpty()) EmptyText(stringResource(R.string.fin_no_complaints))
        state.complaints.forEach { c ->
            GlassCard(contentPadding = dimen_12, minHeight = dimen_64, onClick = { c.id?.let(onOpen) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Textview(c.complaint_code.orEmpty(), size = font_11, color = appColors.textSecondary, modifier = Modifier.weight(1f))
                    BookingTag(c.status_label.orEmpty(), statusTone(c.status))
                }
                Textview(c.category_name.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                Textview(
                    listOfNotNull(c.booking?.booking_code, formatServerDate(c.created_at)).joinToString(" · "),
                    size = font_12, color = appColors.textSecondary
                )
            }
        }
        Spacer(modifier = Modifier.height(dimen_16))
    }
}

@Composable
fun NewComplaintScreen(vm: FinanceViewModel, presetBookingId: String?, onBack: () -> Unit, onCreated: (String) -> Unit) {
    val state by vm.ui.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.loadComplaintForm() }
    LaunchedEffect(state.createdComplaintId) {
        if (state.createdComplaintId != null) vm.consumeCreatedComplaint()?.let(onCreated)
    }
    var category by rememberSaveable { mutableStateOf("") }
    var bookingId by rememberSaveable { mutableStateOf(presetBookingId) }
    var description by rememberSaveable { mutableStateOf("") }

    FinPage(stringResource(R.string.fin_new_complaint), onBack, subtitle = stringResource(R.string.fin_new_complaint_sub)) {
        FinSection(stringResource(R.string.fin_step_what_wrong)) {
            ChoiceChips(state.categories.map { IdName(it.id, it.name) }, category) { category = it }
            state.categories.firstOrNull { it.id == category }?.description?.let {
                Textview(it, size = font_12, color = appColors.textSecondary, modifier = Modifier.padding(top = dimen_6))
            }
        }
        FinSection(stringResource(R.string.fin_step_which_booking)) {
            val options = state.bookings.map { b ->
                IdName(b.booking_id, listOfNotNull(b.booking_code, b.location, formatServerDate(b.start_at)).joinToString(" · "))
            } + IdName("none", stringResource(R.string.fin_no_booking))
            ChoiceChips(options, bookingId ?: "none") { bookingId = if (it == "none") null else it }
        }
        FinSection(stringResource(R.string.fin_step_tell_us)) {
            GlassInputField(
                label = stringResource(R.string.fin_describe), value = description, onValueChange = { description = it.take(2000) },
                placeholder = stringResource(R.string.fin_describe_hint), singleLine = false, minHeight = dimen_100
            )
        }
        CommonGradientButton(
            text = if (state.saving) stringResource(R.string.fin_sending) else stringResource(R.string.fin_submit_complaint),
            enabled = !state.saving,
            onClick = { vm.createComplaint(bookingId, category, description) }
        )
        Spacer(modifier = Modifier.height(dimen_16))
    }
}

@Composable
fun ComplaintDetailScreen(vm: FinanceViewModel, complaintId: String, isOwner: Boolean, onBack: () -> Unit) {
    val state by vm.ui.collectAsStateWithLifecycle()
    LaunchedEffect(complaintId) { vm.openComplaint(complaintId) }
    val c = state.complaint?.takeIf { it.id == complaintId }
    var message by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableStateOf<String?>(null) }

    FinPage(stringResource(R.string.fin_complaint), onBack, subtitle = c?.complaint_code) {
        Notice(state.notice)
        if (c == null) {
            if ("complaint" in state.loaded) EmptyText(stringResource(R.string.fin_not_found))
            return@FinPage
        }
        FinSection(c.category_name) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Textview(stringResource(R.string.fin_status), size = font_13, color = appColors.textSecondary, modifier = Modifier.weight(1f))
                BookingTag(c.status_label.orEmpty(), statusTone(c.status))
            }
            c.booking?.let { b ->
                TextRow(stringResource(R.string.fin_job), b.booking_code)
                TextRow(stringResource(R.string.fin_amount), b.amount?.let { amountText(it) })
            }
            if (isOwner) TextRow(stringResource(R.string.fin_customer), c.customer?.name)
            Spacer(modifier = Modifier.height(dimen_6))
            Textview(c.description.orEmpty(), size = font_14, color = appColors.textPrimary)
            if (!c.resolution_note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(dimen_6))
                Textview(stringResource(R.string.fin_outcome, c.resolution_note), size = font_13, color = FinGreen, fontWeight = FontWeight.SemiBold)
            }
        }

        FinSection(stringResource(R.string.fin_history)) {
            c.updates.orEmpty().forEach { u ->
                Column(modifier = Modifier.padding(vertical = dimen_6)) {
                    Textview(
                        listOfNotNull(u.author_name ?: u.author_type, formatServerDateTime(u.created_at)).joinToString(" · "),
                        size = font_11, color = appColors.textHint
                    )
                    if (!u.status_to_label.isNullOrBlank()) Textview(stringResource(R.string.fin_status_changed, u.status_to_label), size = font_12, color = AmberPrimary)
                    if (!u.message.isNullOrBlank()) Textview(u.message, size = font_14, color = appColors.textPrimary)
                }
            }
        }

        if (c.can_reply == true) {
            FinSection(stringResource(R.string.fin_add_message)) {
                GlassInputField(
                    label = stringResource(R.string.fin_message), value = message, onValueChange = { message = it.take(2000) },
                    placeholder = stringResource(R.string.fin_message_hint2), singleLine = false, minHeight = dimen_88
                )
                if (isOwner) {
                    Spacer(modifier = Modifier.height(dimen_8))
                    Textview(stringResource(R.string.fin_change_status), size = font_13, color = appColors.textSecondary)
                    Spacer(modifier = Modifier.height(dimen_6))
                    ChoiceChips(
                        listOf(
                            IdName("keep", stringResource(R.string.fin_status_keep)),
                            IdName("in_review", stringResource(R.string.fin_status_in_review)),
                            IdName("resolved", stringResource(R.string.fin_status_resolved))
                        ),
                        status ?: "keep"
                    ) { status = if (it == "keep") null else it }
                }
                Spacer(modifier = Modifier.height(dimen_10))
                CommonGradientButton(
                    text = if (state.saving) stringResource(R.string.fin_sending) else stringResource(R.string.fin_send),
                    enabled = !state.saving,
                    onClick = {
                        vm.replyComplaint(complaintId, message, if (isOwner) status else null)
                        message = ""
                        status = null
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(dimen_16))
    }
}
