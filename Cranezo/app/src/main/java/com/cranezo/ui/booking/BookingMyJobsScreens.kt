package com.cranezo.ui.booking

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cranezo.R
import com.cranezo.ui.booking.components.BookingScaffold
import com.cranezo.ui.booking.components.BookingTag
import com.cranezo.ui.booking.components.PriceLine
import com.cranezo.ui.booking.model.ActiveOperator
import com.cranezo.ui.booking.model.ActiveTrack
import com.cranezo.ui.booking.model.BookingIntent
import com.cranezo.ui.booking.model.BookingStatus
import com.cranezo.ui.booking.model.BookingUiState
import com.cranezo.ui.booking.model.JobCounts
import com.cranezo.ui.booking.model.MyJobCrane
import com.cranezo.ui.booking.model.MyJobItem
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.Textview
import com.cranezo.ui.cranes.components.CraneImage
import com.cranezo.ui.mainhome.components.InfoBlue
import com.cranezo.ui.mainhome.components.SuccessGreen
import com.cranezo.ui.theme.*
import com.cranezo.utils.formatServerDate
import com.cranezo.utils.formatServerDateTime
import com.cranezo.utils.formatServerTime
import com.cranezo.utils.rupees
import kotlinx.coroutines.delay
import java.util.Locale

private val previewJobItem = MyJobItem(
    booking_id = "1", booking_code = "CRZ-260905-08421", status_label = "Confirmed", tab = "upcoming", tab_label = "Upcoming", stage = "accepted",
    crane = MyJobCrane(title = "50 Ton Mobile Crane", capacity_label = "50 Ton Capacity", crane_code = "CRN001"),
    location = "Kakinada", start_at = "2026-09-05T03:48:00.000Z", amount = 32500.0
)

@Composable
private fun tabTagColor(tab: String?, label: String?): Color = when (tab) {
    "active" -> SuccessGreen
    "completed" -> SuccessGreen
    "cancelled" -> Red
    else -> if (label == "Awaiting Owner") InfoBlue else AmberPrimary
}

// ---------------------------------------------------------------------------------------------
// My Jobs
// ---------------------------------------------------------------------------------------------

@Preview(showBackground = true, heightDp = 800)
@Composable
fun MyJobsScreen(vm: BookingViewModel? = null, onBack: () -> Unit = {}) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value
        ?: BookingUiState(jobs = listOf(previewJobItem), jobsLoaded = true, counts = JobCounts(2, 1, 4, 2))
    LaunchedEffect(Unit) { vm?.onIntent(BookingIntent.LoadJobs(state.jobsTab)) }

    BookingScaffold(onBack = onBack, title = stringResource(R.string.bk_my_jobs), subtitle = stringResource(R.string.cj_my_jobs_sub)) {
        // One filter dropdown (All by default) instead of a row of chips
        val c = state.counts
        val filters = listOf(
            Triple("all", R.string.jf_all, c?.let { (it.upcoming ?: 0) + (it.active ?: 0) + (it.completed ?: 0) + (it.cancelled ?: 0) }),
            Triple("upcoming", R.string.oj_tab_upcoming, c?.upcoming),
            Triple("active", R.string.oj_tab_active, c?.active),
            Triple("completed", R.string.op_tab_completed, c?.completed),
            Triple("cancelled", R.string.cj_tab_cancelled, c?.cancelled)
        )
        var filterOpen by remember { mutableStateOf(false) }
        val current = filters.firstOrNull { it.first == state.jobsTab } ?: filters.first()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Textview(stringResource(R.string.jf_showing), size = font_12, color = appColors.textSecondary, modifier = Modifier.weight(1f))
            Box {
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(dimen_16))
                        .background(AmberPrimary.copy(alpha = 0.16f))
                        .clickable { filterOpen = true }
                        .padding(horizontal = dimen_12, vertical = dimen_8),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Textview(
                        stringResource(current.second) + (current.third?.let { " ($it)" } ?: ""),
                        size = font_12, color = appColors.primaryText, fontWeight = FontWeight.Bold
                    )
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = appColors.primaryText, modifier = Modifier.size(dimen_18))
                }
                androidx.compose.material3.DropdownMenu(expanded = filterOpen, onDismissRequest = { filterOpen = false }) {
                    filters.forEach { (key, label, count) ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = {
                                Textview(
                                    stringResource(label) + (count?.let { " ($it)" } ?: ""), size = font_13,
                                    color = if (key == state.jobsTab) appColors.primaryText else appColors.textPrimary,
                                    fontWeight = if (key == state.jobsTab) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                filterOpen = false
                                vm?.onIntent(BookingIntent.LoadJobs(key))
                            }
                        )
                    }
                }
            }
        }

        if (state.jobsLoaded && state.jobs.isEmpty()) {
            Textview(stringResource(R.string.bk_no_bookings), size = font_14, color = appColors.textSecondary, modifier = Modifier.fillMaxWidth().padding(top = dimen_40), textAlign = TextAlign.Center)
        }
        state.jobs.forEach { job ->
            GlassCard(contentPadding = dimen_12, minHeight = dimen_104, onClick = { job.booking_id?.let { vm?.onIntent(BookingIntent.OpenBooking(it)) } }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CraneImage(path = job.crane?.primary_photo, modifier = Modifier.size(dimen_70))
                    Spacer(modifier = Modifier.width(dimen_12))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Textview(job.booking_code.orEmpty(), size = font_10, color = appColors.textSecondary, modifier = Modifier.weight(1f), maxLines = 1)
                            BookingTag(job.tab_label ?: job.status_label.orEmpty(), tabTagColor(job.tab, job.status_label))
                        }
                        Textview(job.crane?.title.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(dimen_12))
                            Textview(job.location.orEmpty(), size = font_11, color = appColors.textSecondary, maxLines = 1)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(dimen_12))
                            Textview(formatServerDate(job.start_at), size = font_11, color = appColors.textSecondary, modifier = Modifier.weight(1f))
                            Textview(rupees(job.amount), size = font_14, color = appColors.primaryText, fontWeight = FontWeight.Bold)
                        }
                        if (job.status_label == "Awaiting Owner" && job.tab == "upcoming") BookingTag(job.status_label.orEmpty(), InfoBlue)
                        if (job.can_rate == true) BookingTag(stringResource(R.string.cj_rate), AmberPrimary)
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = appColors.textSecondary)
                }
            }
        }
        if (state.jobsHasMore) LaunchedEffect(state.jobs.size) { vm?.onIntent(BookingIntent.LoadMoreJobs) }
        Spacer(modifier = Modifier.height(dimen_8))
    }
}

// ---------------------------------------------------------------------------------------------
// Job Details: the screen of the booking's stage. Stages that need an action show their own screen.
// ---------------------------------------------------------------------------------------------

@Preview(showBackground = true, heightDp = 1000)
@Composable
fun JobDetailsScreen(
    vm: BookingViewModel? = null,
    onBack: () -> Unit = {},
    onTrack: () -> Unit = {},
    onExtra: () -> Unit = {},
    onExtraStatus: () -> Unit = {},
    onPay: () -> Unit = {},
    onReceipt: () -> Unit = {}
) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value ?: BookingUiState(booking = com.cranezo.ui.booking.model.Booking(
        booking_code = "CRZ-1", tab_label = "Upcoming", stage = "accepted", crane = com.cranezo.ui.booking.model.BookingCrane(title = "50 Ton Mobile Crane", capacity_label = "50 Ton Capacity")
    ))
    val booking = state.booking ?: return
    val context = LocalContext.current
    var cancelOpen by remember { mutableStateOf(false) }

    // Whatever the stage is, the booking is kept fresh while it can still change
    val live = booking.stage in listOf("accepted", "awaiting_operator", "en_route", "summary")
    LaunchedEffect(booking.booking_id, live) {
        val id = booking.booking_id ?: return@LaunchedEffect
        while (live) {
            delay(5000)
            vm?.onIntent(BookingIntent.PollBooking(id))
        }
    }

    // An owner accepted and the advance is not paid yet: the request screen has the countdown and Pay button
    if (booking.stage == "awaiting_payment") {
        val context = androidx.compose.ui.platform.LocalContext.current
        LaunchedEffect(booking.booking_id) {
            context.startActivity(com.cranezo.ui.booking.BookingActivity.intent(context, BookingRoutes.REQUEST))
            onBack()
        }
        return
    }

    when (booking.stage) {
        "awaiting_owner" -> { WaitingScreen(vm, onConfirmed = {}, onChooseAnother = { vm?.onIntent(BookingIntent.BookAgain) }, onClose = onBack); return }
        "arrived" -> { CraneArrivedScreen(vm, onBack); return }
        "working" -> { JobInProgressScreen(vm, onBack, onExtra, onExtraStatus); return }
        "awaiting_confirmation" -> { ShareCodeScreen(vm, onBack); return }
    }

    val completed = booking.stage == "completed"
    val closed = booking.stage == "closed"
    val actions = booking.actions

    BookingScaffold(
        onBack = onBack,
        title = stringResource(R.string.oj_job_details),
        bottom = {
            Column(verticalArrangement = Arrangement.spacedBy(dimen_8)) {
                when {
                    closed -> {
                        CommonGradientButton(text = stringResource(R.string.cj_book_again), onClick = { vm?.onIntent(BookingIntent.BookAgain) })
                    }
                    completed -> {
                        if (actions?.can_pay == true) CommonGradientButton(text = stringResource(R.string.cj_complete_payment), onClick = onPay)
                        if (actions?.can_view_receipt == true) CommonGradientButton(text = stringResource(R.string.cj_view_receipt), onClick = onReceipt)
                    }
                    else -> {
                        if (actions?.can_track == true) CommonGradientButton(text = stringResource(R.string.cj_view_live_tracking), onClick = onTrack)
                        if (actions?.can_cancel == true) {
                            // asks once more right here (no popup)
                            if (cancelOpen) {
                                com.cranezo.ui.components.InlineConfirm(
                                    message = stringResource(R.string.cj_cancel_booking_message),
                                    confirmText = stringResource(R.string.cj_cancel_booking),
                                    cancelText = stringResource(R.string.common_back),
                                    onConfirm = {
                                        cancelOpen = false
                                        vm?.onIntent(BookingIntent.CancelBooking)
                                    },
                                    onCancel = { cancelOpen = false }
                                )
                            } else {
                                CommonGradientButton(
                                    text = stringResource(R.string.cj_cancel_booking), onClick = { cancelOpen = true },
                                    gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = Red
                                )
                            }
                        }
                    }
                }
                if (booking.operator?.mobile_number != null) {
                    CommonGradientButton(
                        text = stringResource(R.string.cj_contact_operator), leftIcon = Icons.Default.Call,
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${booking.operator.country_code.orEmpty()}${booking.operator.mobile_number}")))
                        },
                        gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = appColors.textPrimary
                    )
                }
            }
        }
    ) {
        GlassCard(contentPadding = dimen_12) {
            Row {
                CraneImage(path = booking.crane?.primary_photo, modifier = Modifier.size(dimen_80))
                Spacer(modifier = Modifier.width(dimen_12))
                Column {
                    Textview(booking.booking_code.orEmpty(), size = font_10, color = appColors.textSecondary)
                    BookingTag(booking.tab_label ?: booking.status_label.orEmpty(), if (closed) Red else if (completed || booking.tab == "active") SuccessGreen else AmberPrimary)
                    Textview(booking.crane?.title.orEmpty(), size = font_15, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                    Textview(booking.crane?.capacity_label ?: booking.crane?.capacity_name.orEmpty(), size = font_11, color = appColors.textSecondary)
                }
            }
            Spacer(modifier = Modifier.height(dimen_8))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(dimen_14))
                Textview(" ${booking.site?.name.orEmpty()}", size = font_12, color = appColors.textPrimary)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(dimen_14))
                val range = if (completed && booking.job_summary != null)
                    "${formatServerDateTime(booking.job_summary.started_at)} - ${formatServerTime(booking.job_summary.completed_at)}"
                else "${formatServerDateTime(booking.schedule?.start_at)} - ${formatServerTime(booking.schedule?.end_at)}"
                Textview(" $range", size = font_11, color = appColors.textSecondary)
            }
        }

        // Crane and operator, the operator part only when there is one
        GlassCard(contentPadding = dimen_12) {
            Textview(stringResource(R.string.cj_crane_operator), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(dimen_8))
            Row {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    CraneImage(path = booking.crane?.primary_photo, modifier = Modifier.size(dimen_56), shape = CircleShape)
                    Textview(booking.crane?.title.orEmpty(), size = font_11, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    if (booking.crane?.verified == true) BookingTag(stringResource(R.string.common_verified).uppercase(), SuccessGreen)
                }
                booking.operator?.let { operator ->
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        CraneImage(path = operator.profile_image, modifier = Modifier.size(dimen_56), shape = CircleShape)
                        Textview(operator.full_name.orEmpty(), size = font_11, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        operator.rating?.let {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(dimen_12))
                                Textview(" ${String.format(Locale.US, "%.1f", it)}", size = font_10, color = appColors.textSecondary)
                            }
                        }
                        BookingTag(stringResource(R.string.common_verified).uppercase(), SuccessGreen)
                    }
                }
            }
        }

        // how much is paid and how much is left (advance now, balance after the job)
        if (!closed) PaymentProgressCard(booking.payment)
        // transaction history
        com.cranezo.ui.ownerjobs.TransactionHistory(booking.transactions?.takeIf { it.isNotEmpty() })

        when {
            closed -> CancellationBlock(booking.cancellation)
            booking.timeline != null && (booking.tab == "active" || completed) -> TimelineBlock(booking)
            else -> SummaryRowsBlock(booking)
        }

        if (completed) {
            GlassCard(contentPadding = dimen_14) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Textview(stringResource(R.string.bk_payment_summary), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    val paid = booking.payment?.status == "paid"
                    BookingTag((booking.payment?.status_label ?: "").uppercase(), if (paid) SuccessGreen else AmberPrimary)
                }
                Spacer(modifier = Modifier.height(dimen_6))
                booking.payment?.let { p ->
                    PriceLine(stringResource(R.string.cj_booking_amount, (p.booking_hours ?: 0.0).toInt()), rupees(p.booking_amount))
                    if ((p.extra_time_amount ?: 0.0) > 0) PriceLine(stringResource(R.string.cj_extra_time_min, p.extra_minutes ?: 0), rupees(p.extra_time_amount))
                    PriceLine(stringResource(R.string.bk_total_amount), rupees(p.final_total), bold = true)
                }
            }
            if (actions?.can_rate == true) {
                Textview(stringResource(R.string.cj_rate_from_jobs), size = font_11, color = appColors.primaryText)
            }
        }
        Spacer(modifier = Modifier.height(dimen_8))
    }
}

@Composable
private fun SummaryRowsBlock(booking: com.cranezo.ui.booking.model.Booking) {
    val rows = booking.summary_rows ?: return
    GlassCard(contentPadding = dimen_14) {
        Textview(stringResource(R.string.oj_job_timeline), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(dimen_6))
        PriceLine(stringResource(R.string.cj_booking_date), formatServerDateTime(rows.booking_date))
        PriceLine(stringResource(R.string.bk_site_address), rows.location.orEmpty())
        PriceLine(stringResource(R.string.cj_estimated_duration), rows.estimated_duration.orEmpty())
        PriceLine(stringResource(R.string.cj_estimated_amount), rupees(rows.estimated_amount))
        PriceLine(stringResource(R.string.cj_payment_status), rows.payment_status.orEmpty())
    }
}

@Composable
private fun TimelineBlock(booking: com.cranezo.ui.booking.model.Booking) {
    GlassCard(contentPadding = dimen_14) {
        Textview(stringResource(R.string.oj_job_timeline), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(dimen_8))
        booking.timeline.orEmpty().forEach { step ->
            Row(modifier = Modifier.padding(vertical = dimen_5), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (step.state == "pending") Icons.Default.RadioButtonUnchecked else Icons.Default.CheckCircle, contentDescription = null,
                    tint = when (step.state) { "done" -> AmberPrimary; "ongoing" -> SuccessGreen; else -> appColors.textHint }, modifier = Modifier.size(dimen_16)
                )
                Spacer(modifier = Modifier.width(dimen_10))
                Textview(step.label.orEmpty(), size = font_12, color = if (step.state == "pending") appColors.textSecondary else appColors.textPrimary, modifier = Modifier.weight(1f))
                Textview(if (step.at != null) formatServerDateTime(step.at) else if (step.state == "ongoing") stringResource(R.string.cj_ongoing) else "–", size = font_10, color = appColors.textSecondary)
            }
        }
    }
}

@Composable
private fun CancellationBlock(cancellation: com.cranezo.ui.booking.model.CancellationInfo?) {
    cancellation ?: return
    GlassCard(contentPadding = dimen_14, borderColor = Red) {
        Textview(stringResource(R.string.cj_cancellation_details), size = font_13, color = Red, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(dimen_6))
        PriceLine(stringResource(R.string.cj_cancelled_by), cancellation.cancelled_by_label.orEmpty())
        PriceLine(stringResource(R.string.cj_cancellation_date), formatServerDateTime(cancellation.cancelled_at))
        if (!cancellation.reason.isNullOrBlank()) PriceLine(stringResource(R.string.cj_reason), cancellation.reason)
        PriceLine(stringResource(R.string.cj_refund_status), cancellation.refund?.status_label.orEmpty())
        if ((cancellation.refund?.amount ?: 0.0) > 0) PriceLine(stringResource(R.string.cj_refund_amount), rupees(cancellation.refund?.amount))
    }
}

// ---------------------------------------------------------------------------------------------
// Track My Cranes
// ---------------------------------------------------------------------------------------------

@Preview(showBackground = true, heightDp = 800)
@Composable
fun TrackMyCranesScreen(vm: BookingViewModel? = null, onBack: () -> Unit = {}) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value ?: BookingUiState(
        activeTracks = listOf(ActiveTrack("1", "CRZ-260908-07431", MyJobCrane(title = "50 Ton Mobile Crane"), ActiveOperator("Rajesh Kumar", 4.8, 98), "en_route", "On the way", 6.4, 18)),
        activeLoaded = true
    )
    // Every 12 seconds while the screen is open
    LaunchedEffect(Unit) {
        while (true) {
            vm?.onIntent(BookingIntent.LoadActiveTracking)
            delay(12_000)
        }
    }

    BookingScaffold(onBack = onBack, title = stringResource(R.string.cj_track_cranes), subtitle = stringResource(R.string.cj_track_sub)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Textview(stringResource(R.string.cj_n_active_cranes, state.activeTracks.size), size = font_14, color = appColors.primaryText, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            BookingTag(stringResource(R.string.cj_realtime_sync), appColors.textSecondary)
        }
        if (state.activeLoaded && state.activeTracks.isEmpty()) {
            Textview(stringResource(R.string.cj_no_active_cranes), size = font_14, color = appColors.textSecondary, modifier = Modifier.fillMaxWidth().padding(top = dimen_40), textAlign = TextAlign.Center)
        }
        state.activeTracks.forEach { t ->
            GlassCard(contentPadding = dimen_12) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CraneImage(path = t.crane?.primary_photo, modifier = Modifier.size(dimen_56))
                    Spacer(modifier = Modifier.width(dimen_12))
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(t.crane?.title.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
                        Textview(t.booking_code.orEmpty(), size = font_10, color = appColors.textSecondary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Textview(t.operator?.full_name.orEmpty(), size = font_11, color = appColors.textSecondary)
                            t.operator?.rating?.let {
                                Icon(Icons.Default.Star, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(dimen_12))
                                Textview(String.format(Locale.US, "%.1f", it), size = font_10, color = appColors.textSecondary)
                            }
                        }
                    }
                    BookingTag(t.tag.orEmpty(), SuccessGreen)
                }
                Spacer(modifier = Modifier.height(dimen_8))
                Row(horizontalArrangement = Arrangement.spacedBy(dimen_8)) {
                    Column(modifier = Modifier.weight(1f).clip(RoundedCornerShape(dimen_10)).background(appColors.subtleFill).padding(dimen_8)) {
                        Textview(stringResource(R.string.bk_distance).uppercase(), size = font_10, color = appColors.textSecondary)
                        Textview(if (t.distance_km != null) stringResource(R.string.cj_km, String.format(Locale.US, "%.1f", t.distance_km)) else "–", size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f).clip(RoundedCornerShape(dimen_10)).background(appColors.subtleFill).padding(dimen_8)) {
                        Textview(stringResource(R.string.bk_eta).uppercase(), size = font_10, color = appColors.textSecondary)
                        Textview(if (t.eta_minutes != null) stringResource(R.string.bk_eta_min, t.eta_minutes) else "–", size = font_13, color = appColors.primaryText, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(dimen_8))
                CommonGradientButton(
                    text = stringResource(R.string.cj_view_live_tracking),
                    onClick = { t.booking_id?.let { vm?.onIntent(BookingIntent.OpenBooking(it)) } },
                    height = dimen_40, textSize = font_12
                )
            }
        }
        Spacer(modifier = Modifier.height(dimen_8))
    }
}

/** "₹8,000 paid of ₹40,000" with a bar, then advance paid and balance still to pay */
@Composable
private fun PaymentProgressCard(p: com.cranezo.ui.booking.model.BookingPayment?) {
    val total = p?.final_total ?: return
    if (total <= 0.0) return
    val paid = (p.paid_amount ?: 0.0).coerceAtLeast(0.0)
    val balance = (p.balance_due ?: (total - paid)).coerceAtLeast(0.0)
    val fraction = (paid / total).toFloat().coerceIn(0f, 1f)
    val green = SuccessGreen
    GlassCard(contentPadding = dimen_14) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Textview(stringResource(R.string.pp_title), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (balance <= 0.0) BookingTag(stringResource(R.string.pp_fully_paid), green)
        }
        Spacer(modifier = Modifier.height(dimen_8))
        Textview(stringResource(R.string.pp_paid_of, rupees(paid), rupees(total)), size = font_13, color = appColors.textSecondary)
        Spacer(modifier = Modifier.height(dimen_8))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimen_10)
                .clip(RoundedCornerShape(dimen_8))
                .background(appColors.subtleFill)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(dimen_10)
                    .clip(RoundedCornerShape(dimen_8))
                    .background(green)
            )
        }
        Spacer(modifier = Modifier.height(dimen_10))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(dimen_8).clip(CircleShape).background(green))
            Textview("  " + stringResource(R.string.pp_advance_paid), size = font_12, color = appColors.textSecondary, modifier = Modifier.weight(1f))
            Textview(rupees(paid), size = font_13, color = green, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(dimen_4))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(dimen_8).clip(CircleShape).background(AmberPrimary))
            Textview("  " + stringResource(R.string.pp_balance), size = font_12, color = appColors.textSecondary, modifier = Modifier.weight(1f))
            Textview(rupees(balance), size = font_13, color = AmberPrimary, fontWeight = FontWeight.Bold)
        }
    }
}
