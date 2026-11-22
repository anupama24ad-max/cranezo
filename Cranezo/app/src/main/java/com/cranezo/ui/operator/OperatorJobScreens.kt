package com.cranezo.ui.operator

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cranezo.ui.components.ShimmerImage
import com.cranezo.R
import com.cranezo.ui.booking.components.BookingScaffold
import com.cranezo.ui.booking.components.BookingTag
import com.cranezo.ui.booking.components.FixedHoursCard
import com.cranezo.ui.booking.components.PriceLine
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.GlassInputField
import com.cranezo.ui.components.GlassSelectField
import com.cranezo.ui.components.Textview
import com.cranezo.ui.cranes.components.CraneImage
import com.cranezo.ui.mainhome.components.IconBadge
import com.cranezo.ui.mainhome.components.InfoBlue
import com.cranezo.ui.mainhome.components.SuccessGreen
import com.cranezo.ui.operator.model.Checklist
import com.cranezo.ui.operator.model.ChecklistItem
import com.cranezo.ui.operator.model.JobArrival
import com.cranezo.ui.operator.model.JobCrane
import com.cranezo.ui.operator.model.JobCustomer
import com.cranezo.ui.operator.model.JobSite
import com.cranezo.ui.operator.model.JobSummary
import com.cranezo.ui.operator.model.NextAction
import com.cranezo.ui.operator.model.OperatorJob
import com.cranezo.ui.operator.model.OperatorJobIntent
import com.cranezo.ui.operator.model.OperatorJobState
import com.cranezo.ui.operator.model.StartCheck
import com.cranezo.ui.theme.*
import com.cranezo.utils.fileUrl
import com.cranezo.utils.formatSeconds
import com.cranezo.utils.formatServerDate
import com.cranezo.utils.formatServerDateTime
import com.cranezo.utils.formatServerTime
import com.cranezo.utils.imagePicker.ImageSourceBottomSheet
import com.cranezo.utils.imagePicker.rememberImagePickerController
import com.cranezo.utils.rupees
import kotlinx.coroutines.delay

private val previewJob = OperatorJob(
    booking_id = "1", booking_code = "CRZ10245", next_action = NextAction.ACCEPT_JOB, stage = "awaiting_operator",
    requested_crane = JobCrane(title = "50 Ton Crane", crane_type_name = "Mobile Crane"),
    assigned_crane = JobCrane(crane_code = "CRN001", model_name = "Liebherr LTM 1050", registration_number = "AP XX 1234", capacity_name = "50 Ton", crane_type_name = "Mobile Crane", fuel_type_name = "Diesel"),
    customer = JobCustomer(name = "Sai Constructions"),
    job_site = JobSite(name = "Construction Site", address = "Kakinada, Andhra Pradesh", distance_km = 12.0, eta_minutes = 24),
    estimated_earnings = 12000.0, special_requirements = "Heavy lifting equipment required\nSite access available"
)

private fun dial(context: Context, customer: JobCustomer?) {
    val number = customer?.mobile_number ?: return
    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.country_code.orEmpty()}$number")))
}

// ---------------------------------------------------------------------------------------------
// The flow: the job's next_action picks the screen
// ---------------------------------------------------------------------------------------------

@Preview(showBackground = true, heightDp = 900)
@Composable
fun OperatorJobFlow(
    vm: OperatorJobViewModel? = null,
    bookingId: String = "",
    onClose: () -> Unit = {}
) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value ?: OperatorJobState(job = previewJob, loaded = true)
    val onIntent: (OperatorJobIntent) -> Unit = { vm?.onIntent(it) }
    LaunchedEffect(bookingId) { if (bookingId.isNotEmpty()) onIntent(OperatorJobIntent.Load(bookingId)) }
    com.cranezo.push.OnPush { data -> if (data["booking_id"].isNullOrBlank() || data["booking_id"] == bookingId) onIntent(OperatorJobIntent.Poll) }

    // Breakdown, issue and emergency open over the monitoring screen
    var report by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        vm?.effect?.collect { effect ->
            when (effect) {
                com.cranezo.ui.operator.model.OperatorJobEffect.Close -> onClose()
                com.cranezo.ui.operator.model.OperatorJobEffect.ReportSent -> report = null
            }
        }
    }

    val job = state.job
    if (job == null) {
        BookingScaffold(onBack = onClose, title = stringResource(R.string.op_job_title)) {
            if (state.loaded) {
                Textview(stringResource(R.string.crane_msg_generic_error), size = font_14, color = appColors.textSecondary, modifier = Modifier.padding(top = dimen_40))
            }
        }
        return
    }

    if (report != null) {
        ReportScreen(report.orEmpty(), state, onIntent) { report = null }
        return
    }

    when (job.next_action) {
        NextAction.ACCEPT_JOB, NextAction.START_JOURNEY -> JobRequestScreen(job, onIntent, onClose)
        NextAction.WAIT_OWNER -> MessageScreen(stringResource(R.string.op_waiting_owner), onClose)
        NextAction.MARK_ARRIVED -> JourneyScreen(job, state, onIntent, onClose)
        NextAction.WAIT_CUSTOMER -> ArrivalWaitScreen(job, onIntent, onClose)
        NextAction.START_JOB -> StartJobScreen(job, onIntent, onClose)
        NextAction.MONITOR -> MonitorScreen(job, state, onIntent, onClose) { report = it }
        NextAction.REQUEST_CONFIRMATION -> SummaryScreen(job, onIntent, onClose)
        NextAction.ENTER_OTP -> OtpScreen(job, onIntent, onClose)
        NextAction.DONE -> CompletedScreen(job, onClose)
        else -> MessageScreen(job.stage_label.orEmpty(), onClose)
    }
}

@Composable
private fun MessageScreen(text: String, onClose: () -> Unit) {
    BookingScaffold(onBack = onClose, title = stringResource(R.string.op_job_title)) {
        Textview(text, size = font_16, color = appColors.textPrimary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = dimen_60))
    }
}

// ---------------------------------------------------------------------------------------------
// Shared pieces
// ---------------------------------------------------------------------------------------------

@Composable
private fun CraneHeaderCard(crane: JobCrane?, tag: String? = null) {
    GlassCard(contentPadding = dimen_12) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CraneImage(path = crane?.primary_photo, modifier = Modifier.size(dimen_56))
            Spacer(modifier = Modifier.width(dimen_12))
            Column(modifier = Modifier.weight(1f)) {
                Textview(crane?.title ?: crane?.capacity_name.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
                Textview(crane?.model_name.orEmpty(), size = font_11, color = appColors.textSecondary, maxLines = 1)
                Textview(
                    listOfNotNull(crane?.capacity_name, crane?.crane_type_name, crane?.fuel_type_name).joinToString("  •  "),
                    size = font_10, color = appColors.textSecondary, maxLines = 1
                )
            }
            (tag ?: crane?.crane_code)?.takeIf { it.isNotBlank() }?.let { BookingTag(it, InfoBlue) }
        }
    }
}

@Composable
private fun LabelValueCard(label: String, value: String, modifier: Modifier = Modifier, sub: String? = null) {
    GlassCard(modifier = modifier, contentPadding = dimen_12) {
        Textview(label.uppercase(), size = font_10, color = appColors.textSecondary)
        Textview(value, size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
        if (!sub.isNullOrBlank()) Textview(sub, size = font_11, color = appColors.textSecondary)
    }
}

@Composable
private fun CameraBox(onClick: () -> Unit, uploading: Boolean, label: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimen_16))
            .border(dimen_1, appColors.subtleBorder, RoundedCornerShape(dimen_16))
            .clickable(enabled = !uploading) { onClick() }
            .padding(vertical = dimen_24),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconBadge(icon = Icons.Default.AddAPhoto, accent = AmberPrimary, size = dimen_44, filled = true)
        Spacer(modifier = Modifier.height(dimen_8))
        Textview(if (uploading) stringResource(R.string.mp_uploading) else label, size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
        Textview(stringResource(R.string.op_photo_hint), size = font_11, color = appColors.textSecondary)
    }
}

/**
 * The operator's whole job is five steps. Every job screen opens with the same header: a five-part bar,
 * "Step 2 of 5", the step's name in large type and one short line saying what to do.
 */
private const val OPERATOR_STEPS = 5
private const val REACHED_RADIUS_M = 50.0

private fun metersBetween(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val r = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2)
    return 2 * r * Math.asin(Math.sqrt(a))
}

private fun stepOf(nextAction: String?): Int = when (nextAction) {
    NextAction.ACCEPT_JOB -> 1
    NextAction.START_JOURNEY, NextAction.MARK_ARRIVED -> 2
    NextAction.SITE_PHOTOS, NextAction.CHECKLIST, NextAction.WAIT_CUSTOMER, NextAction.START_JOB -> 3
    NextAction.MONITOR -> 4
    NextAction.REQUEST_CONFIRMATION, NextAction.ENTER_OTP -> 5
    else -> 0
}

@Composable
private fun OperatorStepHeader(nextAction: String?) {
    val step = stepOf(nextAction)
    if (step == 0) return
    val (title, hint) = when (step) {
        1 -> R.string.op_step_accept to R.string.op_step_accept_hint
        2 -> R.string.op_step_go to R.string.op_step_go_hint
        3 -> R.string.op_step_safety to R.string.op_step_safety_hint
        4 -> R.string.op_step_work to R.string.op_step_work_hint
        else -> R.string.op_step_finish to R.string.op_step_finish_hint
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(dimen_6), modifier = Modifier.fillMaxWidth()) {
            repeat(OPERATOR_STEPS) { i ->
                Box(
                    modifier = Modifier.weight(1f).height(dimen_6).clip(CircleShape)
                        .background(if (i < step) AmberPrimary else appColors.subtleFill)
                )
            }
        }
        Spacer(modifier = Modifier.height(dimen_10))
        Textview(stringResource(R.string.op_step_of, step, OPERATOR_STEPS).uppercase(), size = font_11, color = appColors.primaryText, fontWeight = FontWeight.SemiBold)
        Textview(stringResource(title), size = font_20, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
        Textview(stringResource(hint), size = font_13, color = appColors.textSecondary)
    }
}

/** One line of the job card: icon on the left, small label over a bold value, optional action on the right. */
@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    sub: String? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = dimen_6)) {
        IconBadge(icon = icon, accent = AmberPrimary, size = dimen_36)
        Spacer(modifier = Modifier.width(dimen_12))
        Column(modifier = Modifier.weight(1f)) {
            Textview(label, size = font_11, color = appColors.textSecondary)
            Textview(value, size = font_15, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
            if (!sub.isNullOrBlank()) Textview(sub, size = font_12, color = appColors.textSecondary)
        }
        trailing?.invoke()
    }
}

// ---------------------------------------------------------------------------------------------
// New Job Request / Start Navigation
// ---------------------------------------------------------------------------------------------

@Composable
private fun JobRequestScreen(job: OperatorJob, onIntent: (OperatorJobIntent) -> Unit, onClose: () -> Unit) {
    val context = LocalContext.current
    val accepted = job.next_action == NextAction.START_JOURNEY
    // the new job rings (buzzer tone) until the operator accepts or declines it
    androidx.compose.runtime.DisposableEffect(job.booking_id, accepted) {
        com.cranezo.push.PushNotifications.cancelFor(context, job.booking_id)
        if (!accepted) com.cranezo.push.BuzzerPlayer.start(context)
        onDispose { com.cranezo.push.BuzzerPlayer.stop() }
    }
    // Decline asks once more on the page itself (no popup): the bottom bar turns into Cancel / Yes, decline
    var confirmDecline by remember { mutableStateOf(false) }
    // before driving off: the operator confirms they are with the crane this job booked
    var withCrane by rememberSaveable(job.booking_id) { mutableStateOf(false) }

    BookingScaffold(
        onBack = onClose,
        title = stringResource(R.string.bk_new_request),
        bottom = {
            if (accepted) {
                CommonGradientButton(
                    text = stringResource(R.string.op_start_navigation),
                    enabled = withCrane,
                    onClick = { onIntent(OperatorJobIntent.StartJourney) }
                )
            } else if (confirmDecline) {
                Column {
                    Textview(stringResource(R.string.op_decline_message), size = font_13, color = appColors.textPrimary, modifier = Modifier.padding(bottom = dimen_8))
                    Row(horizontalArrangement = Arrangement.spacedBy(dimen_12)) {
                        CommonGradientButton(
                            text = stringResource(R.string.common_cancel), modifier = Modifier.weight(1f), onClick = { confirmDecline = false },
                            gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = appColors.textPrimary
                        )
                        CommonGradientButton(
                            text = stringResource(R.string.bk_decline), modifier = Modifier.weight(1f),
                            onClick = {
                                confirmDecline = false
                                onIntent(OperatorJobIntent.Decline(""))
                            },
                            gradientColors = listOf(Red, Red), textColor = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(dimen_12)) {
                    CommonGradientButton(
                        text = stringResource(R.string.bk_decline), modifier = Modifier.weight(1f), onClick = { confirmDecline = true },
                        gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = Red
                    )
                    CommonGradientButton(text = stringResource(R.string.bk_accept), modifier = Modifier.weight(1f), onClick = { onIntent(OperatorJobIntent.Accept) })
                }
            }
        }
    ) {
        OperatorStepHeader(job.next_action)
        // Everything about the job in one card, one line per fact
        GlassCard(contentPadding = dimen_14) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CraneImage(path = job.assigned_crane?.primary_photo ?: job.requested_crane?.primary_photo, modifier = Modifier.size(dimen_64))
                Spacer(modifier = Modifier.width(dimen_12))
                Column(modifier = Modifier.weight(1f)) {
                    Textview(job.requested_crane?.title.orEmpty(), size = font_16, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                    Textview(
                        listOfNotNull(job.assigned_crane?.model_name, job.assigned_crane?.registration_number).joinToString("  •  "),
                        size = font_12, color = appColors.textSecondary, maxLines = 1
                    )
                }
            }
            Spacer(modifier = Modifier.height(dimen_8))
            InfoRow(Icons.Default.Person, stringResource(R.string.op_customer), job.customer?.name.orEmpty())
            InfoRow(Icons.Default.LocationOn, stringResource(R.string.op_job_site), job.job_site?.name.orEmpty(), sub = job.job_site?.address)
            InfoRow(Icons.Default.Schedule, stringResource(R.string.op_when), formatServerDateTime(job.schedule?.start_at), sub = job.duration?.label)
            job.job_site?.distance_km?.let { km ->
                InfoRow(Icons.Default.Navigation, stringResource(R.string.op_distance), "$km km", sub = job.job_site?.eta_minutes?.let { stringResource(R.string.op_eta_min, it) })
            }
        }
        // The one number the operator cares about most, large
        GlassCard(contentPadding = dimen_14, borderColor = AmberPrimary) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Textview(stringResource(R.string.op_you_earn), size = font_14, color = appColors.textSecondary, modifier = Modifier.weight(1f))
                Textview(rupees(job.estimated_earnings), size = font_24, color = appColors.primaryText, fontWeight = FontWeight.Bold)
            }
        }
        if (!job.special_requirements.isNullOrBlank()) {
            GlassCard(contentPadding = dimen_12) {
                Textview(stringResource(R.string.op_special_requirements), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                job.special_requirements.split("\n").filter { it.isNotBlank() }.forEach {
                    Textview("• $it", size = font_12, color = appColors.textSecondary)
                }
            }
        }
        if (accepted) {
            // the crane the customer booked: type, capacity and the registration number to check on the vehicle
            val crane = job.assigned_crane
            GlassCard(contentPadding = dimen_14, borderColor = if (withCrane) SuccessGreen else AmberPrimary, onClick = { withCrane = !withCrane }) {
                Textview(stringResource(R.string.op_with_crane_q), size = font_15, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(dimen_6))
                Textview(
                    listOfNotNull(crane?.capacity_name, crane?.crane_type_name).joinToString(" ").ifBlank { job.requested_crane?.title.orEmpty() },
                    size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold
                )
                Textview(
                    listOfNotNull(crane?.registration_number, crane?.model_name, crane?.crane_code).joinToString("  •  "),
                    size = font_13, color = appColors.textSecondary
                )
                Spacer(modifier = Modifier.height(dimen_10))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (withCrane) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank, contentDescription = null,
                        tint = if (withCrane) SuccessGreen else appColors.textSecondary, modifier = Modifier.size(dimen_28)
                    )
                    Spacer(modifier = Modifier.width(dimen_8))
                    Textview(stringResource(R.string.op_with_crane_yes), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
                }
                if (!withCrane) {
                    Spacer(modifier = Modifier.height(dimen_4))
                    Textview(stringResource(R.string.op_with_crane_hint), size = font_12, color = appColors.textSecondary)
                }
            }
        }
        // calling the customer directly is switched off (see Privacy.kt)
        if (com.cranezo.utils.Privacy.PARTY_CALLS && accepted && job.customer?.mobile_number != null) {
            CommonGradientButton(
                text = stringResource(R.string.op_call_customer), leftIcon = Icons.Default.Call, onClick = { dial(context, job.customer) },
                gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = appColors.textPrimary
            )
        }
        Spacer(modifier = Modifier.height(dimen_8))
    }
}

// ---------------------------------------------------------------------------------------------
// On the way: live location is shared while this screen's stage lasts
// ---------------------------------------------------------------------------------------------

@Composable
private fun JourneyScreen(job: OperatorJob, state: OperatorJobState, onIntent: (OperatorJobIntent) -> Unit, onClose: () -> Unit) {
    val context = LocalContext.current
    val siteLat = job.job_site?.latitude
    val siteLng = job.job_site?.longitude
    // the road route comes with every location update (main roads a crane can use); asked here only until then
    LaunchedEffect(state.route.isEmpty(), state.myLatitude != null) {
        val la = state.myLatitude
        val lo = state.myLongitude
        if (state.route.isEmpty() && la != null && lo != null && siteLat != null && siteLng != null) onIntent(OperatorJobIntent.LoadRoute(la, lo, siteLat, siteLng))
    }
    // Location is shared while on the way, so the permission is asked here
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) { permission.launch(Manifest.permission.ACCESS_FINE_LOCATION) }

    // "I have reached" works only when the operator's phone is within 50 m of the job site
    val meters = if (state.myLatitude != null && state.myLongitude != null && siteLat != null && siteLng != null)
        metersBetween(state.myLatitude, state.myLongitude, siteLat, siteLng) else null
    // testing: with TESTING_RIDE_STATUS=true on the server the button is always there; otherwise only within 50 m (the regular flow)
    val testing = job.testing_ride == true
    val reachedReady = testing || (meters != null && meters <= REACHED_RADIUS_M)
    var confirmReached by remember { mutableStateOf(false) }
    if (confirmReached) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { confirmReached = false },
            title = { Textview(stringResource(R.string.op_reached_confirm_title), size = font_16, color = appColors.textPrimary, fontWeight = FontWeight.Bold) },
            // in test mode the alert says the operator is treated as within 50 m of the site
            text = { Textview(stringResource(if (testing) R.string.op_reached_test_msg else R.string.op_reached_confirm_msg), size = font_14, color = appColors.textSecondary) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { confirmReached = false; onIntent(OperatorJobIntent.Arrived) }) {
                    Textview(stringResource(R.string.op_reached_confirm_yes), size = font_14, color = AmberPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { confirmReached = false }) {
                    Textview(stringResource(R.string.common_cancel), size = font_14, color = appColors.textSecondary)
                }
            }
        )
    }

    BookingScaffold(
        onBack = onClose,
        title = stringResource(R.string.op_on_the_way),
        bottom = {
            if (reachedReady) {
                CommonGradientButton(text = stringResource(R.string.op_reached_button), onClick = { confirmReached = true })
            } else {
                Textview(
                    if (meters == null) stringResource(R.string.op_reached_nogps) else stringResource(R.string.op_reached_wait, Math.round(meters - REACHED_RADIUS_M).coerceAtLeast(1).toInt()),
                    size = font_13, color = appColors.textHint, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) {
        OperatorStepHeader(job.next_action)
        // where the customer's site is, and where the operator is now (moves smoothly as they drive)
        Box(modifier = Modifier.fillMaxWidth().height(380.dp).clip(RoundedCornerShape(dimen_16))) {
            com.cranezo.ui.booking.request.SiteMap(
                latitude = job.job_site?.latitude, longitude = job.job_site?.longitude, address = job.job_site?.address,
                modifier = Modifier.matchParentSize(),
                craneLatitude = state.myLatitude, craneLongitude = state.myLongitude,
                craneTitle = job.assigned_crane?.model_name,
                route = state.route,
                follow = true
            )
        }
        GlassCard(contentPadding = dimen_14) {
            InfoRow(Icons.Default.LocationOn, stringResource(R.string.op_job_site), job.job_site?.name.orEmpty(), sub = job.job_site?.address)
            // km and minutes left along the road, at crane speed (live from the server once moving)
            val km = state.liveDistanceKm ?: job.job_site?.distance_km
            val eta = state.liveEtaMinutes ?: job.job_site?.eta_minutes
            if (km != null) {
                InfoRow(
                    Icons.Default.Navigation, stringResource(R.string.op_en_route),
                    "$km km", sub = stringResource(R.string.op_eta_min, eta ?: 0)
                )
            }
        }
        Textview(stringResource(R.string.op_location_shared), size = font_12, color = appColors.textHint, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

// ---------------------------------------------------------------------------------------------
// Waiting for the customer to confirm the arrival
// ---------------------------------------------------------------------------------------------

@Composable
private fun ArrivalWaitScreen(job: OperatorJob, onIntent: (OperatorJobIntent) -> Unit, onClose: () -> Unit) {
    // The customer confirms in their app: asked every 5 seconds
    LaunchedEffect(job.booking_id) {
        while (true) {
            delay(5000)
            onIntent(OperatorJobIntent.Poll)
        }
    }

    BookingScaffold(onBack = onClose, title = stringResource(R.string.op_job_title)) {
        OperatorStepHeader(job.next_action)
        Column(modifier = Modifier.fillMaxWidth().padding(top = dimen_8), horizontalAlignment = Alignment.CenterHorizontally) {
            IconBadge(icon = Icons.Default.LocationOn, accent = AmberPrimary, size = dimen_70, filled = true)
            Spacer(modifier = Modifier.height(dimen_12))
            Textview(stringResource(R.string.op_reached_site), size = font_22, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            Textview(stringResource(R.string.op_within_site), size = font_12, color = appColors.textSecondary)
        }
        LabelValueCard(stringResource(R.string.op_customer), job.customer?.name.orEmpty(), sub = job.job_site?.address)
        Row(horizontalArrangement = Arrangement.spacedBy(dimen_12)) {
            LabelValueCard(stringResource(R.string.op_arrival_time), formatServerTime(job.arrival?.arrived_at), Modifier.weight(1f), formatServerDate(job.arrival?.arrived_at))
            LabelValueCard(stringResource(R.string.op_assigned_crane), job.assigned_crane?.model_name.orEmpty(), Modifier.weight(1f), job.assigned_crane?.registration_number)
        }
        GlassCard(contentPadding = dimen_12) {
            Textview(stringResource(R.string.op_waiting_customer), size = font_14, color = appColors.primaryText, fontWeight = FontWeight.Bold)
            Textview(stringResource(R.string.op_waiting_customer_sub), size = font_12, color = appColors.textSecondary)
        }
        // Only while the customer has not confirmed
        if (job.arrival?.waiting_for_customer == true) {
            CommonGradientButton(
                text = stringResource(R.string.op_confirm_arrival_manually), onClick = { onIntent(OperatorJobIntent.ConfirmArrival) },
                gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = AmberPrimary
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Start Job
// ---------------------------------------------------------------------------------------------

@Composable
private fun StartJobScreen(job: OperatorJob, onIntent: (OperatorJobIntent) -> Unit, onClose: () -> Unit) {
    BookingScaffold(
        onBack = onClose,
        title = stringResource(R.string.op_start_job),
        bottom = {
            CommonGradientButton(text = stringResource(R.string.op_start_job), onClick = { onIntent(OperatorJobIntent.StartJob) }, enabled = job.can_start == true)
        }
    ) {
        OperatorStepHeader(job.next_action)
        SafetyReminderCard()
        // The green checks (Crane Verified, Operator Verified, Documents Valid, Arrival Completed) are not shown any more.
        // A check that is NOT ok still is: it is the reason the Start Job button is off. Remove the filter to show all again.
        job.start_checks.orEmpty().filter { it.ok != true }.forEach { check ->
            GlassCard(contentPadding = dimen_12) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(check.label.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
                        // When something is not green, this is the reason
                        Textview(check.description.orEmpty(), size = font_11, color = if (check.ok == true) appColors.textSecondary else AmberPrimary)
                    }
                    Icon(
                        if (check.ok == true) Icons.Default.CheckCircle else Icons.Default.Warning, contentDescription = null,
                        tint = if (check.ok == true) SuccessGreen else AmberPrimary, modifier = Modifier.size(dimen_22)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Job Monitoring
// ---------------------------------------------------------------------------------------------

@Composable
private fun MonitorScreen(
    job: OperatorJob,
    state: OperatorJobState,
    onIntent: (OperatorJobIntent) -> Unit,
    onClose: () -> Unit,
    onReport: (String) -> Unit
) {
    var extraOpen by rememberSaveable { mutableStateOf(false) }
    val pendingExtra = job.extension?.pending
    LaunchedEffect(pendingExtra?.id) { if (pendingExtra != null) extraOpen = true }
    if (extraOpen && pendingExtra != null) {
        ExtraTimeAnswerScreen(job, pendingExtra, onIntent, onBack = { extraOpen = false })
        return
    }
    // Re-sync with the server every 5 seconds (an extra time request can arrive any time)
    LaunchedEffect(job.booking_id) {
        while (true) {
            delay(5_000)
            onIntent(OperatorJobIntent.Poll)
        }
    }

    BookingScaffold(
        onBack = onClose,
        title = stringResource(R.string.op_job_monitoring),
        bottom = { CommonGradientButton(text = stringResource(R.string.op_finish_job), onClick = { onIntent(OperatorJobIntent.Finish("")) }) }
    ) {
        OperatorStepHeader(job.next_action)
        // The real work time (ticking), against the booked hours
        FixedHoursCard(job.schedule?.start_at, job.schedule?.end_at, job.duration?.label, timers = job.timers)
        // Hold / Resume: pauses the job (waiting for material, the site, the weather); the customer sees it
        val onHold = job.timers?.is_waiting == true
        GlassCard(contentPadding = dimen_14, borderColor = if (onHold) AmberPrimary else Color.Unspecified) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    if (onHold) {
                        Textview(stringResource(R.string.op_on_hold_title), size = font_14, color = appColors.primaryText, fontWeight = FontWeight.Bold)
                        Textview(stringResource(R.string.op_on_hold_sub, formatServerTime(job.timers?.waiting_since)), size = font_12, color = appColors.textSecondary)
                    } else {
                        Textview(stringResource(R.string.op_hold_hint), size = font_12, color = appColors.textSecondary)
                    }
                }
                Spacer(modifier = Modifier.width(dimen_10))
                CommonGradientButton(
                    text = stringResource(if (onHold) R.string.op_resume else R.string.op_hold),
                    onClick = { onIntent(OperatorJobIntent.ToggleWaiting(!onHold)) },
                    modifier = Modifier.width(dimen_110), height = dimen_40, textSize = font_13,
                    gradientColors = if (onHold) listOf(SuccessGreen, SuccessGreen) else listOf(appColors.subtleFill, appColors.subtleFill),
                    textColor = if (onHold) Color.White else AmberPrimary
                )
            }
        }
        if (pendingExtra != null) {
            GlassCard(contentPadding = dimen_14, borderColor = AmberPrimary, onClick = { extraOpen = true }) {
                Textview(stringResource(R.string.op_extra_time_request), size = font_15, color = appColors.primaryText, fontWeight = FontWeight.Bold)
                Textview(stringResource(R.string.op_extra_requested_sub), size = font_12, color = appColors.textSecondary)
            }
        }
        // Problems: grouped at the bottom, out of the way of the normal work
        Spacer(modifier = Modifier.height(dimen_4))
        Textview(stringResource(R.string.op_problem_at_site), size = font_13, color = appColors.textSecondary, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(dimen_12)) {
            CommonGradientButton(
                text = stringResource(R.string.op_break_down), modifier = Modifier.weight(1f), onClick = { onReport("breakdown") },
                leftIcon = Icons.Default.Build, height = dimen_48, textSize = font_13,
                gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = AmberPrimary
            )
            CommonGradientButton(
                text = stringResource(R.string.op_report_issue), modifier = Modifier.weight(1f), onClick = { onReport("issue") },
                leftIcon = Icons.Default.Warning, height = dimen_48, textSize = font_13,
                gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = appColors.textPrimary
            )
        }
        CommonGradientButton(
            text = stringResource(R.string.op_emergency), leftIcon = Icons.Default.Emergency, onClick = { onReport("emergency") },
            height = dimen_48, textSize = font_14, gradientColors = listOf(Red, DarkRed), textColor = Color.White
        )
        Spacer(modifier = Modifier.height(dimen_8))
    }
}

// ---------------------------------------------------------------------------------------------
// Report breakdown, issue and emergency
// ---------------------------------------------------------------------------------------------

@Composable
private fun ReportScreen(kind: String, state: OperatorJobState, onIntent: (OperatorJobIntent) -> Unit, onBack: () -> Unit) {
    val job = state.job
    LaunchedEffect(kind) { if (kind == "breakdown") onIntent(OperatorJobIntent.LoadBreakdownTypes) }
    var type by rememberSaveable { mutableStateOf<Int?>(null) }
    var typeName by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var photo by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmEmergency by remember { mutableStateOf(false) }
    val picker = rememberImagePickerController()
    LaunchedEffect(picker.imageUrl) {
        val uri = picker.imageUrl ?: return@LaunchedEffect
        photo = uri
        picker.clear()
    }
    ImageSourceBottomSheet(picker)
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) { permission.launch(Manifest.permission.ACCESS_FINE_LOCATION) }


    if (kind == "emergency") {
        BookingScaffold(onBack = onBack, title = stringResource(R.string.op_emergency)) {
            Column(modifier = Modifier.fillMaxWidth().padding(top = dimen_24), horizontalAlignment = Alignment.CenterHorizontally) {
                IconBadge(icon = Icons.Default.Emergency, accent = Red, size = dimen_88, filled = true)
                Spacer(modifier = Modifier.height(dimen_12))
                Textview(stringResource(R.string.op_emergency).uppercase(), size = font_22, color = Red, fontWeight = FontWeight.Bold)
                Textview(stringResource(R.string.op_emergency_sub), size = font_12, color = appColors.textSecondary)
            }
            GlassCard(contentPadding = dimen_12) {
                Textview(stringResource(R.string.op_emergency_note), size = font_12, color = appColors.primaryText)
            }
            // A second tap on the page confirms (no popup), so help is never called by accident
            if (confirmEmergency) {
                GlassCard(contentPadding = dimen_12) {
                    Textview(stringResource(R.string.op_emergency_confirm), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(dimen_12)) {
                    CommonGradientButton(
                        text = stringResource(R.string.common_cancel), modifier = Modifier.weight(1f), onClick = { confirmEmergency = false },
                        gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = appColors.textPrimary
                    )
                    CommonGradientButton(
                        text = stringResource(R.string.op_get_help_now), modifier = Modifier.weight(1f),
                        onClick = {
                            confirmEmergency = false
                            onIntent(OperatorJobIntent.ReportEmergency)
                        },
                        gradientColors = listOf(Red, DarkRed), textColor = Color.White
                    )
                }
            } else {
                CommonGradientButton(
                    text = stringResource(R.string.op_get_help_now), onClick = { confirmEmergency = true },
                    gradientColors = listOf(Red, DarkRed), textColor = Color.White
                )
            }
            GlassCard(contentPadding = dimen_12) {
                Textview(stringResource(R.string.op_when_to_use), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                listOf(R.string.op_use_1, R.string.op_use_2, R.string.op_use_3, R.string.op_use_4).forEach {
                    Textview("• ${stringResource(it)}", size = font_12, color = appColors.textSecondary)
                }
            }
        }
        return
    }

    val isBreakdown = kind == "breakdown"
    BookingScaffold(
        onBack = onBack,
        title = stringResource(if (isBreakdown) R.string.op_report_breakdown else R.string.op_report_issue),
        bottom = {
            CommonGradientButton(
                text = stringResource(R.string.op_submit_report),
                enabled = if (isBreakdown) type != null else description.isNotBlank(),
                onClick = {
                    if (isBreakdown) onIntent(OperatorJobIntent.ReportBreakdown(type, description, photo))
                    else onIntent(OperatorJobIntent.ReportIssue(description, photo))
                }
            )
        }
    ) {
        GlassCard(contentPadding = dimen_12, borderColor = Red) {
            Textview(stringResource(if (isBreakdown) R.string.op_report_breakdown else R.string.op_report_issue), size = font_14, color = Red, fontWeight = FontWeight.Bold)
            Textview(stringResource(R.string.op_report_sub), size = font_11, color = appColors.textSecondary)
        }
        job?.let { CraneHeaderCard(it.assigned_crane, it.customer?.name) }
        if (isBreakdown) {
            GlassSelectField(
                label = stringResource(R.string.op_breakdown_type),
                value = typeName,
                placeholder = stringResource(R.string.op_select_breakdown_type),
                options = state.breakdownTypes,
                onSelected = {
                    type = it.id
                    typeName = it.name
                },
                onOpen = { onIntent(OperatorJobIntent.LoadBreakdownTypes) }
            )
        }
        GlassInputField(
            label = stringResource(R.string.op_description),
            value = description,
            onValueChange = { description = it.take(500) },
            placeholder = stringResource(R.string.op_description_hint),
            singleLine = false,
            minHeight = dimen_100,
            isRequired = !isBreakdown
        )
        CameraBox(onClick = picker::openChooser, uploading = false, label = if (photo == null) stringResource(R.string.profile_add_photo) else stringResource(R.string.op_photo_added))
    }
}

// ---------------------------------------------------------------------------------------------
// Job Summary, OTP, Completed
// ---------------------------------------------------------------------------------------------

@Composable
private fun SummaryRows(summary: JobSummary?) {
    summary ?: return
    GlassCard(contentPadding = dimen_14) {
        Textview(stringResource(R.string.op_job_timing), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(dimen_6))
        PriceLine(stringResource(R.string.fh_booked_hours), summary.base_hours?.let { stringResource(R.string.fh_n_hours, it.toInt()) } ?: "-")
        PriceLine(stringResource(R.string.op_start_time), formatServerTime(summary.start_time))
        PriceLine(stringResource(R.string.op_end_time), formatServerTime(summary.end_time))
        if ((summary.extra_seconds ?: 0L) > 0) PriceLine(stringResource(R.string.op_extra_time), formatSeconds(summary.extra_seconds), valueColor = InfoBlue)
    }
    // the operator sees only their own earning, never the customer's bill or the owner's prices
    GlassCard(contentPadding = dimen_14, borderColor = AmberPrimary) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Textview(stringResource(R.string.op_you_earn), size = font_14, color = appColors.textSecondary, modifier = Modifier.weight(1f))
            Textview(
                summary.earnings_amount?.let { rupees(it) } ?: stringResource(R.string.op_commission_pending),
                size = font_22, color = appColors.primaryText, fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SummaryScreen(job: OperatorJob, onIntent: (OperatorJobIntent) -> Unit, onClose: () -> Unit) {
    BookingScaffold(
        onBack = onClose,
        title = stringResource(R.string.op_job_summary),
        bottom = { CommonGradientButton(text = stringResource(R.string.op_request_confirmation), onClick = { onIntent(OperatorJobIntent.RequestConfirmation) }) }
    ) {
        OperatorStepHeader(job.next_action)
        SummaryRows(job.summary)
    }
}

@Composable
private fun OtpScreen(job: OperatorJob, onIntent: (OperatorJobIntent) -> Unit, onClose: () -> Unit) {
    var otp by rememberSaveable { mutableStateOf("") }
    BookingScaffold(
        onBack = onClose,
        title = stringResource(R.string.op_customer_confirmation),
        bottom = {
            CommonGradientButton(
                text = stringResource(R.string.op_confirm_completion), enabled = otp.length == 6,
                onClick = { onIntent(OperatorJobIntent.ConfirmCompletion(otp)) }
            )
        }
    ) {
        OperatorStepHeader(job.next_action)
        // What was done, in two numbers
        GlassCard(contentPadding = dimen_14) {
            PriceLine(stringResource(R.string.fh_booked_hours), job.duration?.label ?: "-")
            PriceLine(stringResource(R.string.op_you_earn), (job.summary?.earnings_amount ?: job.settlement?.final_amount)?.let { rupees(it) } ?: stringResource(R.string.op_commission_pending), bold = true)
        }
        GlassCard(contentPadding = dimen_16, borderColor = AmberPrimary) {
            Textview(stringResource(R.string.op_code_sent), size = font_13, color = appColors.textSecondary)
            Spacer(modifier = Modifier.height(dimen_10))
            GlassInputField(
                label = stringResource(R.string.op_enter_otp),
                value = otp,
                onValueChange = { otp = it.filter(Char::isDigit).take(6) },
                placeholder = "0 0 0 0 0 0",
                keyboardType = KeyboardType.NumberPassword
            )
            job.completion?.attempts_left?.let {
                Textview(stringResource(R.string.op_attempts_left, it), size = font_12, color = appColors.textSecondary)
            }
        }
        // A new code replaces the old one
        Textview(
            stringResource(R.string.op_resend_code), size = font_14, color = appColors.primaryText, fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().clickable { onIntent(OperatorJobIntent.RequestConfirmation) }.padding(vertical = dimen_12)
        )
    }
}

@Composable
private fun CompletedScreen(job: OperatorJob, onClose: () -> Unit) {
    BookingScaffold(
        onBack = onClose,
        success = true,
        title = stringResource(R.string.op_job_title),
        bottom = { CommonGradientButton(text = stringResource(R.string.op_view_earnings), onClick = { PendingTab.route = "earnings"; onClose() }) }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(top = dimen_16), horizontalAlignment = Alignment.CenterHorizontally) {
            com.cranezo.ui.components.SuccessTick(size = dimen_80)
            Spacer(modifier = Modifier.height(dimen_8))
            Textview(stringResource(R.string.op_completed_title), size = font_20, color = SuccessGreen, fontWeight = FontWeight.Bold)
            Textview(stringResource(R.string.op_completed_sub), size = font_12, color = SuccessGreen)
        }
        CraneHeaderCard(job.assigned_crane, job.customer?.name)
        SummaryRows(job.summary)
        job.settlement?.status_label?.let {
            Textview(it, size = font_12, color = appColors.primaryText, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
    }
}

/** Extra time the customer asked for: the operator approves or rejects within the countdown. */
@Composable
private fun ExtraTimeAnswerScreen(
    job: OperatorJob,
    request: com.cranezo.ui.booking.model.ExtensionRequest,
    onIntent: (OperatorJobIntent) -> Unit,
    onBack: () -> Unit
) {
    var secondsLeft by remember(request.id, request.seconds_left) { androidx.compose.runtime.mutableIntStateOf(request.seconds_left ?: 0) }
    LaunchedEffect(request.id, request.seconds_left) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
    }
    BookingScaffold(
        onBack = onBack,
        title = stringResource(R.string.op_extra_time_request),
        bottom = {
            Row(horizontalArrangement = Arrangement.spacedBy(dimen_12)) {
                CommonGradientButton(
                    text = stringResource(R.string.bk_decline), modifier = Modifier.weight(1f), onClick = { onIntent(OperatorJobIntent.RespondExtension(false, "")) },
                    gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = Red
                )
                CommonGradientButton(text = stringResource(R.string.op_approve), modifier = Modifier.weight(1f), onClick = { onIntent(OperatorJobIntent.RespondExtension(true, "")) })
            }
        }
    ) {
        GlassCard(contentPadding = dimen_12, borderColor = AmberPrimary) {
            Textview(stringResource(R.string.op_pending_approval), size = font_14, color = appColors.primaryText, fontWeight = FontWeight.Bold)
            Textview(stringResource(R.string.op_extra_requested_sub), size = font_12, color = appColors.textSecondary)
            Textview("%02d:%02d".format(secondsLeft / 60, secondsLeft % 60), size = font_20, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
        }
        CraneHeaderCard(job.assigned_crane, job.customer?.name)
        GlassCard(contentPadding = dimen_14) {
            Textview(stringResource(R.string.op_current_job_timing), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(dimen_6))
            PriceLine(stringResource(R.string.op_start_time), formatServerDateTime(request.current_start_at))
            PriceLine(stringResource(R.string.op_current_end_time), formatServerDateTime(request.current_end_at))
        }
        GlassCard(contentPadding = dimen_14) {
            Textview(stringResource(R.string.op_requested_extension), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            Textview("+ ${request.label.orEmpty()}", size = font_20, color = appColors.primaryText, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(dimen_6))
            PriceLine(stringResource(R.string.op_additional_amount), rupees(request.amount))
            PriceLine(stringResource(R.string.op_new_end_time), formatServerDateTime(request.new_end_at))
        }
        if (!request.message.isNullOrBlank()) {
            GlassCard(contentPadding = dimen_12) {
                Textview(stringResource(R.string.op_customer_message), size = font_11, color = appColors.textSecondary)
                Textview("\"${request.message}\"", size = font_13, color = appColors.textPrimary)
            }
        }
    }
}
