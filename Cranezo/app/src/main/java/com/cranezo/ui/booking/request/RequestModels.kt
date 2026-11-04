package com.cranezo.ui.booking.request

import com.cranezo.ui.finance.model.BillLine

/** Rapido-style booking request (customer/crane-requests/... and owner/crane-requests). Field names match the API. */

data class AmountRange(val min: Double? = null, val max: Double? = null)

data class RequestEstimate(
    val found: Int? = null,
    val search_radius_km: Double? = null,
    val hours: Int? = null,
    val price_range: AmountRange? = null,
    val distance_charge_range: AmountRange? = null,
    val free_km: Double? = null,
    val min_offer: Double? = null,
    val suggested_offer: Double? = null,
    val raise_step: Double? = null,
    val raise_after_minutes: Double? = null,
    val request_minutes: Double? = null,
    val payment_window_minutes: Double? = null,
    val note: String? = null
)

data class RequestSite(val name: String? = null, val address: String? = null)
data class RequestDuration(val label: String? = null, val hours: Int? = null)
data class RequestSchedule(val start_type: String? = null, val start_at: String? = null, val end_at: String? = null)
data class RequestCrane(val title: String? = null, val model_name: String? = null, val primary_photo: String? = null, val rating: Double? = null)

data class RequestBooking(
    val booking_id: String? = null,
    val booking_code: String? = null,
    val status: Int? = null,
    val owner_name: String? = null,
    val crane: RequestCrane? = null,
    val bill: List<BillLine>? = null,
    val total: Double? = null,
    val advance_percent: Int? = null,
    val advance_amount: Double? = null,
    val balance_after_advance: Double? = null,
    val payment_due_at: String? = null,
    val payment_seconds_left: Long? = null,
    val advance_paid_at: String? = null,
    val test_payment: Boolean? = null
)

data class CraneRequestView(
    val request_id: String? = null,
    val request_code: String? = null,
    val status: String? = null,
    val status_label: String? = null,
    val crane_title: String? = null,
    val site: RequestSite? = null,
    val schedule: RequestSchedule? = null,
    val duration: RequestDuration? = null,
    val offer_amount: Double? = null,
    val first_offer_amount: Double? = null,
    val min_offer_amount: Double? = null,
    val estimate: AmountRange? = null,
    val owners_notified: Int? = null,
    val raise_count: Int? = null,
    val auto_raise: Boolean? = null,
    val auto_raise_max: Double? = null,
    val raise_step: Double? = null,
    val can_raise: Boolean? = null,
    val can_raise_at: String? = null,
    val expires_at: String? = null,
    val seconds_left: Long? = null,
    val can_cancel: Boolean? = null,
    val can_send_to_team: Boolean? = null,
    val booking: RequestBooking? = null
)

// ---- owner side ----

data class OwnerRequestCrane(
    val crane_id: String? = null,
    val crane_code: String? = null,
    val title: String? = null,
    val registration_number: String? = null,
    val primary_photo: String? = null,
    val distance_km: Double? = null,
    val your_usual_price: Double? = null,
    val offer_vs_usual: Double? = null,
    val distance_charge: Double? = null,
    val customer_total: Double? = null,
    val advance_amount: Double? = null
)

data class OwnerRequestItem(
    val request_id: String? = null,
    val request_code: String? = null,
    val crane_title: String? = null,
    val site: RequestSite? = null,
    val schedule: RequestSchedule? = null,
    val duration_label: String? = null,
    val hours: Int? = null,
    val special_requirements: String? = null,
    val offer_amount: Double? = null,
    val raised: Boolean? = null,
    val seconds_left: Long? = null,
    val cranes: List<OwnerRequestCrane>? = null
)

data class OwnerRequestList(
    val items: List<OwnerRequestItem>? = null,
    val cranes: Int? = null,
    val advance_percent: Int? = null,
    val payment_window_minutes: Double? = null
)

/** Accepting returns the new booking (owner view); only its id is needed */
data class AcceptedBooking(val booking_id: String? = null, val booking_code: String? = null)
