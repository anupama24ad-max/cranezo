package com.cranezo.ui.booking

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.Anchor
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cranezo.R
import com.cranezo.ui.booking.components.BookingScaffold
import com.cranezo.ui.booking.components.BookingTag
import com.cranezo.ui.booking.components.BottomButtons
import com.cranezo.ui.booking.components.PriceLine
import com.cranezo.ui.booking.components.SelectableCard
import com.cranezo.ui.booking.components.SelectedCheck
import com.cranezo.ui.booking.model.AvailableCrane
import com.cranezo.ui.booking.model.BookingIntent
import com.cranezo.ui.booking.model.BookingPrice
import com.cranezo.ui.booking.model.BookingUiState
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.Textview
import com.cranezo.ui.cranes.components.CraneImage
import com.cranezo.ui.cranes.components.CraneSmallButton
import com.cranezo.ui.mainhome.components.IconBadge
import com.cranezo.ui.mainhome.components.SuccessGreen
import com.cranezo.ui.theme.*
import com.cranezo.utils.formatServerDateTime
import com.cranezo.utils.rupees
import java.util.Locale

private val previewCrane = AvailableCrane(
    id = "1", title = "50T All-Terrain Crane", crane_type_name = "Mobile Crane", capacity_name = "50 Ton",
    fuel_type_name = "Diesel", verified = true, operator_included = true, availability_label = "Available",
    distance_km = 3.5, eta_minutes = 15, hourly_rate = 4000.0,
    price = BookingPrice(4000.0, 8.0, 32000.0, 0.0, 0.0, 0.0, true, true, 32000.0, 20, 6400.0, 25600.0)
)

private fun km(value: Double?) = String.format(Locale.US, "%.1f", value ?: 0.0)

// ---------------------------------------------------------------------------------------------
// Available Cranes
// ---------------------------------------------------------------------------------------------

@Preview(showBackground = true, heightDp = 800)
@Composable
fun AvailableCranesScreen(
    vm: BookingViewModel? = null,
    onBack: () -> Unit = {},
    onEditRequirements: () -> Unit = {}
) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value
        ?: BookingUiState(results = listOf(previewCrane), searched = true, resultsMessage = "We found 1 crane near you.")

    BookingScaffold(
        onBack = onBack,
        bottom = { BottomButtons(mainText = stringResource(R.string.bk_change_search), onMain = onEditRequirements, onBack = onBack) }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Textview(stringResource(R.string.bk_available_cranes), size = font_20, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                if (!state.resultsMessage.isNullOrBlank()) {
                    Textview(state.resultsMessage, size = font_12, color = appColors.textSecondary)
                }
            }
            if (state.siteAddress.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(dimen_14))
                        .background(appColors.subtleFill)
                        .padding(horizontal = dimen_10, vertical = dimen_6),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(dimen_14))
                    Spacer(modifier = Modifier.width(dimen_4))
                    Textview(
                        state.siteName.ifBlank { state.siteAddress }.take(18),
                        size = font_11,
                        color = appColors.textPrimary,
                        maxLines = 1
                    )
                }
            }
        }

        if (state.searched && state.results.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimen_60),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Textview(
                    stringResource(R.string.bk_no_cranes),
                    size = font_14,
                    color = appColors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        state.results.forEach { crane -> AvailableCraneCard(crane) { crane.id?.let { vm?.onIntent(BookingIntent.OpenCrane(it)) } } }

        if (state.hasMore) {
            // Infinite scroll: the next page is requested when this row is reached
            LaunchedEffect(state.results.size) { vm?.onIntent(BookingIntent.LoadMore) }
            Textview(
                stringResource(R.string.bk_loading_more),
                size = font_12,
                color = appColors.textSecondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MiniInfo(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(dimen_12))
        Spacer(modifier = Modifier.width(dimen_4))
        Textview(text, size = font_11, color = appColors.textSecondary, maxLines = 1)
    }
}

@Composable
private fun RatingRow(rating: Double?, reviews: Int?) {
    // No reviews exist yet: nothing to show
    if (rating == null) return
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Star, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(dimen_14))
        Spacer(modifier = Modifier.width(dimen_4))
        Textview(
            text = "${String.format(Locale.US, "%.1f", rating)} (${reviews ?: 0})",
            size = font_12,
            color = appColors.textPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AvailableCraneCard(crane: AvailableCrane, onSelect: () -> Unit) {
    GlassCard(contentPadding = dimen_12) {
        Row {
            Box {
                CraneImage(path = crane.primary_photo, modifier = Modifier.size(dimen_88))
                if (!crane.availability_label.isNullOrBlank()) {
                    Box(modifier = Modifier.padding(dimen_4)) { BookingTag(crane.availability_label, SuccessGreen) }
                }
            }
            Spacer(modifier = Modifier.width(dimen_12))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Textview(
                        crane.title.orEmpty(), size = font_14, color = appColors.textPrimary,
                        fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.weight(1f)
                    )
                    if (crane.verified == true) BookingTag(stringResource(R.string.common_verified), SuccessGreen)
                }
                RatingRow(crane.rating, crane.reviews_count)
                Spacer(modifier = Modifier.height(dimen_4))
                Row(horizontalArrangement = Arrangement.spacedBy(dimen_10)) {
                    MiniInfo(Icons.Default.Navigation, stringResource(R.string.bk_km_away, km(crane.distance_km)))
                    crane.eta_minutes?.let { MiniInfo(Icons.Default.Schedule, stringResource(R.string.bk_eta_min, it)) }
                }
                Spacer(modifier = Modifier.height(dimen_2))
                Row(horizontalArrangement = Arrangement.spacedBy(dimen_10)) {
                    crane.capacity_name?.let { MiniInfo(Icons.Default.Anchor, it) }
                    crane.fuel_type_name?.let { MiniInfo(Icons.Default.Timelapse, it) }
                }
                if (crane.operator_included == true) {
                    Textview(stringResource(R.string.bk_operator_included), size = font_11, color = appColors.textSecondary)
                }
            }
        }
        Spacer(modifier = Modifier.height(dimen_10))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Bottom) {
                Textview(rupees(crane.hourly_rate), size = font_18, color = appColors.primaryText, fontWeight = FontWeight.Bold)
                Textview(stringResource(R.string.bk_per_hour_short), size = font_11, color = appColors.textSecondary)
            }
            CommonGradientButton(
                text = stringResource(R.string.bk_select_crane),
                onClick = onSelect,
                height = dimen_40,
                textSize = font_12,
                modifier = Modifier.width(dimen_120)
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Crane Details
// ---------------------------------------------------------------------------------------------

@Composable
private fun StatTile(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier.fillMaxHeight(), contentPadding = dimen_10) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon = icon, accent = AmberPrimary, size = dimen_28)
            Spacer(modifier = Modifier.width(dimen_8))
            Column {
                Textview(value, size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
                Textview(label, size = font_10, color = appColors.textSecondary, maxLines = 1)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Preview(showBackground = true, heightDp = 900)
@Composable
fun BookableCraneScreen(
    vm: BookingViewModel? = null,
    onBack: () -> Unit = {},
    onEditBooking: () -> Unit = {},
    onSelect: () -> Unit = {}
) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value ?: BookingUiState(detail = previewCrane.copy(bookable = true))
    val crane = state.detail

    BookingScaffold(
        onBack = onBack,
        title = stringResource(R.string.crane_details_title),
        bottom = if (crane != null) {
            {
                CommonGradientButton(
                    text = stringResource(R.string.bk_select_this_crane),
                    onClick = onSelect,
                    enabled = crane.bookable != false
                )
            }
        } else null
    ) {
        if (crane == null) return@BookingScaffold

        val photos = (listOfNotNull(crane.primary_photo) + crane.photos.orEmpty().mapNotNull { it.photo_url }).distinct()
        if (photos.isNotEmpty()) {
            val pager = rememberPagerState(pageCount = { photos.size })
            HorizontalPager(state = pager, modifier = Modifier.fillMaxWidth()) { page ->
                CraneImage(
                    path = photos[page],
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimen_191),
                    shape = RoundedCornerShape(dimen_16)
                )
            }
        } else {
            CraneImage(path = null, modifier = Modifier.fillMaxWidth().height(dimen_191), shape = RoundedCornerShape(dimen_16))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Textview(crane.title.orEmpty(), size = font_20, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                RatingRow(crane.rating, crane.reviews_count)
            }
            Textview(rupees(crane.hourly_rate), size = font_18, color = appColors.primaryText, fontWeight = FontWeight.Bold)
            Textview(stringResource(R.string.bk_per_hour_short), size = font_11, color = appColors.textSecondary)
        }

        if (crane.bookable == false) {
            Textview(
                text = crane.not_bookable_reason ?: stringResource(R.string.crane_msg_generic_error),
                size = font_12,
                color = Red
            )
        }

        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(dimen_8)) {
            StatTile(Icons.Default.Anchor, crane.capacity_name.orEmpty(), stringResource(R.string.mp_crane_capacity), Modifier.weight(1f))
            StatTile(Icons.Default.Navigation, "${km(crane.distance_km)} km", stringResource(R.string.bk_distance), Modifier.weight(1f))
            StatTile(Icons.Default.Schedule, stringResource(R.string.bk_eta_min, crane.eta_minutes ?: 0), stringResource(R.string.bk_eta), Modifier.weight(1f))
        }

        crane.specifications?.let { specs ->
            Textview(stringResource(R.string.bk_specifications), size = font_15, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            GlassCard(contentPadding = dimen_12) {
                specs.entries.filter { !it.value.isNullOrBlank() }.chunked(2).forEach { pair ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = dimen_4)) {
                        pair.forEach { (key, value) ->
                            Column(modifier = Modifier.weight(1f)) {
                                Textview(key.replace('_', ' ').replaceFirstChar { it.uppercase() }, size = font_10, color = appColors.textSecondary)
                                Textview(value.orEmpty(), size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        crane.provider?.let { provider ->
            GlassCard(contentPadding = dimen_12) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(provider.company_name.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                        if (provider.verified == true) {
                            Spacer(modifier = Modifier.height(dimen_4))
                            BookingTag(stringResource(R.string.bk_verified_provider), SuccessGreen)
                        }
                    }
                    RatingRow(provider.rating, provider.reviews_count)
                }
            }
        }

        if (!crane.about.isNullOrBlank()) {
            Textview(stringResource(R.string.bk_about_crane), size = font_15, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            Textview(crane.about, size = font_12, color = appColors.textSecondary)
        }

        BookingDetailsCard(state, crane.booking_details?.duration_label, crane.booking_details?.start_at, onEditBooking)
        Spacer(modifier = Modifier.height(dimen_8))
    }
}

/** Where, when and for how long: shown on crane details and on the review screen. */
@Composable
private fun BookingDetailsCard(state: BookingUiState, durationLabel: String?, startAt: String?, onEdit: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Textview(
            stringResource(R.string.bk_your_booking_details),
            size = font_15, color = appColors.textPrimary, fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        CraneSmallButton(text = stringResource(R.string.common_edit), icon = Icons.Default.Edit, onClick = onEdit)
    }
    GlassCard(contentPadding = dimen_12) {
        DetailLine(Icons.Default.LocationOn, state.siteName.ifBlank { state.siteAddress }, state.siteAddress.takeIf { state.siteName.isNotBlank() })
        DetailLine(
            Icons.Default.CalendarMonth,
            if (state.startType == "now") stringResource(R.string.bk_now) else formatServerDateTime(startAt),
            null
        )
        DetailLine(Icons.Default.AccessTime, durationLabel.orEmpty(), null)
    }
}

@Composable
private fun DetailLine(icon: ImageVector, title: String, subtitle: String?) {
    Row(modifier = Modifier.padding(vertical = dimen_6), verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(dimen_16))
        Spacer(modifier = Modifier.width(dimen_10))
        Column {
            Textview(title, size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold)
            if (!subtitle.isNullOrBlank()) Textview(subtitle, size = font_11, color = appColors.textSecondary)
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Review Your Booking
// ---------------------------------------------------------------------------------------------

@Composable
private fun PriceBreakdown(price: BookingPrice?) {
    if (price == null) return
    val included = stringResource(R.string.bk_included)
    GlassCard(contentPadding = dimen_14) {
        Textview(stringResource(R.string.bk_price_breakdown), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(dimen_8))
        PriceLine(
            stringResource(R.string.bk_crane_rental_line, rupees(price.hourly_rate), (price.duration_hours ?: 0.0).toInt()),
            rupees(price.crane_rental)
        )
        PriceLine(
            stringResource(R.string.bk_operator_charges),
            if (price.operator_included == true) included else rupees(price.operator_charges),
            valueColor = if (price.operator_included == true) SuccessGreen else appColors.textPrimary
        )
        PriceLine(
            stringResource(R.string.bk_fuel_charges),
            if (price.fuel_included == true) included else rupees(price.fuel_charges),
            valueColor = if (price.fuel_included == true) SuccessGreen else appColors.textPrimary
        )
        PriceLine(stringResource(R.string.bk_other_charges), rupees(price.other_charges))
        // the crane owner's extra charges and GST, so the total below is what the customer really pays
        price.breakdown.orEmpty()
            .filter { it.key?.startsWith("charge_") == true || it.key == "gst" }
            .forEach { PriceLine(it.label.orEmpty(), rupees(it.amount)) }
        Spacer(modifier = Modifier.height(dimen_6))
        PriceLine(stringResource(R.string.bk_estimated_total), rupees(price.total_payable ?: price.total_amount), bold = true)
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
fun ReviewBookingScreen(
    vm: BookingViewModel? = null,
    onBack: () -> Unit = {},
    onChangeCrane: () -> Unit = {},
    onEditBooking: () -> Unit = {},
    onConfirm: () -> Unit = {}
) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value ?: BookingUiState(detail = previewCrane)
    val crane = state.detail
    LaunchedEffect(Unit) { vm?.onIntent(BookingIntent.LoadPolicy) }
    var policyOpen by remember { mutableStateOf(false) }

    BookingScaffold(
        onBack = onBack,
        title = stringResource(R.string.bk_review_title),
        bottom = { CommonGradientButton(text = stringResource(R.string.bk_continue_to_payment), onClick = onConfirm) }
    ) {
        if (crane == null) return@BookingScaffold

        Row(verticalAlignment = Alignment.CenterVertically) {
            Textview(stringResource(R.string.bk_selected_crane), size = font_15, color = appColors.textPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            CraneSmallButton(text = stringResource(R.string.bk_change), icon = Icons.Default.Edit, onClick = onChangeCrane)
        }
        GlassCard(contentPadding = dimen_12) {
            Row {
                CraneImage(path = crane.primary_photo, modifier = Modifier.size(dimen_80))
                Spacer(modifier = Modifier.width(dimen_12))
                Column {
                    Textview(crane.title.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                    RatingRow(crane.rating, crane.reviews_count)
                    if (crane.verified == true) BookingTag(stringResource(R.string.common_verified), SuccessGreen)
                    Textview(
                        listOfNotNull(crane.capacity_name, crane.fuel_type_name).joinToString("  •  "),
                        size = font_11, color = appColors.textSecondary
                    )
                    if (crane.operator_included == true) {
                        Textview(stringResource(R.string.bk_operator_included), size = font_11, color = appColors.textSecondary)
                    }
                }
            }
        }

        BookingDetailsCard(state, crane.booking_details?.duration_label, crane.booking_details?.start_at, onEditBooking)
        PriceBreakdown(crane.price)

        GlassCard(contentPadding = dimen_12, onClick = { policyOpen = !policyOpen }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Textview(
                    stringResource(R.string.bk_policy), size = font_13, color = appColors.textPrimary,
                    fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f)
                )
                Icon(
                    if (policyOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null, tint = appColors.textSecondary
                )
            }
            if (policyOpen) {
                Spacer(modifier = Modifier.height(dimen_8))
                Textview(
                    text = state.policy ?: stringResource(R.string.bk_policy_unavailable),
                    size = font_12, color = appColors.textSecondary
                )
            }
        }
        Spacer(modifier = Modifier.height(dimen_8))
    }
}

// ---------------------------------------------------------------------------------------------
// Complete Payment (Cash on Delivery only for now)
// ---------------------------------------------------------------------------------------------

@Preview(showBackground = true, heightDp = 900)
@Composable
fun PaymentScreen(
    vm: BookingViewModel? = null,
    onBack: () -> Unit = {},
    onConfirm: () -> Unit = {}
) {
    val state = vm?.uiState?.collectAsStateWithLifecycle()?.value ?: BookingUiState(detail = previewCrane)
    val crane = state.detail
    val price = crane?.price

    BookingScaffold(
        onBack = onBack,
        title = stringResource(R.string.bk_payment_title),
        bottom = {
            CommonGradientButton(text = stringResource(R.string.bk_confirm_booking), onClick = onConfirm, enabled = crane != null)
        }
    ) {
        if (crane == null) return@BookingScaffold

        GlassCard(contentPadding = dimen_12) {
            Row {
                CraneImage(path = crane.primary_photo, modifier = Modifier.size(dimen_70))
                Spacer(modifier = Modifier.width(dimen_12))
                Column {
                    Textview(crane.title.orEmpty(), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                    RatingRow(crane.rating, crane.reviews_count)
                    Textview(
                        listOfNotNull(crane.capacity_name, crane.fuel_type_name, crane.booking_details?.duration_label).joinToString("  •  "),
                        size = font_11, color = appColors.textSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(dimen_8))
            Textview(state.siteName.ifBlank { state.siteAddress }, size = font_12, color = appColors.textSecondary, maxLines = 1)
        }

        if (price != null) {
            GlassCard(contentPadding = dimen_14) {
                Textview(stringResource(R.string.bk_payment_amount), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(dimen_8))
                PriceLine(stringResource(R.string.bk_advance_due, price.advance_percent ?: 0), rupees(price.advance_amount), valueColor = AmberPrimary)
                PriceLine(stringResource(R.string.bk_remaining_amount), rupees(price.remaining_amount))
                PriceLine(stringResource(R.string.bk_total_amount), rupees(price.total_payable ?: price.total_amount))
                Spacer(modifier = Modifier.height(dimen_6))
                PriceLine(stringResource(R.string.bk_advance_due, price.advance_percent ?: 0), rupees(price.advance_amount), bold = true)
            }
        }

        Textview(stringResource(R.string.bk_select_payment_method), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
        val methods = state.options?.payment_methods.orEmpty()
        methods.forEach { method ->
            val enabled = method.enabled == true
            val selected = enabled && method.id == state.paymentMethod
            SelectableCard(
                selected = selected,
                onClick = { if (enabled) Unit },
                modifier = Modifier.fillMaxWidth(),
                minHeight = dimen_76
            ) {
                Row(modifier = Modifier.padding(dimen_14), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(
                            method.name.orEmpty(), size = font_14,
                            color = if (enabled) appColors.textPrimary else appColors.textHint,
                            fontWeight = FontWeight.Bold
                        )
                        Textview(
                            text = if (enabled) method.description.orEmpty() else stringResource(R.string.bk_coming_soon),
                            size = font_11, color = appColors.textSecondary
                        )
                    }
                    if (selected) SelectedCheck()
                }
            }
        }
        Textview(stringResource(R.string.bk_cod_note), size = font_11, color = appColors.textHint)
        Spacer(modifier = Modifier.height(dimen_8))
    }
}
