package com.cranezo.ui.operator.model

import com.cranezo.ui.booking.model.BookingDuration
import com.cranezo.ui.booking.model.BookingPayment
import com.cranezo.ui.booking.model.BookingSchedule

/** Response models of the Operator Jobs / Earnings APIs. All fields nullable. */

// ---- Home ----

data class OperatorHome(
    val greeting_name: String? = null,
    val profile_image: String? = null,
    val online: Boolean? = null,
    // documents approved by the admin; until then no job actions
    val verified: Boolean? = null,
    val online_label: String? = null,
    val online_message: String? = null,
    val assigned_crane: HomeCrane? = null,
    val today_jobs: TodayJobs? = null,
    val today_earnings: Double? = null,
    val document_status: DocumentStatus? = null,
    val active_job: ActiveJob? = null,
    val jobs_waiting_for_response: Int? = null,
    val unread_notifications: Int? = null
)

data class HomeCrane(
    val id: String? = null,
    val crane_code: String? = null,
    val title: String? = null,
    val model_name: String? = null,
    val registration_number: String? = null,
    val crane_type_name: String? = null,
    val capacity_name: String? = null,
    val primary_photo: String? = null,
    val availability_label: String? = null,
    val owner_name: String? = null
)

data class TodayJobs(val count: Int? = null, val items: List<JobListItem>? = null)

data class DocumentStatus(
    val all_verified: Boolean? = null,
    val uploaded: Int? = null,
    val verified: Int? = null,
    val pending: Int? = null,
    val rejected: Int? = null,
    val label: String? = null
)

data class ActiveJob(val booking_id: String? = null, val booking_code: String? = null, val status: Int? = null)

data class OnlineStatus(val online: Boolean? = null, val online_label: String? = null)

data class LocationAck(val updated: Boolean? = null, val distance_km: Double? = null, val eta_minutes: Int? = null)

// ---- Jobs list ----

data class JobListItem(
    val booking_id: String? = null,
    val booking_code: String? = null,
    val status: Int? = null,
    val tab_label: String? = null,
    val stage: String? = null,
    val stage_label: String? = null,
    val customer_name: String? = null,
    val location: String? = null,
    val start_at: String? = null,
    val crane_label: String? = null,
    val crane_code: String? = null,
    val duration_label: String? = null,
    val estimated_earnings: Double? = null,
    val operator_status: Int? = null
)

// ---- The job object ----

data class JobCrane(
    val id: String? = null,
    val crane_code: String? = null,
    val title: String? = null,
    val model_name: String? = null,
    val registration_number: String? = null,
    val crane_type_name: String? = null,
    val capacity_name: String? = null,
    val fuel_type_name: String? = null,
    val primary_photo: String? = null
)

data class JobCustomer(
    val id: String? = null,
    val name: String? = null,
    val full_name: String? = null,
    val member_id: String? = null,
    val mobile_number: String? = null,
    val country_code: String? = null
)

data class JobSite(
    val name: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val distance_km: Double? = null,
    val eta_minutes: Int? = null
)

data class ProgressStep(val key: String? = null, val label: String? = null, val at: String? = null, val state: String? = null)

data class JobTimers(
    val server_now: String? = null,
    val work_started_at: String? = null,
    val work_ended_at: String? = null,
    val working_seconds: Long? = null,
    val waiting_seconds: Long? = null,
    val is_waiting: Boolean? = null,
    val waiting_since: String? = null
)

data class JobArrival(
    val arrived_at: String? = null,
    val confirmed_at: String? = null,
    val confirmed_by: String? = null,
    val waiting_for_customer: Boolean? = null
)

data class SitePhoto(
    val id: Int? = null,
    val photo_url: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val captured_at: String? = null
)

data class ChecklistItem(val key: String? = null, val title: String? = null, val description: String? = null, val done: Boolean? = null)

data class Checklist(
    val items: List<ChecklistItem>? = null,
    val completed_count: Int? = null,
    val total: Int? = null,
    val completed: Boolean? = null
)

data class StartCheck(val key: String? = null, val label: String? = null, val description: String? = null, val ok: Boolean? = null)

data class JobSummary(
    val start_time: String? = null,
    val end_time: String? = null,
    val total_seconds: Long? = null,
    val working_seconds: Long? = null,
    val waiting_seconds: Long? = null,
    val extra_seconds: Long? = null,
    val base_hours: Double? = null,
    val base_amount: Double? = null,
    val waiting_charge: Double? = null,
    val extra_amount: Double? = null,
    val final_amount: Double? = null
)

data class JobCompletion(
    val requested_at: String? = null,
    val otp_expires_at: String? = null,
    val attempts_left: Int? = null,
    val completed_at: String? = null
)

data class JobSettlement(val final_amount: Double? = null, val status: String? = null, val status_label: String? = null)

data class OperatorJob(
    val booking_id: String? = null,
    val booking_code: String? = null,
    val status: Int? = null,
    val status_label: String? = null,
    val tab_label: String? = null,
    val stage: String? = null,
    val stage_label: String? = null,
    val next_action: String? = null,
    val operator_status: Int? = null,
    val requested_crane: JobCrane? = null,
    val assigned_crane: JobCrane? = null,
    val customer: JobCustomer? = null,
    // booking for someone else: call this person at the site
    val site_contact: com.cranezo.ui.ownerjobs.model.SiteContact? = null,
    val job_site: JobSite? = null,
    val schedule: BookingSchedule? = null,
    val duration: BookingDuration? = null,
    val estimated_earnings: Double? = null,
    val special_requirements: String? = null,
    val payment: BookingPayment? = null,
    val progress: List<ProgressStep>? = null,
    val timers: JobTimers? = null,
    val arrival: JobArrival? = null,
    val site_photos: List<SitePhoto>? = null,
    val checklist: Checklist? = null,
    val start_checks: List<StartCheck>? = null,
    val can_start: Boolean? = null,
    val final_photos: List<SitePhoto>? = null,
    val job_notes: String? = null,
    val summary: JobSummary? = null,
    val completion: JobCompletion? = null,
    val settlement: JobSettlement? = null,
    // Extra time the customer asked for (answered by the operator)
    val extension: com.cranezo.ui.booking.model.ExtensionInfo? = null
)

object NextAction {
    const val ACCEPT_JOB = "accept_job"
    const val WAIT_OWNER = "waiting_for_owner_to_assign"
    const val START_JOURNEY = "start_journey"
    const val MARK_ARRIVED = "mark_arrived"
    const val SITE_PHOTOS = "take_site_photos"
    const val CHECKLIST = "complete_checklist"
    const val WAIT_CUSTOMER = "wait_for_customer_confirmation"
    const val START_JOB = "start_job"
    const val MONITOR = "monitor_job"
    const val REQUEST_CONFIRMATION = "request_customer_confirmation"
    const val ENTER_OTP = "enter_customer_otp"
    const val DONE = "done"
}

// ---- Issues, OTP ----

data class IssueResult(val issue_id: String? = null, val kind: String? = null, val status: String? = null, val message: String? = null)

data class OtpSent(val otp_sent: Boolean? = null, val expires_at: String? = null, val message: String? = null)

// ---- Earnings ----

data class EarningsSummary(
    val today_jobs: Int? = null,
    val today_earnings: Double? = null,
    val pending_settlement: Double? = null,
    val completed_settlement: Double? = null,
    val completed_jobs: Int? = null
)

data class EarningItem(
    val booking_id: String? = null,
    val booking_code: String? = null,
    val customer_name: String? = null,
    val location: String? = null,
    val completed_at: String? = null,
    val start_at: String? = null,
    val crane_label: String? = null,
    val amount: Double? = null,
    val settlement_status: String? = null,
    val settlement_label: String? = null
)
