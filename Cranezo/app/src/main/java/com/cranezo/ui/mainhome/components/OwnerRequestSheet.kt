package com.cranezo.ui.mainhome.components

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Vibrator
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.cranezo.R
import com.cranezo.ui.booking.components.BookingTag
import com.cranezo.ui.booking.components.PriceLine
import com.cranezo.ui.booking.model.BookingCrane
import com.cranezo.ui.booking.model.BookingDuration
import com.cranezo.ui.booking.model.BookingPrice
import com.cranezo.ui.booking.model.BookingSchedule
import com.cranezo.ui.booking.model.OwnerRequest
import com.cranezo.ui.booking.model.RequestJobLocation
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.Textview
import com.cranezo.ui.cranes.components.CraneImage
import com.cranezo.ui.theme.*
import com.cranezo.utils.formatServerDateTime
import com.cranezo.utils.rupees
import kotlinx.coroutines.delay
import java.util.Locale

private val previewRequest = OwnerRequest(
    booking_id = "1", status = 0, can_respond = true, seconds_left = 540,
    crane = BookingCrane(title = "50 Ton Crane", crane_type_name = "Mobile Crane"),
    job_location = RequestJobLocation(address = "OMR, Sholinganallur, Chennai, TN", distance_km = 12.4),
    schedule = BookingSchedule("scheduled", "2025-09-15T08:30:00.000Z"),
    duration = BookingDuration(label = "8 Hours"),
    approx_earnings = 40000.0,
    earnings = BookingPrice(5000.0, 8.0, 40000.0, 0.0, 0.0, 0.0, true, true, 40000.0),
    next_steps = "The customer has been notified. Your job will appear in Active Jobs."
)

/**
 * The buzzer: "New Crane Request" with a countdown and Accept / Decline, then "Booking Accepted".
 * Plays the notification sound and vibrates when a request opens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerRequestHost(
    request: OwnerRequest?,
    accepted: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onDismiss: () -> Unit
) {
    if (request == null) return
    val context = LocalContext.current

    // The alert, once per request
    LaunchedEffect(request.booking_id) {
        runCatching {
            RingtoneManager.getRingtone(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).play()
            @Suppress("DEPRECATION")
            (context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator)?.vibrate(600)
        }
    }

    // Counted down on the phone from the server's number; at 0 the buttons stop working
    var secondsLeft by remember(request.booking_id) { mutableIntStateOf(request.seconds_left ?: 0) }
    LaunchedEffect(request.booking_id, accepted) {
        if (accepted) return@LaunchedEffect
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = dimen_28, topEnd = dimen_28),
        containerColor = appColors.screenBottom
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = dimen_20)
                .padding(bottom = dimen_16),
            verticalArrangement = Arrangement.spacedBy(dimen_12)
        ) {
            if (accepted) AcceptedContent(request, onDismiss) else NewRequestContent(request, secondsLeft, onAccept, onDecline)
        }
    }
}

@Composable
private fun JobLine(icon: ImageVector, label: String, value: String, extra: (@Composable () -> Unit)? = null) {
    Row(modifier = Modifier.padding(vertical = dimen_6), verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(dimen_16))
        Spacer(modifier = Modifier.width(dimen_10))
        Column(modifier = Modifier.weight(1f)) {
            Textview(label.uppercase(), size = font_10, color = appColors.textSecondary)
            Textview(value, size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
        }
        extra?.invoke()
    }
}

@Composable
private fun RequestDetails(request: OwnerRequest) {
    val context = LocalContext.current
    var showBreakdown by remember { mutableStateOf(false) }
    val location = request.job_location

    GlassCard(contentPadding = dimen_12) {
        JobLine(
            Icons.Default.LocationOn, stringResource(R.string.bk_job_location), location?.address.orEmpty(),
            extra = {
                Column(horizontalAlignment = Alignment.End) {
                    location?.distance_km?.let {
                        Textview(stringResource(R.string.bk_km_away, String.format(Locale.US, "%.1f", it)), size = font_10, color = appColors.textSecondary)
                    }
                    if (location?.latitude != null && location.longitude != null) {
                        Textview(
                            stringResource(R.string.bk_view_on_map), size = font_11, color = AmberPrimary, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickableNoRipple {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse("geo:${location.latitude},${location.longitude}?q=${location.latitude},${location.longitude}"))
                                )
                            }
                        )
                    }
                }
            }
        )
        JobLine(
            Icons.Default.CalendarMonth, stringResource(R.string.bk_required_date_time),
            if (request.schedule?.start_type == "now") stringResource(R.string.bk_now) else formatServerDateTime(request.schedule?.start_at)
        )
        JobLine(Icons.Default.AccessTime, stringResource(R.string.bk_duration), request.duration?.label.orEmpty())
        JobLine(
            Icons.Default.CurrencyRupee, stringResource(R.string.bk_approx_earnings), rupees(request.approx_earnings),
            extra = {
                Textview(
                    stringResource(R.string.bk_view_breakdown), size = font_11, color = AmberPrimary, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickableNoRipple { showBreakdown = !showBreakdown }
                )
            }
        )
        if (showBreakdown) {
            request.earnings?.let { e ->
                val included = stringResource(R.string.bk_included)
                PriceLine(stringResource(R.string.bk_crane_rental_line, rupees(e.hourly_rate), (e.duration_hours ?: 0.0).toInt()), rupees(e.crane_rental))
                PriceLine(stringResource(R.string.bk_operator_charges), if (e.operator_included == true) included else rupees(e.operator_charges))
                PriceLine(stringResource(R.string.bk_fuel_charges), if (e.fuel_included == true) included else rupees(e.fuel_charges))
                PriceLine(stringResource(R.string.bk_estimated_total), rupees(e.total_amount), bold = true)
            }
        }
    }
}

private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = this.clickable { onClick() }

@Composable
private fun NewRequestContent(request: OwnerRequest, secondsLeft: Int, onAccept: () -> Unit, onDecline: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Notifications, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(dimen_22))
        Spacer(modifier = Modifier.width(dimen_8))
        Column(modifier = Modifier.weight(1f)) {
            Textview(stringResource(R.string.bk_new_request), size = font_18, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            Textview(stringResource(R.string.bk_new_request_sub), size = font_11, color = appColors.textSecondary)
        }
        Textview(
            "%02d:%02d".format(secondsLeft / 60, secondsLeft % 60),
            size = font_16, color = AmberPrimary, fontWeight = FontWeight.Bold
        )
    }
    GlassCard(contentPadding = dimen_10) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CraneImage(path = request.crane?.primary_photo, modifier = Modifier.size(dimen_70))
            Spacer(modifier = Modifier.width(dimen_12))
            Column {
                BookingTag(stringResource(R.string.bk_requested_crane), AmberPrimary)
                Textview(request.crane?.title.orEmpty(), size = font_15, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                Textview(request.crane?.crane_type_name.orEmpty(), size = font_12, color = appColors.textSecondary)
            }
        }
    }
    RequestDetails(request)

    // Out of time or already answered: only the state is shown
    if (request.can_respond == true && secondsLeft > 0) {
        Row(horizontalArrangement = Arrangement.spacedBy(dimen_12)) {
            CommonGradientButton(
                text = stringResource(R.string.bk_decline),
                modifier = Modifier.weight(1f),
                onClick = onDecline,
                gradientColors = listOf(appColors.subtleFill, appColors.subtleFill),
                textColor = Red
            )
            CommonGradientButton(text = stringResource(R.string.bk_accept), modifier = Modifier.weight(1f), onClick = onAccept)
        }
    } else {
        Textview(request.status_label.orEmpty(), size = font_14, color = appColors.textSecondary, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    }
}

@Composable
private fun AcceptedContent(request: OwnerRequest, onDone: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(dimen_56).clip(CircleShape).background(SuccessGreen),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(dimen_30)) }
        Spacer(modifier = Modifier.height(dimen_8))
        Textview(stringResource(R.string.bk_booking_accepted), size = font_20, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
        Textview(stringResource(R.string.bk_booking_accepted_sub), size = font_12, color = appColors.textSecondary)
    }
    GlassCard(contentPadding = dimen_10) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CraneImage(path = request.crane?.primary_photo, modifier = Modifier.size(dimen_56))
            Spacer(modifier = Modifier.width(dimen_12))
            Column {
                BookingTag(stringResource(R.string.bk_accepted), SuccessGreen)
                Textview(request.crane?.title.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                Textview(request.crane?.crane_type_name.orEmpty(), size = font_12, color = appColors.textSecondary)
            }
        }
    }
    RequestDetails(request)
    if (!request.next_steps.isNullOrBlank()) {
        GlassCard(contentPadding = dimen_12) {
            Textview(stringResource(R.string.bk_whats_next), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(dimen_4))
            Textview(request.next_steps, size = font_12, color = appColors.textSecondary)
        }
    }
    CommonGradientButton(text = stringResource(R.string.bk_done), onClick = onDone)
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun NewRequestPreview() {
    CranezoTheme(darkTheme = true) {
        Column(modifier = Modifier.padding(dimen_20), verticalArrangement = Arrangement.spacedBy(dimen_12)) {
            NewRequestContent(previewRequest, 540, {}, {})
        }
    }
}
