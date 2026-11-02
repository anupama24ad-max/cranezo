package com.cranezo.ui.booking.model

import com.cranezo.ui.cranes.model.CranePhoto

/** Response models of the Book a Crane / Crane Requests / Notifications APIs. All fields nullable. */

// ---- POST /customer/booking/options ----

data class BookingOptions(
    val crane_types: List<BookingChoice>? = null,
    val capacities: List<BookingChoice>? = null,
    val start_types: List<StartTypeOption>? = null,
    val duration_types: List<DurationTypeOption>? = null,
    val payment_methods: List<PaymentMethodOption>? = null,
    val default_payment_method: String? = null,
    val advance_percent: Int? = null,
    val search_radius_km: Int? = null
)

data class BookingChoice(
    val id: Int? = null,
    val name: String? = null,
    val description: String? = null,
    val image: String? = null
)

data class StartTypeOption(val id: String? = null, val name: String? = null, val description: String? = null)

data class DurationTypeOption(
    val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val min: Int? = null,
    val max: Int? = null,
    val default: Int? = null,
    val hours: Int? = null
)

data class PaymentMethodOption(
    val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val enabled: Boolean? = null
)

// ---- Price, crane (search + details) ----

data class BookingPrice(
    val hourly_rate: Double? = null,
    val duration_hours: Double? = null,
    val crane_rental: Double? = null,
    val operator_charges: Double? = null,
    val fuel_charges: Double? = null,
    val other_charges: Double? = null,
    val operator_included: Boolean? = null,
    val fuel_included: Boolean? = null,
    val total_amount: Double? = null,
    val advance_percent: Int? = null,
    val advance_amount: Double? = null,
    val remaining_amount: Double? = null
)

data class ProviderInfo(
    val id: String? = null,
    val company_name: String? = null,
    val verified: Boolean? = null,
    val rating: Double? = null,
    val reviews_count: Int? = null
)

data class AvailableCrane(
    val id: String? = null,
    val crane_code: String? = null,
    val title: String? = null,
    val model_name: String? = null,
    val crane_type_name: String? = null,
    val capacity_name: String? = null,
    val fuel_type_name: String? = null,
    val primary_photo: String? = null,
    val rating: Double? = null,
    val reviews_count: Int? = null,
    val verified: Boolean? = null,
    val operator_included: Boolean? = null,
    val availability_label: String? = null,
    val distance_km: Double? = null,
    val eta_minutes: Int? = null,
    val hourly_rate: Double? = null,
    val minimum_hours: Int? = null,
    val price: BookingPrice? = null,
    // details only
    val bookable: Boolean? = null,
    val not_bookable_reason: String? = null,
    val photos: List<CranePhoto>? = null,
    val specifications: Map<String, String?>? = null,
    val about: String? = null,
    val provider: ProviderInfo? = null,
    val booking_details: BookingDetailsInfo? = null
)

data class BookingDetailsInfo(
    val site_address: String? = null,
    val start_type: String? = null,
    val start_at: String? = null,
    val end_at: String? = null,
    val duration_label: String? = null
)

// ---- The booking object ----

data class BookingCrane(
    val id: String? = null,
    val crane_code: String? = null,
    val title: String? = null,
    val model_name: String? = null,
    val crane_type_name: String? = null,
    val capacity_name: String? = null,
    val fuel_type_name: String? = null,
    val primary_photo: String? = null,
    val rating: Double? = null,
    val reviews_count: Int? = null,
    val verified: Boolean? = null,
    val operator_included: Boolean? = null
)

data class BookingOperator(
    val id: String? = null,
    val member_id: String? = null,
    val full_name: String? = null,
    val profile_image: String? = null,
    val mobile_number: String? = null,
    val country_code: String? = null,
    val rating: Double? = null,
    val reviews_count: Int? = null
)

data class BookingSite(
    val name: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class BookingSchedule(val start_type: String? = null, val start_at: String? = null, val end_at: String? = null)

data class BookingDuration(
    val type: String? = null,
    val service_type: String? = null,
    val hours: Double? = null,
    val days: Int? = null,
    val label: String? = null
)

data class BookingPayment(
    val method: String? = null,
    val method_name: String? = null,
    val status: String? = null,
    val total_amount: Double? = null,
    val advance_percent: Int? = null,
    val advance_amount: Double? = null,
    val remaining_amount: Double? = null,
    val paid_amount: Double? = null
)

data class Booking(
    val booking_id: String? = null,
    val booking_code: String? = null,
    val status: Int? = null,
    val status_label: String? = null,
    val status_message: String? = null,
    val can_cancel: Boolean? = null,
    val crane: BookingCrane? = null,
    val provider: ProviderInfo? = null,
    val operator: BookingOperator? = null,
    val site: BookingSite? = null,
    val schedule: BookingSchedule? = null,
    val duration: BookingDuration? = null,
    val distance_km: Double? = null,
    val eta_minutes: Int? = null,
    val price: BookingPrice? = null,
    val payment: BookingPayment? = null,
    val request_expires_at: String? = null,
    val seconds_left: Int? = null,
    val decline_reason: String? = null,
    val cancel_reason: String? = null,
    val created_at: String? = null
)

object BookingStatus {
    const val AWAITING = 0
    const val CONFIRMED = 1
    const val DECLINED = 2
    const val EXPIRED = 3
    const val CANCELLED = 4
}

// ---- POST /customer/booking/track ----

data class CraneLocation(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val is_live: Boolean? = null,
    val updated_at: String? = null
)

data class TrackInfo(
    val booking_id: String? = null,
    val booking_code: String? = null,
    val status: Int? = null,
    val status_label: String? = null,
    val status_message: String? = null,
    val crane: BookingCrane? = null,
    val operator: BookingOperator? = null,
    val site: BookingSite? = null,
    val crane_location: CraneLocation? = null,
    val distance_km: Double? = null,
    val eta_minutes: Int? = null,
    val expected_arrival_at: String? = null,
    val start_at: String? = null
)

// ---- Owner: Crane Requests ----

data class RequestJobLocation(
    val name: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val distance_km: Double? = null
)

data class RequestCustomer(
    val full_name: String? = null,
    val member_id: String? = null,
    val mobile_number: String? = null,
    val country_code: String? = null
)

data class OwnerRequest(
    val booking_id: String? = null,
    val booking_code: String? = null,
    val status: Int? = null,
    val status_label: String? = null,
    val can_respond: Boolean? = null,
    val seconds_left: Int? = null,
    val crane: BookingCrane? = null,
    val job_location: RequestJobLocation? = null,
    val schedule: BookingSchedule? = null,
    val duration: BookingDuration? = null,
    val approx_earnings: Double? = null,
    val earnings: BookingPrice? = null,
    val payment: BookingPayment? = null,
    val customer: RequestCustomer? = null,
    val operator: BookingOperator? = null,
    val next_steps: String? = null
)

// ---- Notifications ----

data class AppNotification(
    val id: String? = null,
    val type: String? = null,
    val title: String? = null,
    val body: String? = null,
    val data: Map<String, Any?>? = null,
    val is_read: Boolean? = null
)

data class UnreadCount(val unread: Int? = null)
