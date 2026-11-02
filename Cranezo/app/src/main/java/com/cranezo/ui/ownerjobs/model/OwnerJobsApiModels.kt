package com.cranezo.ui.ownerjobs.model

import com.cranezo.ui.booking.model.BookingDuration
import com.cranezo.ui.booking.model.BookingOperator
import com.cranezo.ui.booking.model.BookingPayment
import com.cranezo.ui.booking.model.BookingSchedule
import com.cranezo.ui.operator.model.JobCrane
import com.cranezo.ui.operator.model.JobCustomer
import com.cranezo.ui.operator.model.JobTimers
import com.cranezo.ui.operator.model.ProgressStep
import com.cranezo.ui.operator.model.SitePhoto

/** Response models of the Owner Jobs / Owner Earnings APIs. All fields nullable. */

data class OwnerJobCounts(
    val new_requests: Int? = null,
    val upcoming: Int? = null,
    val active: Int? = null,
    val completed: Int? = null,
    val closed: Int? = null
)

data class OwnerLocation(
    val name: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val distance_km: Double? = null
)

data class AssignedOperator(
    val full_name: String? = null,
    val mobile_number: String? = null,
    val country_code: String? = null,
    val rating: Double? = null,
    val role_label: String? = null,
    val tag: String? = null
)

data class TimelineStep(val key: String? = null, val label: String? = null, val at: String? = null, val state: String? = null)

data class Monitoring(
    val start_time: String? = null,
    val working_seconds: Long? = null,
    val extra_seconds: Long? = null,
    val message: String? = null
)

data class OwnerEarningsSummaryBlock(
    val base_amount: Double? = null,
    val extra_time_amount: Double? = null,
    val final_amount: Double? = null,
    val platform_fee_percent: Double? = null,
    val platform_fee: Double? = null,
    val other_deductions: Double? = null,
    val net_amount: Double? = null
)

data class OwnerSettlement(val label: String? = null, val settled_at: String? = null)

data class OwnerCompletion(
    val scheduled_at: String? = null,
    val actual_duration_seconds: Long? = null,
    val extra_seconds: Long? = null,
    val earnings_summary: OwnerEarningsSummaryBlock? = null,
    val settlement: OwnerSettlement? = null
)

data class EvidenceCounts(
    val site_photos: Int? = null,
    val final_photos: Int? = null,
    val issues: Int? = null,
    val has_notes: Boolean? = null
)

/** One job: the list item and, from jobs/get, the details. */
data class OwnerJob(
    val booking_id: String? = null,
    val booking_code: String? = null,
    val status: Int? = null,
    val status_label: String? = null,
    val tab: String? = null,
    val tab_label: String? = null,
    val stage: String? = null,
    val stage_label: String? = null,
    val crane: JobCrane? = null,
    val location: OwnerLocation? = null,
    val schedule: BookingSchedule? = null,
    val duration_label: String? = null,
    val approx_earnings: Double? = null,
    val requested_ago_minutes: Int? = null,
    val seconds_left: Int? = null,
    val starts_in_minutes: Int? = null,
    val started_at: String? = null,
    val started_ago_minutes: Int? = null,
    val can_respond: Boolean? = null,
    val operator: BookingOperator? = null,
    val operator_label: String? = null,
    val progress: List<ProgressStep>? = null,
    val timers: JobTimers? = null,
    val completed_at: String? = null,
    val actual_duration_seconds: Long? = null,
    val final_amount: Double? = null,
    // details
    val accepted_at: String? = null,
    val job_location: OwnerLocation? = null,
    val duration: BookingDuration? = null,
    val assigned_operator: AssignedOperator? = null,
    val customer: JobCustomer? = null,
    val special_requirements: String? = null,
    val payment: BookingPayment? = null,
    val timeline: List<TimelineStep>? = null,
    val monitoring: Monitoring? = null,
    val completion: OwnerCompletion? = null,
    val evidence: EvidenceCounts? = null,
    val can_view_live_location: Boolean? = null,
    val can_view_evidence: Boolean? = null,
    val can_download_invoice: Boolean? = null
)

data class EvidenceArrival(val arrived_at: String? = null, val confirmed_at: String? = null, val confirmed_by: String? = null)

data class EvidenceIssue(
    val id: String? = null,
    val kind: String? = null,
    val breakdown_type_name: String? = null,
    val description: String? = null,
    val photos: List<String>? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val status: String? = null,
    val reported_at: String? = null
)

data class JobEvidence(
    val booking_id: String? = null,
    val booking_code: String? = null,
    val site_photos: List<SitePhoto>? = null,
    val final_photos: List<SitePhoto>? = null,
    val job_notes: String? = null,
    val arrival: EvidenceArrival? = null,
    val checklist_completed_at: String? = null,
    val issues: List<EvidenceIssue>? = null
)

// ---- Earnings ----

data class EarningsPeriod(val key: String? = null, val from: String? = null, val to: String? = null)

data class EarningsBlock(
    val gross_earnings: Double? = null,
    val platform_fee_percent: Double? = null,
    val platform_fee: Double? = null,
    val other_deductions: Double? = null,
    val net_earnings: Double? = null
)

data class ComparedToPrevious(val label: String? = null, val previous_net: Double? = null, val change_percent: Double? = null)

data class PeriodCard(val net: Double? = null, val jobs: Int? = null)

data class SettleInfo(
    val amount: Double? = null,
    val jobs: Int? = null,
    val last_settled_at: String? = null,
    val expected_by: String? = null,
    val expected_in_days: Int? = null
)

data class SettlementBlock(val settled: SettleInfo? = null, val pending: SettleInfo? = null)

data class OwnerEarningsSummary(
    val period: EarningsPeriod? = null,
    val earnings: EarningsBlock? = null,
    val jobs_completed: Int? = null,
    val compared_to_previous: ComparedToPrevious? = null,
    val cards: Map<String, PeriodCard>? = null,
    val settlement: SettlementBlock? = null
)

data class TrendPoint(
    val key: String? = null,
    val label: String? = null,
    val net: Double? = null,
    val gross: Double? = null,
    val jobs: Int? = null
)

data class EarningsTrend(
    val range: String? = null,
    val points: List<TrendPoint>? = null,
    val peak: TrendPoint? = null,
    val total_net: Double? = null
)

data class EarningJobCrane(val crane_code: String? = null, val title: String? = null, val primary_photo: String? = null)

data class OwnerEarningJob(
    val booking_id: String? = null,
    val booking_code: String? = null,
    val crane: EarningJobCrane? = null,
    val location: String? = null,
    val completed_at: String? = null,
    val amount: Double? = null,
    val net_amount: Double? = null,
    val settlement_status: String? = null,
    val settlement_label: String? = null
)
