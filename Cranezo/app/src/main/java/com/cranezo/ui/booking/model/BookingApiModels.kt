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
    val remaining_amount: Double? = null,
    // what the customer will actually pay: crane hire + owner charges + GST
    val total_payable: Double? = null,
    val breakdown: List<com.cranezo.ui.finance.model.BillLine>? = null
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
    val operator_included: Boolean? = null,
    val capacity_label: String? = null,
    val registration_number: String? = null
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
    val paid_amount: Double? = null,
    val status_label: String? = null,
    val booking_amount: Double? = null,
    val booking_hours: Double? = null,
    val extra_minutes: Int? = null,
    val extra_time_amount: Double? = null,
    val final_total: Double? = null,
    val balance_due: Double? = null,
    val payment_reference: String? = null,
    val paid_at: String? = null,
    val payment_methods: List<PaymentMethodOption>? = null,
    // full bill: crane hire, extra time, owner charges, GST, total
    val breakdown: List<com.cranezo.ui.finance.model.BillLine>? = null
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
    val created_at: String? = null,
    val tab: String? = null,
    val tab_label: String? = null,
    val stage: String? = null,
    val job: BookingJobInfo? = null,
    val timeline: List<com.cranezo.ui.ownerjobs.model.TimelineStep>? = null,
    val summary_rows: SummaryRows? = null,
    val timers: com.cranezo.ui.operator.model.JobTimers? = null,
    val usage: UsageInfo? = null,
    val extension: ExtensionInfo? = null,
    val job_summary: JobSummaryInfo? = null,
    val receipt: ReceiptInfo? = null,
    // every payment of the booking (advance, final, failed attempts)
    val transactions: List<com.cranezo.ui.finance.model.TransactionItem>? = null,
    val cancellation: CancellationInfo? = null,
    val rebook: RebookInfo? = null,
    @com.google.gson.annotations.SerializedName("rating") val rating_info: RatingInfo? = null,
    val actions: BookingActions? = null
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

// ---- After the booking is confirmed: the job, extra time, payment, rating ----

data class BookingJobInfo(
    val stage: String? = null,
    val stage_label: String? = null,
    val en_route_at: String? = null,
    val arrived_at: String? = null,
    val can_confirm_arrival: Boolean? = null,
    val arrival_confirmed_at: String? = null,
    val work_started_at: String? = null,
    val work_ended_at: String? = null,
    val completion_otp: String? = null,
    val completion_otp_expires_at: String? = null,
    val completed_at: String? = null,
    val final_amount: Double? = null
)

data class SummaryRows(
    val booking_date: String? = null,
    val location: String? = null,
    val estimated_duration: String? = null,
    val estimated_amount: Double? = null,
    val payment_status: String? = null
)

data class CurrentEstimate(val amount: Double? = null, val hourly_rate: Double? = null, val note: String? = null)

data class UsageInfo(
    val started_at: String? = null,
    val elapsed_seconds: Long? = null,
    val booked_seconds: Long? = null,
    val used_seconds: Long? = null,
    val remaining_seconds: Long? = null,
    val overtime_seconds: Long? = null,
    val percent_used: Double? = null,
    val overtime_warning: Boolean? = null,
    val current_estimate: CurrentEstimate? = null
)

data class ExtensionRequest(
    val id: String? = null,
    val code: String? = null,
    val minutes: Int? = null,
    val label: String? = null,
    val amount: Double? = null,
    val hourly_rate: Double? = null,
    val message: String? = null,
    val status: Int? = null,
    val status_label: String? = null,
    val requested_at: String? = null,
    val expires_at: String? = null,
    val seconds_left: Int? = null,
    val responded_at: String? = null,
    val reject_reason: String? = null,
    val current_start_at: String? = null,
    val current_end_at: String? = null,
    val new_end_at: String? = null
)

data class ExtensionInfo(
    val pending: ExtensionRequest? = null,
    val approved_minutes: Int? = null,
    val approved_amount: Double? = null,
    val history: List<ExtensionRequest>? = null,
    val can_request: Boolean? = null,
    val options: List<Int>? = null,
    val step_minutes: Int? = null,
    val max_minutes: Int? = null,
    val hourly_rate: Double? = null,
    val response_minutes: Int? = null
)

data class ExtensionResult(val extension: ExtensionRequest? = null, val booking: Booking? = null)

data class JobSummaryInfo(val started_at: String? = null, val completed_at: String? = null, val total_usage_seconds: Long? = null)

data class ReceiptParty(val name: String? = null, val phone: String? = null, val gst_number: String? = null)

data class ReceiptService(
    val crane: String? = null,
    val registration_number: String? = null,
    val site: String? = null,
    val started_at: String? = null,
    val completed_at: String? = null,
    val booked_hours: Double? = null,
    val worked_seconds: Long? = null,
    val extra_minutes: Int? = null,
    val operator: String? = null
)

data class ReceiptPayment(val label: String? = null, val amount: Double? = null, val method: String? = null, val reference: String? = null, val paid_at: String? = null)

data class ReceiptInfo(
    // full receipt
    val receipt_no: String? = null,
    val booking_code: String? = null,
    val issued_at: String? = null,
    val billed_to: ReceiptParty? = null,
    val billed_by: ReceiptParty? = null,
    val service: ReceiptService? = null,
    val lines: List<com.cranezo.ui.finance.model.BillLine>? = null,
    val payments: List<ReceiptPayment>? = null,
    val payment_id: String? = null,
    val paid_at: String? = null,
    val booking_amount: Double? = null,
    val extra_time_amount: Double? = null,
    val total_amount: Double? = null,
    val advance_paid: Double? = null,
    val final_payment: Double? = null,
    val total_paid: Double? = null,
    val method_name: String? = null
)

data class RefundInfo(val status: String? = null, val status_label: String? = null, val amount: Double? = null)

data class CancellationInfo(
    val cancelled_by: String? = null,
    val cancelled_by_label: String? = null,
    val cancelled_at: String? = null,
    val reason: String? = null,
    val refund: RefundInfo? = null
)

data class RebookDuration(val type: String? = null, val hours: Int? = null, val days: Int? = null)

data class RebookInfo(
    val crane_id: String? = null,
    val crane_type: Int? = null,
    val capacity: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val site_name: String? = null,
    val site_address: String? = null,
    val duration: RebookDuration? = null
)

data class RatingInfo(val rated: Boolean? = null, val rating: Int? = null, val comment: String? = null)

data class BookingActions(
    val can_cancel: Boolean? = null,
    val can_modify: Boolean? = null,
    val can_track: Boolean? = null,
    val can_confirm_arrival: Boolean? = null,
    val can_request_extra_time: Boolean? = null,
    val can_pay: Boolean? = null,
    val can_rate: Boolean? = null,
    val can_view_receipt: Boolean? = null
)

// ---- My Jobs, Track My Cranes ----

data class JobCounts(val upcoming: Int? = null, val active: Int? = null, val completed: Int? = null, val cancelled: Int? = null)

data class MyJobCrane(
    val id: String? = null,
    val crane_code: String? = null,
    val title: String? = null,
    val capacity_label: String? = null,
    val primary_photo: String? = null
)

data class MyJobItem(
    val booking_id: String? = null,
    val booking_code: String? = null,
    val status: Int? = null,
    val status_label: String? = null,
    val tab: String? = null,
    val tab_label: String? = null,
    val stage: String? = null,
    val crane: MyJobCrane? = null,
    val location: String? = null,
    val start_at: String? = null,
    val end_at: String? = null,
    val amount: Double? = null,
    val payment_status: String? = null,
    val can_rate: Boolean? = null
)

data class ActiveOperator(val full_name: String? = null, val rating: Double? = null, val reviews_count: Int? = null)

data class ActiveTrack(
    val booking_id: String? = null,
    val booking_code: String? = null,
    val crane: MyJobCrane? = null,
    val operator: ActiveOperator? = null,
    val stage: String? = null,
    val tag: String? = null,
    val distance_km: Double? = null,
    val eta_minutes: Int? = null,
    val crane_location: CraneLocation? = null
)

// ---- POST /places/search and /places/reverse (address search for the location step) ----

data class PlaceHit(val label: String? = null, val latitude: Double? = null, val longitude: Double? = null)

data class PlaceLabel(val label: String? = null)
