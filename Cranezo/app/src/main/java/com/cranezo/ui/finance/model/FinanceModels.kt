package com.cranezo.ui.finance.model

import com.google.gson.annotations.SerializedName

/**
 * Payments, commission, settlements, operator job requests and complaints.
 * Field names match the API (snake_case); everything is nullable so a missing field never crashes.
 */

data class IdName(val id: String? = null, val name: String? = null)

data class PersonRef(val id: String? = null, val name: String? = null, val member_id: String? = null)

// ---------------------------------------------------------------- bill and commission

data class BillLine(val key: String? = null, val label: String? = null, val amount: Double? = null, val total: Boolean? = null)

data class BillBlock(
    val lines: List<BillLine>? = null,
    val total: Double? = null,
    val platform_fee_percent: Double? = null,
    val platform_fee: Double? = null,
    val owner_receives: Double? = null,
    val gst_amount: Double? = null,
    val owner_share: Double? = null,
    @SerializedName("final") val isFinal: Boolean? = null
)

data class Commission(
    val type: String? = null,
    val type_label: String? = null,
    val value: Double? = null,
    val distance_km: Double? = null,
    val amount: Double? = null,
    @SerializedName("set") val isSet: Boolean? = null,
    val can_edit: Boolean? = null,
    val settlement_status: String? = null,
    val settled_at: String? = null,
    val types: List<IdName>? = null
)

data class AssignedOperatorRef(val id: String? = null, val full_name: String? = null)

data class JobLocationRef(val distance_km: Double? = null)

/** The parts of owner/jobs/get the commission screen needs */
data class OwnerJobMoney(
    val booking_id: String? = null,
    val booking_code: String? = null,
    val job_location: JobLocationRef? = null,
    val bill: BillBlock? = null,
    val operator_commission: Commission? = null,
    val assigned_operator: AssignedOperatorRef? = null
)

// ---------------------------------------------------------------- owner billing settings

data class Charge(val name: String? = null, val type: String? = null, val value: Double? = null)

data class BillingSettings(
    val gst_percent: Double? = null,
    val gst_number: String? = null,
    val charges: List<Charge>? = null,
    val default_commission_type: String? = null,
    val default_commission_value: Double? = null,
    val advance_percent: Int? = null,
    val per_km_rate: Double? = null,
    val platform_fee_percent: Double? = null,
    val charge_types: List<IdName>? = null,
    val commission_types: List<IdName>? = null
)

// ---------------------------------------------------------------- settlements and payments

data class OperatorDue(
    val operator: PersonRef? = null,
    val jobs: Int? = null,
    val amount_due: Double? = null,
    val jobs_without_commission: Int? = null
)

data class OperatorDues(val total_due: Double? = null, val items: List<OperatorDue>? = null, val payout_methods: List<IdName>? = null)

data class JobLine(
    val booking_id: String? = null,
    val booking_code: String? = null,
    val completed_at: String? = null,
    val customer_total: Double? = null,
    val platform_fee: Double? = null,
    val owner_receives: Double? = null,
    val gst_amount: Double? = null,
    val operator_commission: Double? = null,
    val commission_set: Boolean? = null,
    val owner_share: Double? = null,
    val payment_status: String? = null,
    val collected_by: String? = null,
    val platform_balance: Double? = null,
    val settlement_status: String? = null,
    val operator_settlement_status: String? = null
)

data class PendingJobs(
    val items: List<JobLine>? = null,
    val total_due: Double? = null,
    val net: Double? = null,
    val net_label: String? = null,
    val can_settle_count: Int? = null,
    val unpaid_count: Int? = null
)

data class SettlementItem(
    val id: String? = null,
    val settlement_code: String? = null,
    val kind: String? = null,
    val direction: String? = null,
    val direction_label: String? = null,
    val amount: Double? = null,
    val jobs_count: Int? = null,
    val method_label: String? = null,
    val reference: String? = null,
    val note: String? = null,
    val settled_at: String? = null,
    val owner: PersonRef? = null,
    val operator: PersonRef? = null
)

data class TransactionItem(
    val id: String? = null,
    val transaction_code: String? = null,
    val booking_code: String? = null,
    val amount: Double? = null,
    val method_name: String? = null,
    val status: String? = null,
    val status_label: String? = null,
    val collected_by_label: String? = null,
    val created_at: String? = null
)

/** Operator earnings summary (operator/earnings/summary) */
data class OperatorEarnings(
    val today_earnings: Double? = null,
    val pending_settlement: Double? = null,
    val completed_settlement: Double? = null,
    val total_earned: Double? = null,
    val completed_jobs: Int? = null,
    val jobs_without_commission: Int? = null
)

// ---------------------------------------------------------------- job requests

data class RequestJob(
    val booking_id: String? = null,
    val booking_code: String? = null,
    val location: String? = null,
    val start_at: String? = null,
    val duration_label: String? = null
)

data class RequestOperator(
    val id: String? = null,
    val full_name: String? = null,
    val member_id: String? = null,
    val mobile_number: String? = null,
    val country_code: String? = null,
    val rating: Double? = null,
    val reviews_count: Int? = null,
    val experience: String? = null,
    val license_verified: Boolean? = null,
    val independent: Boolean? = null
)

data class JobRequestItem(
    val id: String? = null,
    val booking_id: String? = null,
    val booking_code: String? = null,
    val status: Int? = null,
    val status_label: String? = null,
    val message: String? = null,
    val reject_reason: String? = null,
    val requested_at: String? = null,
    val location: String? = null,
    val start_at: String? = null,
    val job: RequestJob? = null,
    val operator: RequestOperator? = null
)

data class JobSite(val name: String? = null, val address: String? = null)

data class AvailableJob(
    val booking_id: String? = null,
    val booking_code: String? = null,
    val crane_label: String? = null,
    val owner_name: String? = null,
    val site: JobSite? = null,
    val distance_km: Double? = null,
    val start_at: String? = null,
    val end_at: String? = null,
    val duration_label: String? = null,
    val expected_earnings: Double? = null,
    val expected_earnings_label: String? = null,
    val my_request: JobRequestItem? = null,
    val can_request: Boolean? = null
)

// ---------------------------------------------------------------- complaints

data class ComplaintCategory(val id: String? = null, val name: String? = null, val group: String? = null, val description: String? = null)

data class ComplaintOptions(val categories: List<ComplaintCategory>? = null)

data class ComplaintBooking(
    val booking_id: String? = null,
    val booking_code: String? = null,
    val location: String? = null,
    val amount: Double? = null,
    val payment_status: String? = null
)

data class ComplaintUpdateItem(
    val id: Int? = null,
    val author_type: String? = null,
    val author_name: String? = null,
    val message: String? = null,
    val status_to_label: String? = null,
    val created_at: String? = null
)

data class ComplaintItem(
    val id: String? = null,
    val complaint_code: String? = null,
    val category: String? = null,
    val category_name: String? = null,
    val subject: String? = null,
    val description: String? = null,
    val status: String? = null,
    val status_label: String? = null,
    val resolution_note: String? = null,
    val booking: ComplaintBooking? = null,
    val customer: PersonRef? = null,
    val owner: PersonRef? = null,
    val updates: List<ComplaintUpdateItem>? = null,
    val can_reply: Boolean? = null,
    val created_at: String? = null
)

/** A customer booking to attach a complaint to (customer/booking/list) */
data class BookingPick(
    val booking_id: String? = null,
    val booking_code: String? = null,
    val location: String? = null,
    val amount: Double? = null,
    val start_at: String? = null
)

/** Owner earnings summary: only the settlement blocks are read here */
data class PlatformBalance(
    val you_owe_platform: Double? = null,
    val platform_owes_you: Double? = null,
    val balance: Double? = null,
    val balance_label: String? = null,
    val cash_jobs: Int? = null,
    val online_jobs: Int? = null,
    val unpaid_jobs: Int? = null
)

data class OwnerMoneySummary(val platform_balance: PlatformBalance? = null)
