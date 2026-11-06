package com.cranezo.ui.ownerjobs

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.IntrinsicSize
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cranezo.ui.components.ShimmerImage
import com.cranezo.R
import com.cranezo.core.composecore.BaseVMComposeActivity
import com.cranezo.ui.booking.components.BookingScaffold
import com.cranezo.ui.booking.components.BookingTag
import com.cranezo.ui.booking.components.PriceLine
import com.cranezo.ui.booking.components.FixedHoursCard
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.Textview
import com.cranezo.ui.cranes.components.CraneImage
import com.cranezo.ui.mainhome.components.IconBadge
import com.cranezo.ui.mainhome.components.InfoBlue
import com.cranezo.ui.mainhome.components.SuccessGreen
import com.cranezo.ui.operator.model.JobCrane
import com.cranezo.ui.ownerjobs.model.AdvanceDue
import com.cranezo.ui.ownerjobs.model.AssignedOperator
import com.cranezo.ui.ownerjobs.model.Monitoring
import com.cranezo.ui.ownerjobs.model.OwnerCompletion
import com.cranezo.ui.ownerjobs.model.OwnerEarningsSummaryBlock
import com.cranezo.ui.ownerjobs.model.OwnerJob
import com.cranezo.ui.ownerjobs.model.OwnerJobDetailIntent
import com.cranezo.ui.ownerjobs.model.OwnerJobDetailState
import com.cranezo.ui.ownerjobs.model.OwnerLocation
import com.cranezo.ui.ownerjobs.model.TimelineStep
import com.cranezo.ui.theme.*
import com.cranezo.utils.fileUrl
import com.cranezo.utils.formatSeconds
import com.cranezo.utils.formatServerDate
import com.cranezo.utils.formatServerDateTime
import com.cranezo.utils.formatServerTime
import com.cranezo.utils.rupees
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.util.Locale

/** Hosts one job of the owner: Job Details, View Live Location and View Job Evidence. */
@AndroidEntryPoint
class OwnerJobActivity :
    BaseVMComposeActivity<OwnerJobDetailViewModel>(OwnerJobDetailViewModel::class.java) {

    companion object {
        private const val EXTRA_ID = "booking_id"
        private const val EXTRA_LIVE = "live"

        fun intent(context: Context, bookingId: String, live: Boolean = false): Intent =
            Intent(context, OwnerJobActivity::class.java).putExtra(EXTRA_ID, bookingId).putExtra(EXTRA_LIVE, live)
    }

    @Composable
    override fun Content(vm: OwnerJobDetailViewModel) {
        var screen by rememberSaveable { mutableStateOf(if (intent.getBooleanExtra(EXTRA_LIVE, false)) "live" else "details") }
        val leave: () -> Unit = { if (screen == "details") finish() else screen = "details" }
        BackHandler { leave() }

        LaunchedEffect(Unit) { vm.onIntent(OwnerJobDetailIntent.Load(intent.getStringExtra(EXTRA_ID).orEmpty())) }

        when (screen) {
            "live" -> LiveLocationScreen(vm, leave)
            "evidence" -> EvidenceScreen(vm, leave)
            else -> OwnerJobDetailsScreen(vm, onBack = leave, onLive = { screen = "live" }, onEvidence = { screen = "evidence" })
        }
    }
}

private val previewDetail = OwnerJob(
    booking_id = "1", booking_code = "CRZ10245", tab = "upcoming", tab_label = "Upcoming",
    crane = JobCrane(title = "50 Ton Mobile Crane", model_name = "Liebherr LTM 1050", crane_code = "CRN001", capacity_name = "50 Ton"),
    job_location = OwnerLocation(name = "OMR, Sholinganallur", address = "Chennai, Tamil Nadu", distance_km = 12.0),
    duration_label = "8 Hours", approx_earnings = 40000.0,
    assigned_operator = AssignedOperator(full_name = "Rajesh Kumar", role_label = "50 Ton Crane Operator", tag = "Assigned"),
    timeline = listOf(TimelineStep("a", "Request Received", "2025-09-15T05:50:00.000Z", "done"), TimelineStep("b", "Operator Notified", null, "current"), TimelineStep("c", "Working", null, "pending")),
    can_view_live_location = true
)

// ---------------------------------------------------------------------------------------------
// Job Details: upcoming, active (monitoring) and completed
// ---------------------------------------------------------------------------------------------

@Composable
private fun Tile(label: String, value: String, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier.fillMaxHeight(), contentPadding = dimen_10) {
        Textview(label, size = font_10, color = appColors.textSecondary)
        Textview(value, size = font_12, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, heightDp = 1000)
@Composable
fun OwnerJobDetailsScreen(
    vm: OwnerJobDetailViewModel? = null,
    onBack: () -> Unit = {},
    onLive: () -> Unit = {},
    onEvidence: () -> Unit = {}
) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value ?: OwnerJobDetailState(job = previewDetail, loaded = true)
    val job = state.job
    val context = LocalContext.current
    var pickOperator by remember { mutableStateOf(false) }
    // after picking an operator, the payment settle screen opens by itself
    var assigning by remember { mutableStateOf(false) }
    val assignedName = job?.assigned_operator?.full_name
    LaunchedEffect(assignedName) {
        val id = job?.booking_id
        if (assigning && assignedName != null && id != null) {
            assigning = false
            context.startActivity(com.cranezo.ui.finance.FinanceActivity.intent(context, com.cranezo.ui.finance.FinanceRoutes.COMMISSION, id))
        }
    }
    // back from the payment settle screen (or anywhere): show the latest
    androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) { vm?.onIntent(OwnerJobDetailIntent.Poll) }
    // a push about a job, request or payment: show the change right away
    com.cranezo.push.OnPush { vm?.onIntent(OwnerJobDetailIntent.Poll) }
    // the customer's advance countdown re-syncs every 15 seconds
    LaunchedEffect(job?.advance_due?.due_at) {
        while (job?.advance_due != null) {
            delay(15_000)
            vm?.onIntent(OwnerJobDetailIntent.Poll)
        }
    }

    // An active job is re-synced every 30 seconds
    LaunchedEffect(job?.tab) {
        while (job?.tab == "active") {
            delay(30_000)
            vm?.onIntent(OwnerJobDetailIntent.Poll)
        }
    }

    if (pickOperator) {
        LaunchedEffect(Unit) { vm?.onIntent(OwnerJobDetailIntent.LoadOperators) }
    }

    BookingScaffold(
        onBack = onBack,
        title = stringResource(if (job?.tab == "active") R.string.op_job_monitoring else R.string.oj_job_details),
        bottom = if (job != null && (job.can_view_live_location == true || job.can_view_evidence == true)) {
            {
                Column(verticalArrangement = Arrangement.spacedBy(dimen_8)) {
                    if (job.can_view_live_location == true) {
                        CommonGradientButton(text = stringResource(R.string.oj_view_live_location), onClick = onLive)
                    }
                    if (job.can_view_evidence == true) {
                        CommonGradientButton(
                            text = stringResource(R.string.oj_view_evidence), onClick = onEvidence,
                            gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = appColors.textPrimary
                        )
                    }
                }
            }
        } else null
    ) {
        if (job == null) return@BookingScaffold
        val location = job.job_location ?: job.location
        val completed = job.tab == "completed"

        Row(verticalAlignment = Alignment.CenterVertically) {
            BookingTag(
                job.tab_label ?: job.status_label.orEmpty(),
                when (job.tab) { "completed" -> SuccessGreen; "active" -> SuccessGreen; else -> AmberPrimary }
            )
            Spacer(modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Textview(stringResource(R.string.op_booking_code, job.booking_code.orEmpty()), size = font_12, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
                Textview(
                    when {
                        completed -> stringResource(R.string.oj_completed_on, formatServerDate(job.completed_at))
                        job.tab == "active" -> stringResource(R.string.oj_started_at, formatServerTime(job.monitoring?.start_time ?: job.started_at))
                        else -> stringResource(R.string.oj_accepted_on, formatServerDate(job.accepted_at))
                    },
                    size = font_10, color = appColors.textSecondary
                )
            }
        }

        GlassCard(contentPadding = dimen_12) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CraneImage(path = job.crane?.primary_photo, modifier = Modifier.size(dimen_70))
                Spacer(modifier = Modifier.width(dimen_12))
                Column {
                    Textview(job.crane?.title.orEmpty(), size = font_15, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                    Textview(job.crane?.model_name.orEmpty(), size = font_11, color = appColors.textSecondary)
                    if (!job.crane?.crane_code.isNullOrBlank()) BookingTag(job.crane?.crane_code.orEmpty(), InfoBlue)
                    Textview(
                        listOfNotNull(job.crane?.capacity_name, job.crane?.crane_type_name, job.crane?.fuel_type_name).joinToString("  •  "),
                        size = font_10, color = appColors.textSecondary
                    )
                }
            }
        }

        GlassCard(contentPadding = dimen_12) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(icon = Icons.Default.LocationOn, accent = InfoBlue, size = dimen_36)
                Spacer(modifier = Modifier.width(dimen_10))
                Column(modifier = Modifier.weight(1f)) {
                    Textview(stringResource(R.string.op_job_site).uppercase(), size = font_10, color = appColors.textSecondary)
                    Textview(location?.name ?: location?.address.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                    Textview(location?.address.orEmpty(), size = font_11, color = appColors.textSecondary)
                }
                if (location?.latitude != null) {
                    Textview(
                        stringResource(R.string.oj_view_on_map), size = font_11, color = appColors.primaryText, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { openMapAt(context, location.latitude, location.longitude) }
                    )
                }
            }
        }

        if (completed) {
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(dimen_8)) {
                Tile(stringResource(R.string.oj_scheduled), formatServerDateTime(job.completion?.scheduled_at ?: job.schedule?.start_at), Modifier.weight(1f))
                Tile(stringResource(R.string.oj_actual_duration), formatSeconds(job.completion?.actual_duration_seconds ?: job.actual_duration_seconds), Modifier.weight(1f))
                if ((job.completion?.extra_seconds ?: 0L) > 0) Tile(stringResource(R.string.op_extra_time), formatSeconds(job.completion?.extra_seconds), Modifier.weight(1f))
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(dimen_8)) {
                Tile(stringResource(R.string.bk_required_date_time), formatServerDateTime(job.schedule?.start_at), Modifier.weight(1f))
                Tile(stringResource(R.string.bk_duration), job.duration?.label ?: job.duration_label.orEmpty(), Modifier.weight(1f))
                Tile(stringResource(R.string.bk_distance), stringResource(R.string.bk_km_away, String.format(Locale.US, "%.0f", location?.distance_km ?: 0.0)), Modifier.weight(1f))
            }
        }

        // the customer still has to pay the advance: same countdown the customer sees
        job.advance_due?.let { AdvanceCountdown(it) }

        // Operator: with Call, and a way to assign one before the journey starts
        val operator = job.assigned_operator
        GlassCard(contentPadding = dimen_12) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(icon = Icons.Default.Person, accent = AmberPrimary, size = dimen_40)
                Spacer(modifier = Modifier.width(dimen_10))
                Column(modifier = Modifier.weight(1f)) {
                    Textview(stringResource(R.string.oj_assigned_operator).uppercase(), size = font_10, color = appColors.textSecondary)
                    Textview(operator?.full_name ?: stringResource(R.string.oj_not_assigned), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                    if (!operator?.role_label.isNullOrBlank()) Textview(operator?.role_label.orEmpty(), size = font_11, color = appColors.textSecondary)
                }
                operator?.tag?.let { BookingTag(it, if (it == "Assigned") SuccessGreen else AmberPrimary) }
                if (operator?.mobile_number != null) {
                    Spacer(modifier = Modifier.width(dimen_8))
                    Icon(Icons.Default.Call, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(dimen_22).clickable { openDialer(context, operator.country_code, operator.mobile_number) })
                }
            }
            if (job.tab == "upcoming" && !pickOperator) {
                Spacer(modifier = Modifier.height(dimen_8))
                CommonGradientButton(
                    text = stringResource(if (operator == null) R.string.oj_assign_operator else R.string.oj_change_operator),
                    onClick = { pickOperator = true }, height = dimen_40, textSize = font_12,
                    gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = AmberPrimary
                )
            }
            // Choose the operator for THIS job, right here on the page (no popup)
            if (job.tab == "upcoming" && pickOperator) {
                Spacer(modifier = Modifier.height(dimen_10))
                OperatorChooser(
                    suggested = job.suggested_operator,
                    operators = state.operators,
                    loaded = state.operatorsLoaded,
                    onPick = { id, byCode ->
                        assigning = true
                        vm?.onIntent(OwnerJobDetailIntent.AssignOperator(id, byCode))
                        pickOperator = false
                    },
                    onCancel = { pickOperator = false }
                )
            }
            // booking for someone else: the person at the site
            job.site_contact?.let { c ->
                Spacer(modifier = Modifier.height(dimen_8))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(stringResource(R.string.rq_site_contact).uppercase(), size = font_10, color = appColors.textSecondary)
                        Textview(listOfNotNull(c.name, c.phone).joinToString(" · "), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
                    }
                    if (!c.phone.isNullOrBlank()) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(dimen_22).clickable { openDialer(context, "+91", c.phone) })
                    }
                }
            }
            // what the operator gets on this job and whether it is paid; the settle screen has the full bill
            if (operator != null && job.booking_id != null) {
                val c = job.operator_commission
                Spacer(modifier = Modifier.height(dimen_10))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(stringResource(R.string.ow_operator_payment).uppercase(), size = font_10, color = appColors.textSecondary)
                        Textview(
                            if (c?.isSet == true) listOfNotNull(rupees(c.amount), c.type_label).joinToString(" · ") else stringResource(R.string.ow_not_set),
                            size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (c?.isSet == true) {
                        BookingTag(
                            stringResource(if (c.settlement_status == "settled") R.string.ow_paid else R.string.ow_pending),
                            if (c.settlement_status == "settled") SuccessGreen else AmberPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(dimen_8))
                CommonGradientButton(
                    text = stringResource(R.string.ow_view_settle),
                    onClick = {
                        context.startActivity(
                            com.cranezo.ui.finance.FinanceActivity.intent(context, com.cranezo.ui.finance.FinanceRoutes.COMMISSION, job.booking_id)
                        )
                    },
                    height = dimen_40, textSize = font_12,
                    gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = AmberPrimary
                )
            }
        }

        if (job.tab == "active") MonitoringBlock(job, job.monitoring)
        else if (completed) CompletionBlock(job.completion)
        else GlassCard(contentPadding = dimen_12) {
            PriceLine(stringResource(R.string.op_estimated_earnings), rupees(job.approx_earnings), bold = true)
        }

        if (!job.timeline.isNullOrEmpty()) {
            GlassCard(contentPadding = dimen_14) {
                Textview(stringResource(if (completed) R.string.oj_job_timeline else R.string.oj_job_status), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(dimen_8))
                job.timeline.forEach { step ->
                    Row(modifier = Modifier.padding(vertical = dimen_5), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (step.state == "pending") Icons.Default.RadioButtonUnchecked else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = when (step.state) { "done" -> SuccessGreen; "current" -> AmberPrimary; else -> appColors.textHint },
                            modifier = Modifier.size(dimen_18)
                        )
                        Spacer(modifier = Modifier.width(dimen_10))
                        Textview(step.label.orEmpty(), size = font_12, color = if (step.state == "pending") appColors.textSecondary else appColors.textPrimary, modifier = Modifier.weight(1f))
                        Textview(if (step.at != null) formatServerDateTime(step.at) else "–", size = font_10, color = appColors.textSecondary)
                    }
                }
            }
        }
        // every payment of this job (advance, final, failed attempts)
        TransactionHistory(job.transactions)
        if (job.can_download_invoice == true) {
            CommonGradientButton(text = stringResource(R.string.oj_download_invoice), onClick = {})
        }
        Spacer(modifier = Modifier.height(dimen_8))
    }
}

@Composable
private fun MonitoringBlock(job: OwnerJob, monitoring: Monitoring?) {
    // Fixed hours: the booked window and time left, the same card the customer and operator see
    FixedHoursCard(job.schedule?.start_at, job.schedule?.end_at, job.duration_label ?: job.duration?.label)
    if ((monitoring?.extra_seconds ?: 0L) > 0) {
        GlassCard(contentPadding = dimen_12) {
            PriceLine(stringResource(R.string.op_extra_time), formatSeconds(monitoring?.extra_seconds))
        }
    }
    if (!monitoring?.message.isNullOrBlank()) {
        GlassCard(contentPadding = dimen_12) {
            Textview(monitoring?.message.orEmpty(), size = font_12, color = appColors.textSecondary)
        }
    }
}

@Composable
private fun CompletionBlock(completion: OwnerCompletion?) {
    val e: OwnerEarningsSummaryBlock = completion?.earnings_summary ?: return
    GlassCard(contentPadding = dimen_14) {
        Textview(stringResource(R.string.op_earnings_summary), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(dimen_6))
        PriceLine(stringResource(R.string.op_base_amount_plain), rupees(e.base_amount))
        if ((e.extra_time_amount ?: 0.0) > 0) PriceLine(stringResource(R.string.op_extra_amount), rupees(e.extra_time_amount))
        PriceLine(stringResource(R.string.op_final_amount), rupees(e.final_amount), bold = true)
        if (e.platform_fee != null) {
            PriceLine(stringResource(R.string.oe_platform_fee, (e.platform_fee_percent ?: 0.0).toInt()), rupees(e.platform_fee))
            PriceLine(stringResource(R.string.oj_you_receive), rupees(e.net_amount), valueColor = SuccessGreen)
        }
        completion.settlement?.label?.let {
            Spacer(modifier = Modifier.height(dimen_4))
            BookingTag(it, if (it == "Paid") SuccessGreen else AmberPrimary)
        }
    }
}

// ---------------------------------------------------------------------------------------------
// View Live Location: polled every 5 seconds. No map SDK yet, so the position opens in the maps app.
// ---------------------------------------------------------------------------------------------

@Composable
fun LiveLocationScreen(vm: OwnerJobDetailViewModel?, onBack: () -> Unit) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value ?: OwnerJobDetailState()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        while (true) {
            vm?.onIntent(OwnerJobDetailIntent.PollTrack)
            delay(5000)
        }
    }
    val track = state.track

    BookingScaffold(onBack = onBack, title = stringResource(R.string.oj_view_live_location)) {
        if (track == null) return@BookingScaffold
        GlassCard(contentPadding = dimen_14) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CraneImage(path = track.crane?.primary_photo, modifier = Modifier.size(dimen_56))
                Spacer(modifier = Modifier.width(dimen_12))
                Column(modifier = Modifier.weight(1f)) {
                    Textview(track.crane?.title.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                    BookingTag(track.status_label.orEmpty(), SuccessGreen)
                }
                Column(horizontalAlignment = Alignment.End) {
                    track.eta_minutes?.let { Textview(stringResource(R.string.bk_eta_min, it), size = font_20, color = appColors.primaryText, fontWeight = FontWeight.Bold) }
                    track.distance_km?.let { Textview(stringResource(R.string.bk_km_away, String.format(Locale.US, "%.1f", it)), size = font_11, color = appColors.textSecondary) }
                }
            }
            if (!track.status_message.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(dimen_8))
                Textview(track.status_message, size = font_12, color = appColors.textSecondary)
            }
        }
        GlassCard(contentPadding = dimen_12) {
            Textview(stringResource(R.string.op_job_site).uppercase(), size = font_10, color = appColors.textSecondary)
            Textview(track.site?.name ?: track.site?.address.orEmpty(), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
            val live = track.crane_location?.is_live == true
            Spacer(modifier = Modifier.height(dimen_6))
            Textview(
                stringResource(if (live) R.string.oj_live_position else R.string.oj_last_known_position), size = font_11,
                color = if (live) SuccessGreen else AmberPrimary
            )
        }
        track.operator?.let { operator ->
            GlassCard(contentPadding = dimen_12) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(stringResource(R.string.bk_operator).uppercase(), size = font_10, color = appColors.textSecondary)
                        Textview(operator.full_name ?: stringResource(R.string.bk_operator), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                    }
                    if (operator.mobile_number != null) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(dimen_24).clickable { openDialer(context, operator.country_code, operator.mobile_number) })
                    }
                }
            }
        }
        val loc = track.crane_location
        if (loc?.latitude != null) {
            CommonGradientButton(text = stringResource(R.string.bk_open_in_maps), onClick = { openMapAt(context, loc.latitude, loc.longitude) })
        }
    }
}

// ---------------------------------------------------------------------------------------------
// View Job Evidence
// ---------------------------------------------------------------------------------------------

@Composable
private fun PhotoGrid(title: String, photos: List<com.cranezo.ui.operator.model.SitePhoto>) {
    if (photos.isEmpty()) return
    val context = LocalContext.current
    GlassCard(contentPadding = dimen_12) {
        Textview(title, size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(dimen_8))
        photos.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(dimen_8), modifier = Modifier.padding(bottom = dimen_8)) {
                row.forEach { photo ->
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(dimen_88).clip(RoundedCornerShape(dimen_10))
                                .clickable { com.cranezo.ui.cranes.openFile(context, photo.photo_url) }
                        ) {
                            ShimmerImage(model = fileUrl(photo.photo_url), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize())
                        }
                        if (photo.captured_at != null) Textview(formatServerDateTime(photo.captured_at), size = font_10, color = appColors.textSecondary, maxLines = 2)
                    }
                }
                repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
fun EvidenceScreen(vm: OwnerJobDetailViewModel?, onBack: () -> Unit) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value ?: OwnerJobDetailState()
    LaunchedEffect(Unit) { vm?.onIntent(OwnerJobDetailIntent.LoadEvidence) }
    val evidence = state.evidence

    BookingScaffold(onBack = onBack, title = stringResource(R.string.oj_view_evidence)) {
        if (evidence == null) {
            if (state.evidenceLoaded) Textview(stringResource(R.string.oj_no_evidence), size = font_14, color = appColors.textSecondary, modifier = Modifier.padding(top = dimen_40))
            return@BookingScaffold
        }
        PhotoGrid(stringResource(R.string.oj_site_photos), evidence.site_photos.orEmpty())
        PhotoGrid(stringResource(R.string.op_final_photos), evidence.final_photos.orEmpty())
        if (!evidence.job_notes.isNullOrBlank()) {
            GlassCard(contentPadding = dimen_12) {
                Textview(stringResource(R.string.op_job_notes), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                Textview(evidence.job_notes, size = font_12, color = appColors.textSecondary)
            }
        }
        evidence.arrival?.let { arrival ->
            if (arrival.arrived_at != null) {
                GlassCard(contentPadding = dimen_12) {
                    Textview(stringResource(R.string.oj_arrival), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                    Textview(stringResource(R.string.oj_arrived_at, formatServerDateTime(arrival.arrived_at)), size = font_12, color = appColors.textSecondary)
                    if (arrival.confirmed_at != null) {
                        Textview(stringResource(R.string.oj_confirmed_by, formatServerDateTime(arrival.confirmed_at), arrival.confirmed_by.orEmpty()), size = font_12, color = SuccessGreen)
                    }
                }
            }
        }
        if (!evidence.issues.isNullOrEmpty()) {
            Textview(stringResource(R.string.oj_issues), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            evidence.issues.forEach { issue ->
                GlassCard(contentPadding = dimen_12, borderColor = if (issue.kind == "emergency") Red else androidx.compose.ui.graphics.Color.Unspecified) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BookingTag((issue.kind ?: "").uppercase(), if (issue.kind == "emergency") Red else AmberPrimary)
                        Spacer(modifier = Modifier.weight(1f))
                        Textview(formatServerDateTime(issue.reported_at), size = font_10, color = appColors.textSecondary)
                    }
                    if (!issue.breakdown_type_name.isNullOrBlank()) Textview(issue.breakdown_type_name, size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
                    if (!issue.description.isNullOrBlank()) Textview(issue.description, size = font_12, color = appColors.textSecondary)
                    issue.photos.orEmpty().forEach { path ->
                        ShimmerImage(model = fileUrl(path), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.padding(top = dimen_6).fillMaxWidth().height(dimen_100).clip(RoundedCornerShape(dimen_10)))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(dimen_8))
    }
}

/**
 * Choose the operator for this one job: the crane's usual operator first, then the owner's operators,
 * or any operator by their ID (OP...) - an outside operator works this job only, then is free again.
 */
@Composable
private fun OperatorChooser(
    suggested: com.cranezo.ui.ownerjobs.model.SuggestedOperator?,
    operators: List<com.cranezo.ui.cranes.model.Operator>,
    loaded: Boolean,
    onPick: (String, Boolean) -> Unit,
    onCancel: () -> Unit
) {
    var code by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(dimen_8)) {
        Textview(stringResource(R.string.rq_operator_by_id), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(dimen_8)) {
            com.cranezo.ui.components.GlassInputField(
                label = "", value = code, onValueChange = { code = it.uppercase().filter { c -> c.isLetterOrDigit() }.take(14) },
                placeholder = "OP497309", modifier = Modifier.weight(1f)
            )
            CommonGradientButton(
                text = stringResource(R.string.rq_assign), modifier = Modifier.width(dimen_100), height = dimen_52,
                enabled = code.length >= 4, onClick = { onPick(code, true) }
            )
        }
        Textview(stringResource(R.string.rq_operator_by_id_hint), size = font_11, color = appColors.textSecondary)

        val list = buildList {
            suggested?.let { s -> s.id?.let { id -> add(Triple(id, s.full_name.orEmpty(), s.member_id.orEmpty()) to true) } }
            operators.filter { it.id != suggested?.id }.forEach { o -> o.id?.let { add(Triple(it, o.full_name.orEmpty(), o.member_id.orEmpty()) to false) } }
        }
        if (list.isNotEmpty()) Textview(stringResource(R.string.rq_your_operators), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
        if (loaded && list.isEmpty()) Textview(stringResource(R.string.oj_no_operators), size = font_12, color = appColors.textSecondary)
        list.forEach { (o, isSuggested) ->
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(dimen_12)).background(appColors.subtleFill)
                    .clickable { onPick(o.first, false) }.padding(dimen_10),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBadge(icon = Icons.Default.Person, accent = AmberPrimary, size = dimen_32)
                Spacer(modifier = Modifier.width(dimen_10))
                Column(modifier = Modifier.weight(1f)) {
                    Textview(o.second, size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
                    Textview(o.third, size = font_11, color = appColors.textSecondary)
                }
                if (isSuggested) BookingTag(stringResource(R.string.rq_usual_operator), SuccessGreen)
            }
        }
        Textview(
            stringResource(R.string.fin_cancel), size = font_13, color = appColors.textSecondary,
            modifier = Modifier.clickable { onCancel() }.padding(vertical = dimen_6)
        )
    }
}

/** Owner: the customer has an accepted booking but has not paid the advance yet */
@Composable
private fun AdvanceCountdown(due: AdvanceDue) {
    var left by remember(due.due_at, due.seconds_left) { mutableStateOf(due.seconds_left ?: 0L) }
    LaunchedEffect(due.due_at, due.seconds_left) {
        while (left > 0) {
            delay(1000)
            left -= 1
        }
    }
    GlassCard(contentPadding = dimen_14, borderColor = AmberPrimary) {
        Textview(stringResource(R.string.ow_advance_wait, rupees(due.amount)), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(dimen_4))
        Textview(String.format(Locale.US, "%02d:%02d", left / 60, left % 60), size = font_26, color = AmberPrimary, fontWeight = FontWeight.Bold)
        Textview(stringResource(R.string.ow_time_left, String.format(Locale.US, "%02d:%02d", left / 60, left % 60)), size = font_12, color = appColors.textSecondary)
        if (due.extended == true) Textview(stringResource(R.string.ow_extended), size = font_12, color = AmberPrimary, modifier = Modifier.padding(top = dimen_4))
    }
}

/** Job details: every payment of the job */
@Composable
fun TransactionHistory(list: List<com.cranezo.ui.finance.model.TransactionItem>?) {
    if (list == null) return
    GlassCard(contentPadding = dimen_14) {
        Textview(stringResource(R.string.tx_history), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(dimen_8))
        if (list.isEmpty()) Textview(stringResource(R.string.tx_none), size = font_12, color = appColors.textSecondary)
        list.forEach { t ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = dimen_6), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Textview(t.kind_label ?: t.method_name.orEmpty(), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
                    Textview(listOfNotNull(t.method_name, formatServerDateTime(t.created_at)).joinToString(" · "), size = font_11, color = appColors.textSecondary)
                    (t.gateway_payment_id ?: t.transaction_code)?.let { Textview(it, size = font_10, color = appColors.textHint) }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Textview(rupees(t.amount), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                    BookingTag(t.status_label.orEmpty(), com.cranezo.ui.finance.statusTone(t.status))
                }
            }
        }
    }
}
