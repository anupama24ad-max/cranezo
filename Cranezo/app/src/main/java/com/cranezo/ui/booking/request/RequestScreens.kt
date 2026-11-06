package com.cranezo.ui.booking.request

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cranezo.R
import com.cranezo.ui.booking.BookingViewModel
import com.cranezo.ui.booking.components.BookingTag
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.GlassCard
import com.cranezo.ui.components.GlassInputField
import com.cranezo.ui.components.InlineConfirm
import com.cranezo.ui.components.Textview
import com.cranezo.ui.finance.BillLines
import com.cranezo.ui.finance.EmptyText
import com.cranezo.ui.finance.FinGreen
import com.cranezo.ui.finance.FinPage
import com.cranezo.ui.finance.FinRed
import com.cranezo.ui.finance.FinSection
import com.cranezo.ui.finance.MoneyRow
import com.cranezo.ui.finance.TextRow
import com.cranezo.ui.finance.amountText
import com.cranezo.ui.finance.trimNumber
import com.cranezo.ui.theme.*
import com.cranezo.utils.formatServerDateTime
import kotlinx.coroutines.delay

/** Default request time (15 minutes) used for the progress bar */
private const val REQUEST_SECONDS = 15 * 60

private fun mmss(seconds: Long): String = String.format(java.util.Locale.US, "%02d:%02d", seconds / 60, seconds % 60)

/** Counts down locally between server refreshes */
@Composable
private fun rememberCountdown(serverSeconds: Long?): Long {
    var left by remember(serverSeconds) { mutableStateOf(serverSeconds ?: 0L) }
    LaunchedEffect(serverSeconds) {
        while (left > 0) {
            delay(1000)
            left -= 1
        }
    }
    return left
}

// =============================================================================================
// Review & price: replaces the list of available cranes
// =============================================================================================

@Composable
fun OfferScreen(bookingVm: BookingViewModel, vm: RequestViewModel, onBack: () -> Unit, onEdit: () -> Unit, onSent: () -> Unit, onClose: () -> Unit) {
    val state by vm.ui.collectAsStateWithLifecycle()
    val body = remember { bookingVm.requestBody() }
    LaunchedEffect(Unit) { vm.estimate(body) }
    val e = state.estimate
    val (capacityName, typeName) = remember { bookingVm.chosenNames() }

    var offer by rememberSaveable { mutableStateOf<Double?>(null) }
    var autoRaise by rememberSaveable { mutableStateOf(false) }
    var autoMax by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(e) {
        if (e != null && offer == null) {
            offer = e.suggested_offer ?: e.min_offer
            val step = e.raise_step ?: 100.0
            autoMax = trimNumber(((e.suggested_offer ?: e.min_offer ?: 0.0) + step * 3))
        }
    }
    val minOffer = e?.min_offer ?: 0.0
    val step = e?.raise_step ?: 100.0

    val booking by bookingVm.uiState.collectAsStateWithLifecycle()
    val showFind = e != null && (e.found ?: 0) > 0
    MapSheetPage(
        map = { m -> SiteMap(booking.latitude, booking.longitude, booking.siteAddress, m, radiusKm = e?.search_radius_km) },
        onBack = onBack,
        bottom = {
            if (showFind) {
                CommonGradientButton(
                    text = if (state.busy) stringResource(R.string.rq_sending) else stringResource(R.string.rq_find_crane),
                    enabled = !state.busy && (offer ?: 0.0) >= minOffer,
                    onClick = { vm.create(body, offer ?: minOffer, autoRaise, autoMax.toDoubleOrNull(), onSent) }
                )
            }
        }
    ) {
        Textview(stringResource(R.string.rq_review_title), size = font_22, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
        Textview(listOf(capacityName, typeName).filter { it.isNotBlank() }.joinToString(" "), size = font_13, color = appColors.textSecondary)
        if (state.estimating || (e == null && !state.estimateFailed)) {
            Box(modifier = Modifier.fillMaxWidth().padding(dimen_32), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AmberPrimary, strokeWidth = dimen_3)
            }
            return@MapSheetPage
        }
        if (e == null) {
            EmptyText(stringResource(R.string.crane_msg_generic_error))
            CommonGradientButton(text = stringResource(R.string.rq_try_again), onClick = { vm.estimate(body) })
            return@MapSheetPage
        }

        // ---- no crane within the radius: send to the team ----
        if ((e.found ?: 0) == 0) {
            if (state.sentToTeam) {
                FinSection(stringResource(R.string.rq_sent_team_title)) {
                    Textview(stringResource(R.string.rq_sent_team_body), size = font_14, color = appColors.textPrimary)
                }
                CommonGradientButton(text = stringResource(R.string.rq_done), onClick = onClose)
                return@MapSheetPage
            }
            FinSection(stringResource(R.string.rq_none_title)) {
                Textview(
                    stringResource(R.string.rq_none_body, trimNumber(e.search_radius_km ?: 20.0)),
                    size = font_14, color = appColors.textSecondary
                )
                Spacer(modifier = Modifier.height(dimen_10))
                GlassInputField(
                    label = stringResource(R.string.rq_note_label), value = note, onValueChange = { note = it.take(500) },
                    placeholder = stringResource(R.string.rq_note_hint), singleLine = false, minHeight = dimen_88
                )
            }
            CommonGradientButton(text = stringResource(R.string.rq_send_team), enabled = !state.busy, onClick = { vm.sendToTeam(note, body) })
            CommonGradientButton(
                text = stringResource(R.string.rq_change_details), onClick = onEdit,
                gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = appColors.textPrimary
            )
            return@MapSheetPage
        }

        // ---- price ----
        FinSection(stringResource(R.string.rq_cranes_near, e.found ?: 0)) {
            e.price_range?.let { r ->
                TextRow(
                    stringResource(R.string.rq_usual_price, e.hours ?: 0),
                    if (r.min == r.max) amountText(r.min) else "${amountText(r.min)} – ${amountText(r.max)}"
                )
            }
            e.distance_charge_range?.let { r ->
                TextRow(
                    stringResource(R.string.rq_distance_charge, trimNumber(e.free_km ?: 7.0)),
                    if ((r.max ?: 0.0) <= 0.0) stringResource(R.string.rq_free) else "${amountText(r.min)} – ${amountText(r.max)}"
                )
            }
            Textview(e.note.orEmpty(), size = font_12, color = appColors.textSecondary, modifier = Modifier.padding(top = dimen_6))
        }

        FinSection(stringResource(R.string.rq_your_offer)) {
            Textview(stringResource(R.string.rq_offer_hint), size = font_12, color = appColors.textSecondary)
            Spacer(modifier = Modifier.height(dimen_10))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                RoundButton(Icons.Filled.Remove, enabled = (offer ?: 0.0) - step >= minOffer) { offer = (offer ?: minOffer) - step }
                Textview(
                    amountText(offer), size = font_30, color = appColors.textPrimary, fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center, modifier = Modifier.weight(1f)
                )
                RoundButton(Icons.Filled.Add, enabled = true) { offer = (offer ?: minOffer) + step }
            }
            Textview(
                stringResource(R.string.rq_min_offer, amountText(minOffer)), size = font_12, color = appColors.textSecondary,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = dimen_4)
            )
        }

        FinSection {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Textview(stringResource(R.string.rq_auto_raise), size = font_15, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                    Textview(stringResource(R.string.rq_auto_raise_hint, trimNumber(e.raise_after_minutes ?: 2.0)), size = font_12, color = appColors.textSecondary)
                }
                Switch(checked = autoRaise, onCheckedChange = { autoRaise = it }, colors = SwitchDefaults.colors(checkedTrackColor = AmberPrimary))
            }
            if (autoRaise) {
                Spacer(modifier = Modifier.height(dimen_8))
                GlassInputField(
                    label = stringResource(R.string.rq_auto_raise_max), value = autoMax,
                    onValueChange = { autoMax = it.filter { c -> c.isDigit() } }, placeholder = "0", keyboardType = KeyboardType.Number
                )
            }
        }

        Textview(stringResource(R.string.rq_send_note, trimNumber(e.payment_window_minutes ?: 10.0)), size = font_12, color = appColors.textSecondary)
    }
}

@Composable
private fun RoundButton(icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(dimen_48).clip(CircleShape)
            .background(if (enabled) AmberPrimary else appColors.subtleFill)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icon, contentDescription = null, tint = if (enabled) androidx.compose.ui.graphics.Color.Black else appColors.textHint) }
}

// =============================================================================================
// The request: searching -> owner accepted (pay advance, countdown) -> booked / expired
// =============================================================================================

@Composable
fun RequestStatusScreen(
    vm: RequestViewModel,
    onClose: () -> Unit,
    onBooked: (String) -> Unit,
    onTryAgain: () -> Unit,
    onEditBooking: () -> Unit = onTryAgain
) {
    val state by vm.ui.collectAsStateWithLifecycle()
    val r = state.request
    // live: every 4 seconds while something can change
    LaunchedEffect(r?.request_id, r?.status) {
        if (r == null) vm.loadActive()
        while (r == null || r.status == "searching" || r.status == "accepted") {
            delay(4000)
            vm.refresh()
        }
    }
    var confirmCancel by rememberSaveable { mutableStateOf(false) }
    var note by rememberSaveable { mutableStateOf("") }

    // Back: booked -> close; still running -> ask to cancel first; finished -> step 4 to book again
    val goBack: () -> Unit = {
        when {
            r == null || r.status == "confirmed" -> onClose()
            r.can_cancel == true -> confirmCancel = true
            else -> onEditBooking()
        }
    }
    androidx.activity.compose.BackHandler { goBack() }
    MapSheetPage(
        map = { m -> SiteMap(r?.site?.latitude, r?.site?.longitude, r?.site?.address, m, radiusKm = if (r?.status == "searching") 20.0 else null) },
        overlay = { if (r?.status == "searching") RadarPulse(Modifier.size(dimen_220)) },
        onBack = goBack
    ) {
        if (r == null) {
            Box(modifier = Modifier.fillMaxWidth().padding(dimen_32), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AmberPrimary, strokeWidth = dimen_3)
            }
            return@MapSheetPage
        }
        // what was asked
        FinSection(r.crane_title) {
            TextRow(stringResource(R.string.fin_where), r.site?.name ?: r.site?.address)
            TextRow(stringResource(R.string.fin_when), formatServerDateTime(r.schedule?.start_at))
            TextRow(stringResource(R.string.fin_duration), r.duration?.label)
            MoneyRow(stringResource(R.string.rq_your_offer), r.offer_amount, bold = true)
        }

        when (r.status) {
            "searching" -> SearchingBlock(vm, r, state.busy)
            "accepted" -> AcceptedBlock(vm, r, state.busy)
            "confirmed" -> {
                FinSection(stringResource(R.string.rq_booked_title)) {
                    Textview(stringResource(R.string.rq_booked_body, r.booking?.owner_name.orEmpty()), size = font_14, color = appColors.textPrimary)
                }
                r.booking?.booking_id?.let { id -> CommonGradientButton(text = stringResource(R.string.rq_view_booking), onClick = { onBooked(id) }) }
            }
            "expired", "payment_expired" -> {
                FinSection(r.status_label) {
                    Textview(
                        stringResource(if (r.status == "expired") R.string.rq_expired_body else R.string.rq_payment_expired_body),
                        size = font_14, color = appColors.textSecondary
                    )
                }
                CommonGradientButton(text = stringResource(R.string.rq_try_higher), onClick = { vm.clearRequest(); onTryAgain() })
                if (r.can_send_to_team == true) {
                    GlassInputField(
                        label = stringResource(R.string.rq_note_label), value = note, onValueChange = { note = it.take(500) },
                        placeholder = stringResource(R.string.rq_note_hint), singleLine = false, minHeight = dimen_88
                    )
                    CommonGradientButton(
                        text = stringResource(R.string.rq_send_team), enabled = !state.busy, onClick = { vm.sendToTeam(note, null) },
                        gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = appColors.textPrimary
                    )
                }
            }
            else -> {
                FinSection(r.status_label) {
                    Textview(
                        stringResource(if (r.status == "not_found") R.string.rq_sent_team_body else R.string.rq_cancelled_body),
                        size = font_14, color = appColors.textSecondary
                    )
                }
                CommonGradientButton(text = stringResource(R.string.rq_done), onClick = { vm.clearRequest(); onClose() })
            }
        }

        if (r.can_cancel == true) {
            if (confirmCancel) {
                // why: one of 4 reasons, then cancel
                CancelReasons(
                    reasons = r.cancel_reasons.orEmpty(),
                    busy = state.busy,
                    onConfirm = { reason -> confirmCancel = false; vm.cancel(reason, onEditBooking) },
                    onKeep = { confirmCancel = false }
                )
            } else {
                Textview(
                    stringResource(R.string.rq_cancel_request), size = font_14, color = FinRed, fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().clickable { confirmCancel = true }.padding(vertical = dimen_10)
                )
            }
        }
        Spacer(modifier = Modifier.height(dimen_16))
    }
}

@Composable
private fun SearchingBlock(vm: RequestViewModel, r: CraneRequestView, busy: Boolean) {
    val left = rememberCountdown(r.seconds_left)
    // Rapido style: "0 of 12 owners accepted yet" with a bar that fills as the request time runs out
    SearchingHeader(
        title = stringResource(R.string.rq_owners_not_yet, r.owners_notified ?: 0),
        progress = 1f - (left.toFloat() / (REQUEST_SECONDS).toFloat())
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Textview(stringResource(R.string.rq_searching_title), size = font_13, color = appColors.textSecondary, modifier = Modifier.weight(1f))
        Textview(mmss(left), size = font_16, color = AmberPrimary, fontWeight = FontWeight.Bold)
    }
    FinSection(stringResource(R.string.rq_no_answer_yet)) {
        if (r.auto_raise == true) {
            Textview(stringResource(R.string.rq_auto_on, amountText(r.auto_raise_max)), size = font_13, color = FinGreen)
            Spacer(modifier = Modifier.height(dimen_6))
        }
        val step = r.raise_step ?: 0.0
        CommonGradientButton(
            text = stringResource(R.string.rq_raise_by, amountText(step)),
            enabled = r.can_raise == true && !busy,
            onClick = { vm.raise() }
        )
        if (r.can_raise != true) {
            Textview(stringResource(R.string.rq_raise_wait), size = font_12, color = appColors.textSecondary, modifier = Modifier.padding(top = dimen_6))
        }
        if ((r.raise_count ?: 0) > 0) {
            Textview(stringResource(R.string.rq_raised_times, r.raise_count ?: 0, amountText(r.first_offer_amount)), size = font_12, color = appColors.textHint, modifier = Modifier.padding(top = dimen_4))
        }
    }
}

@Composable
private fun AcceptedBlock(vm: RequestViewModel, r: CraneRequestView, busy: Boolean) {
    val b = r.booking ?: return
    val left = rememberCountdown(b.payment_seconds_left)
    GlassCard(contentPadding = dimen_16, borderColor = FinGreen) {
        Textview(stringResource(R.string.rq_accepted_title, b.owner_name.orEmpty()), size = font_16, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
        Textview(listOfNotNull(b.crane?.title, b.crane?.model_name).joinToString(" · "), size = font_12, color = appColors.textSecondary)
        Spacer(modifier = Modifier.height(dimen_10))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Textview(stringResource(R.string.rq_pay_within), size = font_12, color = appColors.textSecondary)
                Textview(mmss(left), size = font_26, color = if (left < 120) FinRed else AmberPrimary, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Textview(stringResource(R.string.rq_advance_pct, b.advance_percent ?: 0), size = font_12, color = appColors.textSecondary)
                Textview(amountText(b.advance_amount), size = font_22, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
    // last 2 minutes: "Need more time?" with +5 minutes, once
    if (left in 1L..120L && b.payment_extended != true) {
        GlassCard(contentPadding = dimen_14, borderColor = AmberPrimary) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Textview(stringResource(R.string.rq_more_time_title), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                    Textview(stringResource(R.string.rq_more_time_sub), size = font_12, color = appColors.textSecondary)
                }
                Spacer(modifier = Modifier.width(dimen_8))
                CommonGradientButton(
                    text = stringResource(R.string.rq_more_time_button), enabled = !busy, onClick = { vm.extendPayment() },
                    modifier = Modifier.width(dimen_110), height = dimen_40, textSize = font_12
                )
            }
        }
    }
    FinSection(stringResource(R.string.fin_bill_breakdown)) {
        BillLines(b.bill.orEmpty())
        MoneyRow(stringResource(R.string.rq_advance_now), b.advance_amount, color = AmberPrimary)
        MoneyRow(stringResource(R.string.rq_balance_later), b.balance_after_advance)
    }
    if (b.test_payment == true) {
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(dimen_10)).background(appColors.subtleFill).padding(dimen_10)
        ) { Textview(stringResource(R.string.rq_test_payment_note), size = font_12, color = appColors.textSecondary) }
    }
    // the bill includes the online payment fee (Razorpay)
    if (b.bill.orEmpty().any { it.key == "gateway_fee" }) {
        Textview(stringResource(R.string.rq_gateway_note), size = font_11, color = appColors.textSecondary)
    }
    CommonGradientButton(
        text = if (busy) stringResource(R.string.rq_paying) else stringResource(R.string.rq_pay_advance, amountText(b.advance_amount)),
        enabled = !busy && left > 0,
        onClick = { vm.payAdvance() }
    )
}

// =============================================================================================
// Owner: requests near my cranes - accept and the job is mine
// =============================================================================================

@Composable
fun OwnerCraneRequestsScreen(vm: RequestViewModel, onBack: () -> Unit, onAccepted: (String) -> Unit) {
    val state by vm.ui.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        vm.loadOwnerRequests()
        while (true) {
            delay(10_000)
            vm.loadOwnerRequests(quiet = true)
        }
    }
    LaunchedEffect(state.acceptedBookingId) { if (state.acceptedBookingId != null) vm.consumeAccepted()?.let(onAccepted) }
    val list = state.ownerList

    FinPage(stringResource(R.string.rq_owner_title), onBack, subtitle = stringResource(R.string.rq_owner_sub)) {
        if (state.ownerLoaded && (list?.cranes ?: 0) == 0) {
            EmptyText(stringResource(R.string.rq_owner_no_cranes))
            return@FinPage
        }
        if (state.ownerLoaded && list?.items.isNullOrEmpty()) EmptyText(stringResource(R.string.rq_owner_none))
        list?.items.orEmpty().forEach { item ->
            FinSection {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Textview(item.crane_title.orEmpty(), size = font_16, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                        Textview(item.request_code.orEmpty(), size = font_11, color = appColors.textHint)
                    }
                    if (item.raised == true) BookingTag(stringResource(R.string.rq_price_raised), FinGreen)
                }
                Spacer(modifier = Modifier.height(dimen_6))
                TextRow(stringResource(R.string.fin_where), item.site?.name ?: item.site?.address)
                TextRow(stringResource(R.string.fin_when), formatServerDateTime(item.schedule?.start_at))
                TextRow(stringResource(R.string.fin_duration), item.duration_label)
                if (!item.special_requirements.isNullOrBlank()) TextRow(stringResource(R.string.rq_note_label), item.special_requirements)
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = dimen_8).clip(RoundedCornerShape(dimen_12))
                        .background(FinGreen.copy(alpha = 0.12f)).padding(dimen_12)
                ) {
                    Column {
                        Textview(stringResource(R.string.rq_customer_offers), size = font_12, color = appColors.textSecondary)
                        Textview(amountText(item.offer_amount), size = font_24, color = FinGreen, fontWeight = FontWeight.Bold)
                    }
                }
                item.cranes.orEmpty().forEach { c ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = dimen_6)) {
                        Textview(
                            listOfNotNull(c.crane_code, c.registration_number, c.distance_km?.let { stringResource(R.string.fin_km_away, trimNumber(it)) }).joinToString(" · "),
                            size = font_13, color = appColors.textPrimary, fontWeight = FontWeight.SemiBold
                        )
                        Textview(
                            stringResource(R.string.rq_your_usual, amountText(c.your_usual_price)) +
                                ((c.distance_charge ?: 0.0).takeIf { it > 0 }?.let { " · " + stringResource(R.string.rq_plus_distance, amountText(it)) } ?: ""),
                            size = font_12, color = appColors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(dimen_6))
                        CommonGradientButton(
                            text = stringResource(R.string.rq_accept_with, c.crane_code.orEmpty()),
                            height = dimen_48, enabled = !state.busy,
                            onClick = { if (item.request_id != null && c.crane_id != null) vm.accept(item.request_id, c.crane_id) }
                        )
                    }
                }
                Textview(
                    stringResource(R.string.rq_owner_rule, trimNumber(list?.payment_window_minutes ?: 10.0)),
                    size = font_11, color = appColors.textHint, modifier = Modifier.padding(top = dimen_4)
                )
            }
        }
        Spacer(modifier = Modifier.height(dimen_16))
    }
}

/** Cancel a request: pick one of the 4 reasons, then confirm (or keep the request) */
@Composable
private fun CancelReasons(reasons: List<CancelReason>, busy: Boolean, onConfirm: (String?) -> Unit, onKeep: () -> Unit) {
    var picked by rememberSaveable { mutableStateOf<String?>(null) }
    GlassCard(contentPadding = dimen_14, borderColor = FinRed) {
        Textview(stringResource(R.string.rq_cancel_why), size = font_14, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(dimen_6))
        reasons.forEach { r ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { picked = r.id }.padding(vertical = dimen_2),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.RadioButton(
                    selected = picked == r.id, onClick = { picked = r.id },
                    colors = androidx.compose.material3.RadioButtonDefaults.colors(selectedColor = AmberPrimary)
                )
                Textview(r.name.orEmpty(), size = font_13, color = appColors.textPrimary)
            }
        }
        Spacer(modifier = Modifier.height(dimen_8))
        Row(horizontalArrangement = Arrangement.spacedBy(dimen_10)) {
            CommonGradientButton(
                text = stringResource(R.string.rq_cancel_keep), modifier = Modifier.weight(1f), onClick = onKeep, height = dimen_44, textSize = font_13,
                gradientColors = listOf(appColors.subtleFill, appColors.subtleFill), textColor = appColors.textPrimary
            )
            CommonGradientButton(
                text = stringResource(R.string.rq_cancel_yes), modifier = Modifier.weight(1f), height = dimen_44, textSize = font_13,
                enabled = !busy && (picked != null || reasons.isEmpty()),
                gradientColors = listOf(FinRed, FinRed), textColor = androidx.compose.ui.graphics.Color.White,
                onClick = { onConfirm(picked) }
            )
        }
    }
}
