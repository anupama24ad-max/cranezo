package com.cranezo.ui.booking

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.cranezo.ui.booking.components.FixedHoursCard
import com.cranezo.ui.booking.components.PriceLine
import com.cranezo.ui.booking.model.Booking
import com.cranezo.ui.booking.model.BookingActions
import com.cranezo.ui.booking.model.BookingCrane
import com.cranezo.ui.booking.model.BookingIntent
import com.cranezo.ui.booking.model.BookingJobInfo
import com.cranezo.ui.booking.model.BookingOperator
import com.cranezo.ui.booking.model.BookingPayment
import com.cranezo.ui.booking.model.BookingSite
import com.cranezo.ui.booking.model.BookingUiState
import com.cranezo.ui.booking.model.CurrentEstimate
import com.cranezo.ui.booking.model.ExtensionInfo
import com.cranezo.ui.booking.model.ExtensionRequest
import com.cranezo.ui.booking.model.UsageInfo
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.GlassInputField
import com.cranezo.ui.components.Textview
import com.cranezo.ui.cranes.components.CraneImage
import com.cranezo.ui.mainhome.components.IconBadge
import com.cranezo.ui.mainhome.components.InfoBlue
import com.cranezo.ui.mainhome.components.SuccessGreen
import com.cranezo.ui.operator.model.JobTimers
import androidx.compose.ui.unit.sp
import com.cranezo.ui.theme.*
import com.cranezo.utils.formatServerDateTime
import com.cranezo.utils.formatServerTime
import com.cranezo.utils.rupees
import kotlinx.coroutines.delay
import java.util.Locale

private val previewBooking = Booking(
    booking_id = "1", booking_code = "CRZ-260905-08421", status = 6, status_label = "In Progress", stage = "working", tab_label = "In Progress",
    crane = BookingCrane(title = "50 Ton Mobile Crane", capacity_name = "50 Ton", capacity_label = "50 Ton Capacity", verified = true, rating = 4.7),
    operator = BookingOperator(full_name = "Rajesh Kumar", rating = 4.8),
    site = BookingSite(name = "Construction Site", address = "Kakinada, Andhra Pradesh"),
    usage = UsageInfo(started_at = "2026-09-05T03:48:00.000Z", used_seconds = 9342, booked_seconds = 28800, remaining_seconds = 19458, percent_used = 32.4, current_estimate = CurrentEstimate(10380.0, 4000.0, "Final amount may change based on actual usage.")),
    timers = JobTimers(working_seconds = 9342),
    extension = ExtensionInfo(can_request = true, options = listOf(30, 60, 120, 240), step_minutes = 30, max_minutes = 240, hourly_rate = 4000.0, response_minutes = 5),
    payment = BookingPayment(booking_amount = 32000.0, booking_hours = 8.0, extra_minutes = 0, extra_time_amount = 0.0, final_total = 32000.0, balance_due = 32000.0, method = "COD"),
    job = BookingJobInfo(arrived_at = "2026-09-05T03:48:00.000Z", can_confirm_arrival = true),
    actions = BookingActions(can_request_extra_time = true)
)

@Composable
private fun rememberBooking(vm: BookingViewModel?): BookingUiState =
    vm?.uiState?.collectAsStateWithLifecycle()?.value ?: BookingUiState(booking = previewBooking)

@Composable
private fun hrsMin(seconds: Long?): String {
    val total = (seconds ?: 0L).coerceAtLeast(0)
    return stringResource(R.string.cj_hrs_min, total / 3600, (total % 3600) / 60)
}

private fun dial(context: Context, operator: BookingOperator?) {
    val number = operator?.mobile_number ?: return
    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${operator.country_code.orEmpty()}$number")))
}

/** Asks for the booking again every [periodMs] while the screen is open. */
@Composable
private fun PollWhileOpen(vm: BookingViewModel?, bookingId: String?, periodMs: Long = 5000) {
    LaunchedEffect(bookingId) {
        val id = bookingId ?: return@LaunchedEffect
        while (true) {
            delay(periodMs)
            vm?.onIntent(BookingIntent.PollBooking(id))
        }
    }
}

@Composable
private fun CraneOperatorCard(booking: Booking, showOperator: Boolean = true) {
    GlassCard(contentPadding = dimen_12) {
        Row {
            CraneImage(path = booking.crane?.primary_photo, modifier = Modifier.size(dimen_80))
            Spacer(modifier = Modifier.width(dimen_12))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(dimen_4)) {
                // title on the left, "Verified" in the top-right corner
                Row(verticalAlignment = Alignment.Top) {
                    Textview(
                        booking.crane?.title.orEmpty(), size = font_15, color = appColors.textPrimary, fontWeight = FontWeight.Bold,
                        maxLines = 2, modifier = Modifier.weight(1f).padding(end = dimen_8)
                    )
                    if (booking.crane?.verified == true) BookingTag(stringResource(R.string.common_verified), SuccessGreen)
                }
                booking.crane?.rating?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(dimen_14))
                        Textview(" ${String.format(Locale.US, "%.1f", it)} (${booking.crane.reviews_count ?: 0})", size = font_12, color = appColors.textPrimary)
                    }
                }
                Textview(
                    listOfNotNull(booking.crane?.capacity_name, booking.crane?.fuel_type_name, booking.duration?.label).joinToString("  •  "),
                    size = font_12, color = appColors.textSecondary
                )
            }
        }
        if (showOperator && booking.operator != null) {
            Spacer(modifier = Modifier.height(dimen_10))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(icon = Icons.Default.Call, accent = AmberPrimary, size = dimen_32)
                Spacer(modifier = Modifier.width(dimen_10))
                Column(modifier = Modifier.weight(1f)) {
                    Textview(stringResource(R.string.bk_operator).uppercase(), size = font_10, color = appColors.textSecondary)
                    Textview(booking.operator.full_name ?: stringResource(R.string.bk_operator), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                }
                booking.operator.rating?.let {
                    Icon(Icons.Default.Star, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(dimen_14))
                    Textview(" ${String.format(Locale.US, "%.1f", it)}", size = font_11, color = appColors.textPrimary)
                }
            }
        }
    }
}

/** Green success mark (Job Completed, Payment Successful): green check in a green ring with a soft glow */
@Composable
private fun SuccessMark() {
    val green = androidx.compose.ui.graphics.Color(0xFF22C55E)
    Box(
        modifier = Modifier
            .size(dimen_100)
            .background(androidx.compose.ui.graphics.Brush.radialGradient(listOf(green.copy(alpha = 0.28f), androidx.compose.ui.graphics.Color.Transparent))),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(dimen_64)
                .clip(CircleShape)
                .background(green.copy(alpha = 0.16f))
                .border(dimen_2, green.copy(alpha = 0.7f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = green, modifier = Modifier.size(dimen_32))
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Crane Has Arrived
// ---------------------------------------------------------------------------------------------

@Preview(showBackground = true, heightDp = 900)
@Composable
fun CraneArrivedScreen(vm: BookingViewModel? = null, onBack: () -> Unit = {}) {
    val state = rememberBooking(vm)
    val booking = state.booking ?: return
    val context = LocalContext.current
    PollWhileOpen(vm, booking.booking_id)
    // After tapping Start Job the operator has to finish the safety checks and start
    var tapped by rememberSaveable { mutableStateOf(false) }
    val waiting = tapped || booking.job?.arrival_confirmed_at != null

    BookingScaffold(
        onBack = onBack,
        title = stringResource(R.string.cj_arrived_title),
        bottom = {
            Column(verticalArrangement = Arrangement.spacedBy(dimen_8)) {
                if (booking.actions?.can_confirm_arrival == true && !waiting) {
                    CommonGradientButton(text = stringResource(R.string.cj_confirm_arrival), onClick = {
                        tapped = true
                        vm?.onIntent(BookingIntent.ConfirmArrival)
                    })
                }
                CommonGradientButton(
                    text = stringResource(R.string.cj_contact_operator), leftIcon = Icons.Default.Call, onClick = { dial(context, booking.operator) },
                    gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = appColors.textPrimary
                )
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(top = dimen_8), horizontalAlignment = Alignment.CenterHorizontally) {
            SuccessMark()
            Spacer(modifier = Modifier.height(dimen_8))
            Textview(stringResource(R.string.cj_arrived_heading), size = font_22, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            Textview(stringResource(R.string.cj_arrived_sub), size = font_12, color = appColors.textSecondary)
        }
        CraneOperatorCard(booking)
        GlassCard(contentPadding = dimen_12) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(dimen_16))
                Spacer(modifier = Modifier.width(dimen_8))
                Textview(booking.site?.address ?: booking.site?.name.orEmpty(), size = font_12, color = appColors.textPrimary, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(dimen_6))
            Textview(stringResource(R.string.cj_arrived_at, formatServerTime(booking.job?.arrived_at)), size = font_12, color = SuccessGreen)
        }
        BookingIdStatusRow(booking)
        GlassCard(contentPadding = dimen_12) {
            Textview(
                text = if (waiting) stringResource(R.string.cj_waiting_start) else stringResource(R.string.cj_arrived_confirm_text),
                size = font_12, color = appColors.textSecondary
            )
        }
    }
}

@Composable
private fun BookingIdStatusRow(booking: Booking) {
    val context = LocalContext.current
    GlassCard(contentPadding = dimen_12) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Textview(stringResource(R.string.bk_booking_id).uppercase(), size = font_10, color = appColors.textSecondary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Textview(booking.booking_code.orEmpty(), size = font_12, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(dimen_6))
                    Icon(
                        Icons.Default.ContentCopy, contentDescription = null, tint = appColors.textSecondary,
                        modifier = Modifier.size(dimen_14).clickable {
                            (context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager)
                                .setPrimaryClip(android.content.ClipData.newPlainText("booking", booking.booking_code))
                        }
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Textview(stringResource(R.string.bk_booking_status).uppercase(), size = font_10, color = appColors.textSecondary)
                Textview(booking.job?.stage_label ?: booking.status_label.orEmpty(), size = font_12, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Job In Progress
// ---------------------------------------------------------------------------------------------

@Preview(showBackground = true, heightDp = 1000)
@Composable
fun JobInProgressScreen(
    vm: BookingViewModel? = null,
    onBack: () -> Unit = {},
    onExtra: () -> Unit = {},
    onExtraStatus: () -> Unit = {}
) {
    val state = rememberBooking(vm)
    val booking = state.booking ?: return
    val context = LocalContext.current
    // Re-synced every 30 seconds
    PollWhileOpen(vm, booking.booking_id, 30_000)
    val pending = booking.extension?.pending

    BookingScaffold(
        onBack = onBack,
        title = stringResource(R.string.cj_in_progress_title),
        bottom = {
            Column(verticalArrangement = Arrangement.spacedBy(dimen_8)) {
                if (booking.actions?.can_request_extra_time == true && pending == null) {
                    CommonGradientButton(text = stringResource(R.string.cj_request_extra_time), onClick = onExtra)
                }
                CommonGradientButton(
                    text = stringResource(R.string.cj_contact_operator), leftIcon = Icons.Default.Call, onClick = { dial(context, booking.operator) },
                    gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = appColors.textPrimary
                )
            }
        }
    ) {
        // Fixed hours: what was booked, until when, and how much is left. No running clock.
        FixedHoursCard(booking.schedule?.start_at, booking.schedule?.end_at, booking.duration?.label)

        // The extra time that is waiting for the operator
        if (pending != null) {
            GlassCard(contentPadding = dimen_12, onClick = onExtraStatus) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(stringResource(R.string.cj_request_time), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
                        Textview(stringResource(R.string.cj_view_status).uppercase(), size = font_10, color = appColors.textSecondary)
                    }
                    BookingTag(stringResource(R.string.cj_pending), AmberPrimary)
                }
            }
        }

        CraneOperatorCard(booking)
        // Site and price together, nothing else
        GlassCard(contentPadding = dimen_14) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(dimen_18))
                Spacer(modifier = Modifier.width(dimen_8))
                Textview(booking.site?.address ?: booking.site?.name.orEmpty(), size = font_13, color = appColors.textPrimary, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(dimen_10))
            PriceLine(stringResource(R.string.fh_total_price), rupees(booking.payment?.final_total ?: booking.payment?.booking_amount), bold = true)
        }
        Spacer(modifier = Modifier.height(dimen_8))
    }
}

// ---------------------------------------------------------------------------------------------
// Request Extra Time
// ---------------------------------------------------------------------------------------------

@Composable
private fun minutesLabel(minutes: Int): String =
    if (minutes % 60 == 0) stringResource(if (minutes == 60) R.string.cj_one_hour else R.string.cj_n_hours, minutes / 60)
    else stringResource(R.string.cj_n_min, minutes)

@Preview(showBackground = true, heightDp = 1000)
@Composable
fun ExtraTimeRequestScreen(vm: BookingViewModel? = null, onBack: () -> Unit = {}) {
    val state = rememberBooking(vm)
    val booking = state.booking ?: return
    val ext = booking.extension
    val step = ext?.step_minutes ?: 30
    val max = ext?.max_minutes ?: 240
    var minutes by rememberSaveable { mutableIntStateOf(60) }
    var message by rememberSaveable { mutableStateOf("") }

    BookingScaffold(
        onBack = onBack,
        title = stringResource(R.string.cj_request_extra_time),
        subtitle = stringResource(R.string.cj_need_longer),
        bottom = {
            Column(verticalArrangement = Arrangement.spacedBy(dimen_8)) {
                CommonGradientButton(text = stringResource(R.string.cj_request_extra_time), onClick = { vm?.onIntent(BookingIntent.RequestExtra(minutes, message)) })
                CommonGradientButton(
                    text = stringResource(R.string.common_cancel), onClick = onBack,
                    gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = appColors.textPrimary
                )
            }
        }
    ) {
        // Where the booking stands now: the same booked-hours card as Job In Progress
        FixedHoursCard(booking.schedule?.start_at, booking.schedule?.end_at, booking.duration?.label)

        GlassCard(contentPadding = dimen_14) {
            Textview(stringResource(R.string.cj_how_much_extra), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            Textview(stringResource(R.string.cj_select_extra), size = font_11, color = appColors.textSecondary)
            Spacer(modifier = Modifier.height(dimen_10))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StepButton(Icons.Default.Remove, enabled = minutes - step >= step) { minutes -= step }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Textview(minutesLabel(minutes), size = font_20, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                    Textview(stringResource(R.string.cj_plus_minutes, minutes), size = font_10, color = appColors.textSecondary)
                }
                StepButton(Icons.Default.Add, enabled = minutes + step <= max) { minutes += step }
            }
            Spacer(modifier = Modifier.height(dimen_10))
            Row(horizontalArrangement = Arrangement.spacedBy(dimen_8)) {
                ext?.options.orEmpty().forEach { option ->
                    val selected = option == minutes
                    Box(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(dimen_10))
                            .background(if (selected) AmberPrimary.copy(alpha = 0.2f) else appColors.subtleFill)
                            .clickable { minutes = option }.padding(vertical = dimen_8),
                        contentAlignment = Alignment.Center
                    ) { Textview(minutesChip(option), size = font_11, color = if (selected) appColors.primaryText else appColors.textPrimary, maxLines = 1) }
                }
            }
        }

        // The one number the app may preview: rate x time. The server's amount is final.
        GlassCard(contentPadding = dimen_14) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Textview(stringResource(R.string.cj_estimated_additional), size = font_11, color = appColors.textSecondary)
                    Textview(stringResource(R.string.bk_per_hour, rupees(ext?.hourly_rate)), size = font_10, color = appColors.textHint)
                }
                Textview(rupees((ext?.hourly_rate ?: 0.0) * minutes / 60.0), size = font_20, color = appColors.primaryText, fontWeight = FontWeight.Bold)
            }
        }
        GlassInputField(
            label = stringResource(R.string.cj_message),
            value = message, onValueChange = { message = it.take(500) },
            placeholder = stringResource(R.string.cj_message_hint), singleLine = false, minHeight = dimen_70
        )
        Textview(stringResource(R.string.cj_extra_info, ext?.response_minutes ?: 5), size = font_11, color = appColors.textHint)
    }
}

@Composable
private fun minutesChip(minutes: Int): String = minutesLabel(minutes)


@Composable
private fun StepButton(icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(dimen_44).clip(RoundedCornerShape(dimen_12)).background(appColors.subtleFill).clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) { Icon(icon, contentDescription = null, tint = if (enabled) AmberPrimary else appColors.textHint) }
}

// ---------------------------------------------------------------------------------------------
// Extra Time Requested: waiting for the operator, then the answer
// ---------------------------------------------------------------------------------------------

@Preview(showBackground = true, heightDp = 900)
@Composable
fun ExtraTimeStatusScreen(vm: BookingViewModel? = null, onBack: () -> Unit = {}) {
    val state = rememberBooking(vm)
    val booking = state.booking ?: return
    val context = LocalContext.current
    PollWhileOpen(vm, booking.booking_id)

    val pending = booking.extension?.pending
    // When nothing waits any more, the latest answer is shown
    val request: ExtensionRequest? = pending ?: booking.extension?.history?.firstOrNull()

    var secondsLeft by remember(request?.id, request?.seconds_left) { mutableIntStateOf(request?.seconds_left ?: 0) }
    LaunchedEffect(request?.id, request?.seconds_left) {
        while (pending != null && secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
    }

    BookingScaffold(
        onBack = onBack,
        title = stringResource(R.string.cj_extra_requested),
        bottom = {
            Column(verticalArrangement = Arrangement.spacedBy(dimen_8)) {
                if (pending != null) {
                    CommonGradientButton(text = stringResource(R.string.cj_contact_operator), onClick = { dial(context, booking.operator) })
                    CommonGradientButton(
                        text = stringResource(R.string.cj_cancel_request), onClick = { vm?.onIntent(BookingIntent.CancelExtra) },
                        gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = appColors.textPrimary
                    )
                } else {
                    CommonGradientButton(text = stringResource(R.string.cj_back_to_job), onClick = onBack)
                }
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(top = dimen_8), horizontalAlignment = Alignment.CenterHorizontally) {
            IconBadge(icon = Icons.Default.HourglassTop, accent = AmberPrimary, size = dimen_56)
            Spacer(modifier = Modifier.height(dimen_8))
            Textview(
                stringResource(when (request?.status_label) { "Approved" -> R.string.cj_extra_approved; "Rejected" -> R.string.cj_extra_rejected; "Expired" -> R.string.cj_extra_expired; "Cancelled" -> R.string.cj_extra_cancelled; else -> R.string.cj_extra_requested }),
                size = font_20, color = appColors.textPrimary, fontWeight = FontWeight.Bold
            )
            Textview(
                stringResource(if (pending != null) R.string.cj_extra_waiting else R.string.cj_extra_answered),
                size = font_12, color = appColors.textSecondary, textAlign = TextAlign.Center
            )
        }
        if (request != null) {
            GlassCard(contentPadding = dimen_14) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(stringResource(R.string.cj_request_details), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                        Textview(request.code.orEmpty(), size = font_10, color = appColors.textSecondary)
                    }
                    BookingTag(request.status_label.orEmpty(), when (request.status_label) { "Approved" -> SuccessGreen; "Rejected", "Expired", "Cancelled" -> Red; else -> AmberPrimary })
                }
                Spacer(modifier = Modifier.height(dimen_8))
                Row {
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(stringResource(R.string.cj_requested_extra).uppercase(), size = font_10, color = appColors.textSecondary)
                        Textview("+ ${request.label.orEmpty()}", size = font_16, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Textview(stringResource(R.string.cj_estimated_cost).uppercase(), size = font_10, color = appColors.textSecondary)
                        Textview(rupees(request.amount), size = font_16, color = appColors.primaryText, fontWeight = FontWeight.Bold)
                    }
                }
                if (pending != null) {
                    Spacer(modifier = Modifier.height(dimen_8))
                    Textview(
                        stringResource(R.string.cj_awaiting_operator, "%02d:%02d".format(secondsLeft / 60, secondsLeft % 60)),
                        size = font_12, color = appColors.primaryText, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                    )
                }
                if (!request.reject_reason.isNullOrBlank()) Textview(request.reject_reason, size = font_11, color = appColors.textSecondary)
            }
        }
        CraneOperatorCard(booking)
        if (pending != null) {
            GlassCard(contentPadding = dimen_12) {
                Textview(stringResource(R.string.cj_what_next), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                Textview(stringResource(R.string.cj_what_next_text), size = font_11, color = appColors.textSecondary)
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Share the code with the operator
// ---------------------------------------------------------------------------------------------

@Preview(showBackground = true, heightDp = 700)
@Composable
fun ShareCodeScreen(vm: BookingViewModel? = null, onBack: () -> Unit = {}) {
    val state = rememberBooking(vm)
    val booking = state.booking ?: return
    PollWhileOpen(vm, booking.booking_id)
    val code = booking.job?.completion_otp

    BookingScaffold(onBack = onBack, title = stringResource(R.string.cj_job_finished)) {
        CraneOperatorCard(booking)
        GlassCard(contentPadding = dimen_20) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Textview(stringResource(R.string.cj_share_code_text), size = font_13, color = appColors.textSecondary, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(dimen_12))
                // Only while the confirmation is awaited: the server sends null in every other state
                Textview(code?.chunked(3)?.joinToString(" ") ?: "- - -", size = font_32, color = appColors.primaryText, fontWeight = FontWeight.Bold)
                if (booking.job?.completion_otp_expires_at != null) {
                    Spacer(modifier = Modifier.height(dimen_6))
                    Textview(stringResource(R.string.cj_valid_until, formatServerTime(booking.job.completion_otp_expires_at)), size = font_11, color = appColors.textHint)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Job Completed, Complete Final Payment, Payment Successful, Rating
// ---------------------------------------------------------------------------------------------

@Composable
private fun PaymentRows(payment: BookingPayment?) {
    payment ?: return
    PriceLine(stringResource(R.string.cj_booking_amount, (payment.booking_hours ?: 0.0).toInt()), rupees(payment.booking_amount))
    // Hidden at 0
    if ((payment.extra_time_amount ?: 0.0) > 0) PriceLine(stringResource(R.string.cj_extra_time_min, payment.extra_minutes ?: 0), rupees(payment.extra_time_amount))
    // the owner's extra charges and GST, line by line
    payment.breakdown.orEmpty()
        .filter { it.key?.startsWith("charge_") == true || it.key == "gst" }
        .forEach { PriceLine(it.label.orEmpty(), rupees(it.amount)) }
    Spacer(modifier = Modifier.height(dimen_4))
    PriceLine(stringResource(R.string.cj_final_total), rupees(payment.final_total), bold = true)
    // With Cash on Delivery nothing is collected in advance, so this row is usually hidden
    if ((payment.paid_amount ?: 0.0) > 0) PriceLine(stringResource(R.string.cj_paid_advance, payment.advance_percent ?: 0), "- ${rupees(payment.paid_amount)}")
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
fun JobCompletedScreen(
    vm: BookingViewModel? = null,
    onBack: () -> Unit = {},
    onPay: () -> Unit = {},
    onReceipt: () -> Unit = {}
) {
    val state = rememberBooking(vm)
    val booking = state.booking ?: return
    var rateOpen by remember { mutableStateOf(false) }
    RatingSheet(visible = rateOpen, onDismiss = { rateOpen = false }) { stars, comment ->
        vm?.onIntent(BookingIntent.Rate(stars, comment))
        rateOpen = false
    }

    BookingScaffold(
        onBack = onBack,
        title = stringResource(R.string.cj_job_completed_title),
        bottom = {
            Column(verticalArrangement = Arrangement.spacedBy(dimen_8)) {
                if (booking.actions?.can_pay == true) CommonGradientButton(text = stringResource(R.string.cj_complete_payment), onClick = onPay)
                if (booking.actions?.can_rate == true) CommonGradientButton(text = stringResource(R.string.cj_rate_experience), onClick = { rateOpen = true })
                if (booking.actions?.can_view_receipt == true) {
                    CommonGradientButton(
                        text = stringResource(R.string.cj_view_receipt), onClick = onReceipt,
                        gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = appColors.textPrimary
                    )
                }
                // payment not recorded, unhappy with the work, etc.: opens the complaint form for this booking
                val context = androidx.compose.ui.platform.LocalContext.current
                Textview(
                    stringResource(R.string.fin_raise_complaint), size = font_13, color = appColors.textSecondary,
                    modifier = Modifier.fillMaxWidth().clickable {
                        context.startActivity(
                            com.cranezo.ui.finance.FinanceActivity.intent(context, com.cranezo.ui.finance.FinanceRoutes.COMPLAINT_NEW, booking.booking_id)
                        )
                    }.padding(vertical = dimen_6),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(top = dimen_8), horizontalAlignment = Alignment.CenterHorizontally) {
            SuccessMark()
            Spacer(modifier = Modifier.height(dimen_8))
            Textview(stringResource(R.string.op_job_completed), size = font_22, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            Textview(stringResource(R.string.cj_completed_sub), size = font_12, color = appColors.textSecondary)
        }
        CraneOperatorCard(booking)
        BookingIdStatusRow(booking)
        booking.job_summary?.let { s ->
            GlassCard(contentPadding = dimen_14) {
                Textview(stringResource(R.string.op_job_summary), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(dimen_6))
                PriceLine(stringResource(R.string.cj_site), booking.site?.name.orEmpty())
                PriceLine(stringResource(R.string.op_start_time), formatServerDateTime(s.started_at))
                PriceLine(stringResource(R.string.cj_completed_at), formatServerDateTime(s.completed_at))
                PriceLine(stringResource(R.string.cj_total_usage), hrsMin(s.total_usage_seconds))
            }
        }
        GlassCard(contentPadding = dimen_14) {
            Textview(stringResource(R.string.bk_payment_summary), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(dimen_6))
            PaymentRows(booking.payment)
            PriceLine(stringResource(R.string.cj_balance_due), rupees(booking.payment?.balance_due), bold = true)
        }
        Spacer(modifier = Modifier.height(dimen_8))
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
fun FinalPaymentScreen(vm: BookingViewModel? = null, onBack: () -> Unit = {}, onPayOnline: () -> Unit = {}) {
    val state = rememberBooking(vm)
    val booking = state.booking ?: return
    val payment = booking.payment
    val balance = rupees(payment?.balance_due)

    // Online only (Razorpay: UPI, card, net banking). Cash is not taken from customers.
    BookingScaffold(
        onBack = onBack,
        title = stringResource(R.string.cj_final_payment_title),
        subtitle = stringResource(R.string.cj_final_payment_sub),
        bottom = {
            Column {
                Textview(stringResource(R.string.rq_pay_online_note), size = font_12, color = appColors.textSecondary, modifier = Modifier.padding(bottom = dimen_8))
                CommonGradientButton(text = stringResource(R.string.rq_pay_online, balance), onClick = onPayOnline)
            }
        }
    ) {
        CraneOperatorCard(booking)
        GlassCard(contentPadding = dimen_14) {
            Textview(stringResource(R.string.cj_payment_breakdown), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(dimen_6))
            PaymentRows(payment)
            Spacer(modifier = Modifier.height(dimen_8))
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(dimen_12)).background(appColors.subtleFill).padding(dimen_12),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Textview(stringResource(R.string.cj_balance_due).uppercase(), size = font_10, color = appColors.textSecondary)
                    Textview(stringResource(R.string.cj_to_be_settled), size = font_10, color = appColors.textHint)
                }
                Textview(balance, size = font_22, color = appColors.primaryText, fontWeight = FontWeight.Bold)
            }
        }
        // only the ways that work today (no "coming soon" rows)
        payment?.payment_methods.orEmpty().filter { it.enabled == true }.forEach { method ->
            GlassCard(contentPadding = dimen_12, borderColor = AmberPrimary) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(method.name.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                        Textview(method.description.orEmpty(), size = font_11, color = appColors.textSecondary)
                    }
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AmberPrimary)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RatingSheet(visible: Boolean, onDismiss: () -> Unit, onSubmit: (Int, String) -> Unit) {
    if (!visible) return
    var stars by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = dimen_28, topEnd = dimen_28),
        containerColor = appColors.screenBottom
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = dimen_20).padding(bottom = dimen_16),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimen_10)
        ) {
            IconBadge(icon = Icons.Default.Star, accent = AmberPrimary, size = dimen_48)
            Textview(stringResource(R.string.cj_rate_experience), size = font_18, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            Textview(stringResource(R.string.cj_rate_question), size = font_12, color = appColors.textSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(dimen_6)) {
                (1..5).forEach { i ->
                    Icon(
                        if (i <= stars) Icons.Default.Star else Icons.Default.StarBorder, contentDescription = null,
                        tint = if (i <= stars) AmberPrimary else appColors.textHint,
                        modifier = Modifier.size(dimen_36).clickable { stars = i }
                    )
                }
            }
            // optional: what went well or what to improve
            com.cranezo.ui.components.GlassInputField(
                label = stringResource(R.string.rq_rating_comment), value = comment, onValueChange = { comment = it.take(500) },
                placeholder = stringResource(R.string.rq_rating_comment_hint), singleLine = false, minHeight = dimen_88
            )
            CommonGradientButton(text = stringResource(R.string.cj_submit_rating), enabled = stars > 0, onClick = { onSubmit(stars, comment.trim()) })
            Textview(stringResource(R.string.cj_skip), size = font_13, color = appColors.textSecondary, modifier = Modifier.clickable { onDismiss() })
        }
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
fun PaymentSuccessfulScreen(vm: BookingViewModel? = null, onBack: () -> Unit = {}) {
    val state = rememberBooking(vm)
    val booking = state.booking ?: return
    val receipt = booking.receipt
    var rateOpen by remember { mutableStateOf(false) }
    RatingSheet(visible = rateOpen, onDismiss = { rateOpen = false }) { stars, comment ->
        vm?.onIntent(BookingIntent.Rate(stars, comment))
        rateOpen = false
    }

    BookingScaffold(
        onBack = onBack,
        title = stringResource(R.string.cj_payment_successful),
        bottom = {
            Column(verticalArrangement = Arrangement.spacedBy(dimen_8)) {
                if (booking.actions?.can_rate == true) CommonGradientButton(text = stringResource(R.string.cj_rate_experience), onClick = { rateOpen = true })
                CommonGradientButton(
                    text = stringResource(R.string.cj_view_details), onClick = onBack,
                    gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = appColors.textPrimary
                )
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(top = dimen_8), horizontalAlignment = Alignment.CenterHorizontally) {
            SuccessMark()
            Spacer(modifier = Modifier.height(dimen_8))
            Textview(stringResource(R.string.cj_payment_successful), size = font_22, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            Textview(stringResource(R.string.cj_payment_successful_sub), size = font_12, color = appColors.textSecondary, textAlign = TextAlign.Center)
        }
        CraneOperatorCard(booking)
        if (receipt != null) {
            GlassCard(contentPadding = dimen_14) {
                Textview(stringResource(R.string.bk_payment_summary), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(dimen_6))
                PriceLine(stringResource(R.string.cj_total_booking_amount), rupees(receipt.booking_amount))
                if ((receipt.extra_time_amount ?: 0.0) > 0) PriceLine(stringResource(R.string.op_extra_time), rupees(receipt.extra_time_amount))
                PriceLine(stringResource(R.string.bk_total_amount), rupees(receipt.total_amount))
                if ((receipt.advance_paid ?: 0.0) > 0) PriceLine(stringResource(R.string.cj_advance_paid), rupees(receipt.advance_paid))
                PriceLine(stringResource(R.string.cj_final_payment), rupees(receipt.final_payment))
                Spacer(modifier = Modifier.height(dimen_6))
                PriceLine(stringResource(R.string.cj_total_paid), rupees(receipt.total_paid), bold = true)
                Spacer(modifier = Modifier.height(dimen_6))
                Textview(stringResource(R.string.cj_payment_id, receipt.payment_id.orEmpty()), size = font_10, color = appColors.textSecondary)
                Textview(stringResource(R.string.cj_payment_completed, formatServerDateTime(receipt.paid_at)), size = font_10, color = SuccessGreen)
            }
        }
        Spacer(modifier = Modifier.height(dimen_8))
    }
}
