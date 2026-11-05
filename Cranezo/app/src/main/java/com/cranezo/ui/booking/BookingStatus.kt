package com.cranezo.ui.booking

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.cranezo.R
import com.cranezo.ui.booking.components.BookingScaffold
import com.cranezo.ui.booking.components.BookingTag
import com.cranezo.ui.booking.components.PriceLine
import com.cranezo.ui.booking.model.Booking
import com.cranezo.ui.booking.model.BookingCrane
import com.cranezo.ui.booking.model.BookingIntent
import com.cranezo.ui.booking.model.BookingOperator
import com.cranezo.ui.booking.model.BookingPayment
import com.cranezo.ui.booking.model.BookingStatus
import com.cranezo.ui.booking.model.BookingUiState
import com.cranezo.ui.booking.model.TrackInfo
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.Textview
import com.cranezo.ui.cranes.components.CraneImage
import com.cranezo.ui.cranes.components.CraneSmallButton
import com.cranezo.ui.mainhome.components.IconBadge
import com.cranezo.ui.mainhome.components.SuccessGreen
import com.cranezo.ui.theme.*
import com.cranezo.utils.fileUrl
import com.cranezo.utils.formatServerDateTime
import com.cranezo.utils.formatServerTime
import com.cranezo.utils.rupees
import kotlinx.coroutines.delay

private val previewBooking = Booking(
    booking_id = "1", booking_code = "CRZ-260905-08421", status = 1, status_label = "Confirmed",
    status_message = "Your crane has been successfully booked.",
    crane = BookingCrane(title = "50T Mobile Crane", capacity_name = "50 Ton", fuel_type_name = "Diesel", verified = true, operator_included = true),
    operator = BookingOperator(full_name = "Rajesh Kumar", mobile_number = "9876543210", country_code = "+91"),
    eta_minutes = 18,
    payment = BookingPayment("COD", "Cash on Delivery", "pending", 32000.0, 20, 6400.0, 25600.0, 0.0)
)

// ---------------------------------------------------------------------------------------------
// Waiting for the owner
// ---------------------------------------------------------------------------------------------

/**
 * Request sent: a countdown, the crane, and Cancel. The result is fetched every 5 seconds
 * (the push notification will make this faster once Firebase is set up).
 */
@Preview(showBackground = true, heightDp = 800)
@Composable
fun WaitingScreen(
    vm: BookingViewModel? = null,
    onConfirmed: () -> Unit = {},
    onChooseAnother: () -> Unit = {},
    onClose: () -> Unit = {}
) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value
        ?: BookingUiState(booking = previewBooking.copy(status = 0, seconds_left = 540))
    val booking = state.booking
    val status = booking?.status

    // Poll while the request is open
    LaunchedEffect(booking?.booking_id) {
        val id = booking?.booking_id ?: return@LaunchedEffect
        while (true) {
            delay(5000)
            vm?.onIntent(BookingIntent.PollBooking(id))
        }
    }
    LaunchedEffect(status) {
        if (status == BookingStatus.CONFIRMED) onConfirmed()
        if (status == BookingStatus.CANCELLED) onClose()
    }

    // Seconds left, counted down on the phone from the server's number
    var secondsLeft by remember(booking?.booking_id, booking?.seconds_left) { mutableIntStateOf(booking?.seconds_left ?: 0) }
    LaunchedEffect(booking?.booking_id, booking?.seconds_left) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
        // Counted down to 0: ask the server instead of assuming
        booking?.booking_id?.let { vm?.onIntent(BookingIntent.PollBooking(it)) }
    }

    val finished = status == BookingStatus.DECLINED || status == BookingStatus.EXPIRED

    BookingScaffold(
        onBack = onClose,
        title = stringResource(R.string.bk_request_title),
        bottom = {
            if (finished) {
                CommonGradientButton(text = stringResource(R.string.bk_choose_another), onClick = onChooseAnother)
            } else {
                CommonGradientButton(
                    text = stringResource(R.string.bk_cancel_request),
                    onClick = { vm?.onIntent(BookingIntent.CancelBooking) },
                    gradientColors = listOf(Red, DarkRed, Maroon),
                    textColor = Color.White
                )
            }
        }
    ) {
        Spacer(modifier = Modifier.height(dimen_16))
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Textview(
                text = if (finished) booking?.status_label.orEmpty() else stringResource(R.string.bk_request_sent),
                size = font_20, color = if (finished) Red else appColors.textPrimary, fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(dimen_6))
            Textview(
                text = if (finished) booking?.status_message.orEmpty() else stringResource(R.string.bk_waiting_owner),
                size = font_13, color = appColors.textSecondary, textAlign = TextAlign.Center
            )
            if (finished && !booking?.decline_reason.isNullOrBlank()) {
                Textview(booking?.decline_reason.orEmpty(), size = font_12, color = appColors.textSecondary, textAlign = TextAlign.Center)
            }
            if (!finished) {
                Spacer(modifier = Modifier.height(dimen_16))
                Textview(
                    text = "%02d:%02d".format(secondsLeft / 60, secondsLeft % 60),
                    size = font_32, color = appColors.primaryText, fontWeight = FontWeight.Bold
                )
            }
        }
        booking?.let { CraneSummaryCard(it) }
    }
}

@Composable
private fun CraneSummaryCard(booking: Booking) {
    val crane = booking.crane
    GlassCard(contentPadding = dimen_12) {
        Row {
            CraneImage(path = crane?.primary_photo, modifier = Modifier.size(dimen_80))
            Spacer(modifier = Modifier.width(dimen_12))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(dimen_4)) {
                // title on the left, "Verified" in the top-right corner
                Row(verticalAlignment = Alignment.Top) {
                    Textview(crane?.title.orEmpty(), size = font_15, color = appColors.textPrimary, fontWeight = FontWeight.Bold, maxLines = 2, modifier = Modifier.weight(1f).padding(end = dimen_8))
                    if (crane?.verified == true) BookingTag(stringResource(R.string.common_verified), SuccessGreen)
                }
                Textview(
                    listOfNotNull(crane?.capacity_name, crane?.fuel_type_name, booking.duration?.label).joinToString("  •  "),
                    size = font_12, color = appColors.textSecondary
                )
                if (crane?.operator_included == true) {
                    Textview(stringResource(R.string.bk_operator_included), size = font_11, color = appColors.textSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(dimen_12))
                    Textview(booking.site?.name.orEmpty(), size = font_11, color = appColors.textSecondary, maxLines = 1)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Booking Confirmed (also used to look at an existing booking)
// ---------------------------------------------------------------------------------------------

@Preview(showBackground = true, heightDp = 900)
@Composable
fun BookingConfirmedScreen(
    vm: BookingViewModel? = null,
    onTrack: () -> Unit = {},
    onClose: () -> Unit = {}
) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value ?: BookingUiState(booking = previewBooking)
    val booking = state.booking ?: return
    val context = LocalContext.current
    val operator = booking.operator
    val payment = booking.payment

    BookingScaffold(
        onBack = onClose,
        title = stringResource(R.string.bk_confirmed_title),
        bottom = {
            if (booking.status == BookingStatus.CONFIRMED) {
                CommonGradientButton(text = stringResource(R.string.bk_track_crane), onClick = onTrack)
            } else {
                CommonGradientButton(text = stringResource(R.string.common_back), onClick = onClose)
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(dimen_56).clip(CircleShape).background(SuccessGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(dimen_30)) }
            Spacer(modifier = Modifier.height(dimen_8))
            Textview(booking.status_label.orEmpty(), size = font_20, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            Textview(booking.status_message.orEmpty(), size = font_12, color = appColors.textSecondary, textAlign = TextAlign.Center)
        }

        GlassCard(contentPadding = dimen_12) {
            CraneSummaryRow(booking)
            Spacer(modifier = Modifier.height(dimen_8))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Textview(stringResource(R.string.bk_booking_id), size = font_11, color = appColors.textSecondary, modifier = Modifier.weight(1f))
                Textview(booking.booking_code.orEmpty(), size = font_12, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(dimen_6))
                Icon(
                    Icons.Default.ContentCopy, contentDescription = null, tint = appColors.textSecondary,
                    modifier = Modifier.size(dimen_16).clickable { copyText(context, booking.booking_code) }
                )
            }
        }

        // Known only once the owner accepted
        if (operator != null) {
            GlassCard(contentPadding = dimen_12) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(dimen_44).clip(CircleShape)) {
                        val url = fileUrl(operator.profile_image)
                        Icon(Icons.Default.Person, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(dimen_44))
                        if (url != null) AsyncImage(model = url, contentDescription = null, modifier = Modifier.size(dimen_44))
                    }
                    Spacer(modifier = Modifier.width(dimen_12))
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(stringResource(R.string.bk_operator_assigned).uppercase(), size = font_10, color = appColors.textSecondary)
                        Textview(
                            operator.full_name ?: stringResource(R.string.bk_operator),
                            size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        BookingTag(stringResource(R.string.bk_crane_confirmed), SuccessGreen)
                        booking.eta_minutes?.let {
                            Textview(stringResource(R.string.bk_eta_label, it), size = font_12, color = appColors.textPrimary)
                        }
                    }
                }
            }
        }

        // Cash on Delivery: nothing was paid online, so the advance is "due", never "paid"
        if (payment != null) {
            GlassCard(contentPadding = dimen_14) {
                Textview(stringResource(R.string.bk_payment_summary), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(dimen_8))
                PriceLine(stringResource(R.string.bk_advance_due, payment.advance_percent ?: 0), rupees(payment.advance_amount), valueColor = AmberPrimary)
                PriceLine(stringResource(R.string.bk_remaining_amount), rupees(payment.remaining_amount))
                PriceLine(stringResource(R.string.bk_total_amount), rupees(booking.price?.total_payable ?: payment.total_amount))
                Textview(payment.method_name.orEmpty(), size = font_11, color = appColors.textSecondary)
            }
        }
        Textview(stringResource(R.string.bk_confirm_info), size = font_11, color = appColors.textHint)
        Spacer(modifier = Modifier.height(dimen_8))
    }
}

private fun copyText(context: Context, text: String?) {
    if (text.isNullOrBlank()) return
    (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
        .setPrimaryClip(ClipData.newPlainText("booking", text))
}

@Composable
private fun CraneSummaryRow(booking: Booking) {
    val crane = booking.crane
    Row {
        CraneImage(path = crane?.primary_photo, modifier = Modifier.size(dimen_80))
        Spacer(modifier = Modifier.width(dimen_12))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(dimen_4)) {
            Row(verticalAlignment = Alignment.Top) {
                Textview(crane?.title.orEmpty(), size = font_15, color = appColors.textPrimary, fontWeight = FontWeight.Bold, maxLines = 2, modifier = Modifier.weight(1f).padding(end = dimen_8))
                if (crane?.verified == true) BookingTag(stringResource(R.string.common_verified), SuccessGreen)
            }
            Textview(
                listOfNotNull(crane?.capacity_name, crane?.fuel_type_name, booking.duration?.label).joinToString("  •  "),
                size = font_12, color = appColors.textSecondary
            )
            Textview(booking.site?.name.orEmpty(), size = font_11, color = appColors.textSecondary, maxLines = 1)
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Track My Crane. No map SDK in the project yet, so the position opens in the maps app.
// ---------------------------------------------------------------------------------------------

@Preview(showBackground = true, heightDp = 800)
@Composable
fun TrackScreen(
    vm: BookingViewModel? = null,
    onBack: () -> Unit = {}
) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value ?: BookingUiState(
        booking = previewBooking,
        track = TrackInfo(
            booking_code = "CRZ-260905-08421", status_label = "Confirmed", eta_minutes = 18, distance_km = 2.4,
            crane = previewBooking.crane, operator = previewBooking.operator,
            expected_arrival_at = "2026-09-05T03:34:00.000Z"
        )
    )
    val context = LocalContext.current
    val bookingId = state.booking?.booking_id
    val track = state.track

    // Every 5 seconds while the screen is open
    LaunchedEffect(bookingId) {
        val id = bookingId ?: return@LaunchedEffect
        while (true) {
            vm?.onIntent(BookingIntent.LoadTrack(id))
            delay(5000)
        }
    }

    BookingScaffold(onBack = onBack, title = stringResource(R.string.bk_track_title)) {
        if (track == null) return@BookingScaffold

        GlassCard(contentPadding = dimen_12) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Textview(stringResource(R.string.bk_live_tracking), size = font_13, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                    Textview(stringResource(R.string.bk_live_tracking_sub), size = font_11, color = appColors.textSecondary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Textview(stringResource(R.string.bk_booking_id).uppercase(), size = font_10, color = appColors.textSecondary)
                    Textview(track.booking_code.orEmpty(), size = font_12, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        GlassCard(contentPadding = dimen_14) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CraneImage(path = track.crane?.primary_photo, modifier = Modifier.size(dimen_56))
                Spacer(modifier = Modifier.width(dimen_12))
                Column(modifier = Modifier.weight(1f)) {
                    Textview(track.crane?.title.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                    track.status_label?.let { BookingTag(it, SuccessGreen) }
                }
                Column(horizontalAlignment = Alignment.End) {
                    // null while the crane is not moving yet: the scheduled time is shown instead
                    if (track.eta_minutes != null) {
                        Textview(stringResource(R.string.bk_eta_min, track.eta_minutes), size = font_20, color = appColors.primaryText, fontWeight = FontWeight.Bold)
                        track.distance_km?.let {
                            Textview(stringResource(R.string.bk_km_away, String.format(java.util.Locale.US, "%.1f", it)), size = font_11, color = appColors.textSecondary)
                        }
                    } else {
                        Textview(formatServerDateTime(track.start_at), size = font_12, color = appColors.primaryText, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(dimen_10))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(dimen_16))
                Spacer(modifier = Modifier.width(dimen_6))
                Textview(
                    stringResource(R.string.bk_arriving_at, track.site?.name.orEmpty()),
                    size = font_12, color = appColors.textPrimary
                )
            }
        }

        track.operator?.let { operator ->
            GlassCard(contentPadding = dimen_12) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(icon = Icons.Default.Person, accent = AmberPrimary, size = dimen_40)
                    Spacer(modifier = Modifier.width(dimen_12))
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(stringResource(R.string.bk_operator).uppercase(), size = font_10, color = appColors.textSecondary)
                        Textview(operator.full_name ?: stringResource(R.string.bk_operator), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                    }
                    if (!operator.mobile_number.isNullOrBlank()) {
                        CraneSmallButton(
                            text = stringResource(R.string.bk_call),
                            icon = Icons.Default.Call,
                            onClick = {
                                context.startActivity(
                                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:${operator.country_code.orEmpty()}${operator.mobile_number}"))
                                )
                            }
                        )
                    }
                }
            }
        }

        GlassCard(contentPadding = dimen_12) {
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    Textview(stringResource(R.string.bk_booking_status).uppercase(), size = font_10, color = appColors.textSecondary)
                    Textview(track.status_label.orEmpty(), size = font_13, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Textview(stringResource(R.string.bk_expected_arrival).uppercase(), size = font_10, color = appColors.textSecondary)
                    Textview(
                        formatServerDateTime(track.expected_arrival_at ?: track.start_at),
                        size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Where the crane is, in the maps app
        val location = track.crane_location
        if (location?.latitude != null && location.longitude != null) {
            CommonGradientButton(
                text = stringResource(R.string.bk_open_in_maps),
                leftIcon = Icons.Default.Map,
                onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("geo:${location.latitude},${location.longitude}?q=${location.latitude},${location.longitude}"))
                    )
                }
            )
        }
        Spacer(modifier = Modifier.height(dimen_8))
    }
}

// ---------------------------------------------------------------------------------------------
// My bookings
// ---------------------------------------------------------------------------------------------

@Preview(showBackground = true, heightDp = 800)
@Composable
fun BookingListScreen(
    vm: BookingViewModel? = null,
    onBack: () -> Unit = {}
) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value ?: BookingUiState(bookings = listOf(previewBooking), bookingsLoaded = true)
    var group by remember { mutableStateOf("active") }
    LaunchedEffect(group) { vm?.onIntent(BookingIntent.LoadBookings(group)) }

    BookingScaffold(onBack = onBack, title = stringResource(R.string.bk_my_bookings)) {
        Row(horizontalArrangement = Arrangement.spacedBy(dimen_8)) {
            listOf("active" to R.string.bk_active, "past" to R.string.bk_past).forEach { (key, label) ->
                CraneSmallButton(
                    text = (if (group == key) "● " else "") + stringResource(label),
                    onClick = { group = key }
                )
            }
        }
        if (state.bookingsLoaded && state.bookings.isEmpty()) {
            Textview(stringResource(R.string.bk_no_bookings), size = font_14, color = appColors.textSecondary, modifier = Modifier.padding(top = dimen_40))
        }
        state.bookings.forEach { booking ->
            GlassCard(contentPadding = dimen_12, onClick = { booking.booking_id?.let { vm?.onIntent(BookingIntent.OpenBooking(it)) } }) {
                CraneSummaryRow(booking)
                Spacer(modifier = Modifier.height(dimen_8))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Textview(
                        formatServerDateTime(booking.schedule?.start_at), size = font_11, color = appColors.textSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    BookingTag(
                        booking.status_label.orEmpty(),
                        when (booking.status) {
                            BookingStatus.CONFIRMED, 5, 6, 7 -> SuccessGreen
                            BookingStatus.DECLINED, BookingStatus.EXPIRED, BookingStatus.CANCELLED -> Red
                            else -> AmberPrimary
                        }
                    )
                }
            }
        }
        if (state.bookingsHasMore) {
            LaunchedEffect(state.bookings.size) { vm?.onIntent(BookingIntent.LoadMoreBookings) }
        }
    }
}
