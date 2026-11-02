package com.cranezo.ui.ownerjobs

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cranezo.R
import com.cranezo.ui.booking.components.BookingTag
import com.cranezo.ui.booking.model.BookingOperator
import com.cranezo.ui.booking.model.BookingSchedule
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.Textview
import com.cranezo.ui.cranes.components.CraneImage
import com.cranezo.ui.mainhome.components.HomeHeader
import com.cranezo.ui.mainhome.components.InfoBlue
import com.cranezo.ui.mainhome.components.OwnerRequestHost
import com.cranezo.ui.mainhome.components.SuccessGreen
import com.cranezo.ui.operator.model.JobCrane
import com.cranezo.ui.operator.model.ProgressStep
import com.cranezo.ui.ownerjobs.model.OwnerJob
import com.cranezo.ui.ownerjobs.model.OwnerJobCounts
import com.cranezo.ui.ownerjobs.model.OwnerJobsIntent
import com.cranezo.ui.ownerjobs.model.OwnerJobsState
import com.cranezo.ui.ownerjobs.model.OwnerLocation
import com.cranezo.ui.theme.*
import com.cranezo.utils.compactMinutes
import com.cranezo.utils.formatClock
import com.cranezo.utils.formatSeconds
import com.cranezo.utils.formatServerDateTime
import com.cranezo.utils.formatServerTime
import com.cranezo.utils.rupees
import kotlinx.coroutines.delay
import java.util.Locale

private val previewJob = OwnerJob(
    booking_id = "1", booking_code = "CRZ-1", tab = "new_requests", tab_label = "New Requests",
    crane = JobCrane(crane_code = "CRN001", title = "50 Ton Mobile Crane", model_name = "Liebherr LTM 1050"),
    location = OwnerLocation(name = "OMR", address = "OMR, Sholinganallur, Chennai, TN", distance_km = 12.0),
    schedule = BookingSchedule("scheduled", "2025-09-15T08:30:00.000Z"), duration_label = "8 Hours", approx_earnings = 40000.0,
    requested_ago_minutes = 2, seconds_left = 540, can_respond = true,
    operator = BookingOperator(full_name = "Rajesh Kumar"), operator_label = "Assigned"
)

fun openDialer(context: Context, countryCode: String?, number: String?) {
    if (number.isNullOrBlank()) return
    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${countryCode.orEmpty()}$number")))
}

fun openMapAt(context: Context, latitude: Double?, longitude: Double?) {
    if (latitude == null || longitude == null) return
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude"))) }
}

@Composable
private fun agoText(minutes: Int?): String =
    if ((minutes ?: 0) <= 0) stringResource(R.string.oj_just_now) else stringResource(R.string.oj_ago, compactMinutes(minutes))

@Composable
private fun scheduleText(job: OwnerJob): String =
    if (job.schedule?.start_type == "now") stringResource(R.string.bk_now) else formatServerDateTime(job.schedule?.start_at)

// ---------------------------------------------------------------------------------------------
// Jobs tab
// ---------------------------------------------------------------------------------------------

@Preview(showBackground = true, heightDp = 900)
@Composable
fun OwnerJobsTab(
    vm: OwnerJobsViewModel? = null,
    userName: String = "",
    onOpenJob: (String) -> Unit = {},
    onLiveLocation: (String) -> Unit = {}
) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value
        ?: OwnerJobsState(counts = OwnerJobCounts(1, 2, 1, 0, 0), jobs = listOf(previewJob), total = 1, loaded = true)
    val onIntent: (OwnerJobsIntent) -> Unit = { vm?.onIntent(it) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { onIntent(OwnerJobsIntent.Refresh) }
    val context = LocalContext.current

    OwnerRequestHost(
        request = state.accepted,
        accepted = true,
        onAccept = {},
        onDecline = {},
        onDismiss = { onIntent(OwnerJobsIntent.DismissAccepted) }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimen_20, vertical = dimen_16),
        verticalArrangement = Arrangement.spacedBy(dimen_12)
    ) {
        HomeHeader(name = userName, subtitle = stringResource(R.string.home_owner_subtitle))
        Column {
            Textview(stringResource(R.string.op_jobs_title), size = font_22, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            Textview(stringResource(R.string.oj_jobs_subtitle), size = font_11, color = appColors.textSecondary)
        }

        // Tabs with their counts
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(dimen_8)) {
            val counts = state.counts
            listOf(
                Triple("new_requests", R.string.oj_tab_new_requests, counts?.new_requests),
                Triple("upcoming", R.string.oj_tab_upcoming, counts?.upcoming),
                Triple("active", R.string.oj_tab_active, counts?.active),
                Triple("completed", R.string.op_tab_completed, counts?.completed)
            ).forEach { (key, label, count) ->
                val selected = key == state.tab
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(dimen_16))
                        .background(if (selected) AmberPrimary.copy(alpha = 0.18f) else appColors.subtleFill)
                        .clickable { onIntent(OwnerJobsIntent.SelectTab(key)) }
                        .padding(horizontal = dimen_12, vertical = dimen_6),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Textview(stringResource(label), size = font_11, color = if (selected) AmberPrimary else appColors.textPrimary, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, maxLines = 1)
                    if (count != null) {
                        Spacer(modifier = Modifier.width(dimen_6))
                        Box(modifier = Modifier.size(dimen_16).clip(CircleShape).background(if (key == "new_requests" && count > 0) Red else AmberPrimary.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                            Textview(count.toString(), size = font_8, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Textview(
                text = stringResource(R.string.oj_n_title, state.total, stringResource(tabTitle(state.tab))),
                size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(dimen_10))
                    .background(appColors.subtleFill)
                    .clickable { onIntent(OwnerJobsIntent.ToggleSort) }
                    .padding(horizontal = dimen_10, vertical = dimen_4),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.SwapVert, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(dimen_14))
                Textview(stringResource(if (state.newestFirst) R.string.oj_sort_newest else R.string.oj_sort_oldest), size = font_10, color = appColors.textSecondary)
            }
        }

        if (state.loaded && state.jobs.isEmpty()) {
            Textview(stringResource(R.string.op_no_jobs), size = font_14, color = appColors.textSecondary, modifier = Modifier.fillMaxWidth().padding(top = dimen_40), textAlign = TextAlign.Center)
        }

        state.jobs.forEach { job ->
            OwnerJobCard(
                job = job,
                onAccept = { job.booking_id?.let { onIntent(OwnerJobsIntent.Accept(it)) } },
                onDecline = { job.booking_id?.let { onIntent(OwnerJobsIntent.Decline(it)) } },
                onExpired = { onIntent(OwnerJobsIntent.Refresh) },
                onView = { job.booking_id?.let(onOpenJob) },
                onLive = { job.booking_id?.let(onLiveLocation) },
                onCall = { openDialer(context, job.operator?.country_code, job.operator?.mobile_number) }
            )
        }

        if (state.hasMore) {
            LaunchedEffect(state.jobs.size) { onIntent(OwnerJobsIntent.LoadMore) }
        }
        Spacer(modifier = Modifier.height(dimen_8))
    }
}

private fun tabTitle(tab: String): Int = when (tab) {
    "upcoming" -> R.string.oj_tab_upcoming
    "active" -> R.string.oj_tab_active
    "completed" -> R.string.op_tab_completed
    "closed" -> R.string.oj_tab_closed
    else -> R.string.oj_tab_new_requests
}

// ---------------------------------------------------------------------------------------------
// The four card layouts, built from one list item
// ---------------------------------------------------------------------------------------------

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(dimen_12))
        Spacer(modifier = Modifier.width(dimen_6))
        Textview(text, size = font_11, color = appColors.textSecondary, maxLines = 2)
    }
}

@Composable
private fun OperatorRow(job: OwnerJob, onCall: (() -> Unit)?) {
    val operator = job.operator
    Row(verticalAlignment = Alignment.CenterVertically) {
        Textview(
            text = stringResource(R.string.oj_operator_name, operator?.full_name ?: stringResource(R.string.bk_operator)),
            size = font_11, color = appColors.textPrimary, modifier = Modifier.weight(1f), maxLines = 1
        )
        val label = job.operator_label
        if (label != null && job.tab == "upcoming") {
            BookingTag(label, when (label) { "Assigned" -> SuccessGreen; "Not assigned" -> Red; else -> AmberPrimary })
        }
        if (onCall != null && operator?.mobile_number != null) {
            Spacer(modifier = Modifier.width(dimen_8))
            Icon(Icons.Default.Call, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(dimen_20).clickable { onCall() })
        }
    }
}

@Composable
private fun OwnerJobCard(
    job: OwnerJob,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onExpired: () -> Unit,
    onView: () -> Unit,
    onLive: () -> Unit,
    onCall: () -> Unit
) {
    GlassCard(contentPadding = dimen_12) {
        // Header line: tag and the time hint of the tab
        Row(verticalAlignment = Alignment.CenterVertically) {
            when (job.tab) {
                "upcoming" -> BookingTag(stringResource(R.string.oj_tag_upcoming), AmberPrimary)
                "active" -> BookingTag(stringResource(R.string.op_active).uppercase(), SuccessGreen)
                "completed" -> BookingTag(stringResource(R.string.op_tab_completed).uppercase(), InfoBlue)
                "new_requests" -> BookingTag(stringResource(R.string.oj_tag_new_request), Red)
                else -> BookingTag(job.status_label.orEmpty(), appColors.textSecondary)
            }
            Spacer(modifier = Modifier.weight(1f))
            val hint = when (job.tab) {
                "new_requests" -> agoText(job.requested_ago_minutes)
                "upcoming" -> if ((job.starts_in_minutes ?: 0) <= 0) stringResource(R.string.oj_starting_now) else stringResource(R.string.oj_starts_in, compactMinutes(job.starts_in_minutes))
                "active" -> stringResource(R.string.oj_started_ago, compactMinutes(job.started_ago_minutes))
                "completed" -> formatServerDateTime(job.completed_at)
                else -> ""
            }
            Textview(hint, size = font_10, color = if (job.tab == "upcoming") AmberPrimary else appColors.textSecondary)
        }
        Spacer(modifier = Modifier.height(dimen_8))

        Row {
            CraneImage(path = job.crane?.primary_photo, modifier = Modifier.size(dimen_88))
            Spacer(modifier = Modifier.width(dimen_12))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(dimen_2)) {
                Textview(job.crane?.title.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
                Textview(job.crane?.model_name.orEmpty(), size = font_11, color = appColors.textSecondary, maxLines = 1)
                if (job.tab != "new_requests" && !job.crane?.crane_code.isNullOrBlank()) BookingTag(job.crane?.crane_code.orEmpty(), InfoBlue)
                InfoRow(Icons.Default.LocationOn, listOfNotNull(job.location?.name ?: job.location?.address, job.location?.distance_km?.let { stringResource(R.string.bk_km_away, String.format(Locale.US, "%.0f", it)) }).joinToString(" · "))
                if (job.tab != "active") {
                    InfoRow(Icons.Default.CalendarMonth, scheduleText(job))
                    InfoRow(Icons.Default.AccessTime, if (job.tab == "completed") formatSeconds(job.actual_duration_seconds) else job.duration_label.orEmpty())
                }
                when (job.tab) {
                    "new_requests", "upcoming" -> InfoRow(Icons.Default.Payments, stringResource(R.string.oj_approx_earnings_value, rupees(job.approx_earnings)))
                    "completed" -> Textview(stringResource(R.string.oj_final_amount_value, rupees(job.final_amount)), size = font_12, color = SuccessGreen, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (job.tab == "active") {
            Spacer(modifier = Modifier.height(dimen_10))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                job.progress.orEmpty().forEach { step -> ProgressMini(step) }
            }
            Spacer(modifier = Modifier.height(dimen_8))
            OperatorRow(job, onCall)
            Spacer(modifier = Modifier.height(dimen_8))
            JobTimerBox(job)
        } else if (job.tab != "new_requests") {
            Spacer(modifier = Modifier.height(dimen_8))
            OperatorRow(job, null)
        }

        Spacer(modifier = Modifier.height(dimen_10))
        when (job.tab) {
            "new_requests" -> {
                if (job.can_respond == true) {
                    // The request expires on its own: when the countdown ends the list refreshes
                    var left by remember(job.booking_id, job.seconds_left) { mutableIntStateOf(job.seconds_left ?: 0) }
                    LaunchedEffect(job.booking_id, job.seconds_left) {
                        while (left > 0) {
                            delay(1000)
                            left--
                        }
                        if (job.seconds_left != null) onExpired()
                    }
                    if (job.seconds_left != null) {
                        Textview(stringResource(R.string.oj_time_left, "%02d:%02d".format(left / 60, left % 60)), size = font_10, color = AmberPrimary, modifier = Modifier.padding(bottom = dimen_6))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(dimen_12)) {
                        CommonGradientButton(
                            text = stringResource(R.string.bk_decline), modifier = Modifier.weight(1f), onClick = onDecline, height = dimen_36, textSize = font_12,
                            gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = Red
                        )
                        CommonGradientButton(text = stringResource(R.string.bk_accept), modifier = Modifier.weight(1f), onClick = onAccept, height = dimen_36, textSize = font_12)
                    }
                }
            }
            "active" -> {
                CommonGradientButton(
                    text = stringResource(R.string.oj_view_live_location), onClick = onLive, height = dimen_36, textSize = font_12,
                    gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = AmberPrimary
                )
                Spacer(modifier = Modifier.height(dimen_8))
                ViewDetails(onView)
            }
            else -> ViewDetails(onView)
        }
    }
}

@Composable
private fun ViewDetails(onClick: () -> Unit) {
    CommonGradientButton(
        text = stringResource(R.string.oj_view_details), onClick = onClick, height = dimen_36, textSize = font_12,
        gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = appColors.textPrimary
    )
}

@Composable
private fun ProgressMini(step: ProgressStep) {
    val color = if (step.state == "pending" || step.state == null) appColors.subtleBorder else SuccessGreen
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(dimen_10).clip(CircleShape).background(if (step.state == "done") color else Color.Transparent).then(Modifier.background(color.copy(alpha = if (step.state == "done") 1f else 0.4f))))
        Textview(step.label.orEmpty(), size = font_8, color = appColors.textPrimary, maxLines = 1)
        Textview(if (step.at != null) formatServerTime(step.at) else "", size = font_8, color = appColors.textSecondary)
    }
}

/** The "Job in Progress" timer: working time, ticking from the server's value. */
@Composable
fun JobTimerBox(job: OwnerJob) {
    val timers = job.timers
    val receivedAt = remember(timers) { android.os.SystemClock.elapsedRealtime() }
    var elapsed by remember(timers) { androidx.compose.runtime.mutableLongStateOf(0L) }
    val running = timers?.work_started_at != null && timers.work_ended_at == null
    LaunchedEffect(timers) {
        while (running) {
            delay(1000)
            elapsed = (android.os.SystemClock.elapsedRealtime() - receivedAt) / 1000
        }
    }
    val working = (timers?.working_seconds ?: 0L) + if (running && timers?.is_waiting != true) elapsed else 0L
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(dimen_12)).background(appColors.subtleFill).padding(dimen_10),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Textview(stringResource(R.string.oj_job_progress), size = font_10, color = appColors.textSecondary)
            Textview(formatClock(working), size = font_18, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
        }
        BookingTag(stringResource(R.string.oj_job_in_progress), AmberPrimary)
    }
}
