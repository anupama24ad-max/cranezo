package com.cranezo.ui.ownerjobs.model

import com.cranezo.ui.booking.model.OwnerRequest
import com.cranezo.ui.booking.model.TrackInfo
import com.cranezo.ui.cranes.model.Operator

// ---------------------------------------------------------------------------------------------
// Jobs tab
// ---------------------------------------------------------------------------------------------

data class OwnerJobsState(
    val counts: OwnerJobCounts? = null,
    // "new_requests", "upcoming", "active", "completed" or "closed"
    val tab: String = "new_requests",
    val newestFirst: Boolean = true,
    val jobs: List<OwnerJob> = emptyList(),
    val total: Int = 0,
    val loaded: Boolean = false,
    val hasMore: Boolean = false,
    val nextMin: Int = 0,
    // After Accept: the "Booking Accepted" sheet
    val accepted: OwnerRequest? = null
)

sealed interface OwnerJobsIntent {
    data object Refresh : OwnerJobsIntent
    data class SelectTab(val tab: String) : OwnerJobsIntent
    data object ToggleSort : OwnerJobsIntent
    data object LoadMore : OwnerJobsIntent
    data class Accept(val bookingId: String) : OwnerJobsIntent
    data class Decline(val bookingId: String) : OwnerJobsIntent
    data object DismissAccepted : OwnerJobsIntent
}

// ---------------------------------------------------------------------------------------------
// One job: details, live location, evidence, assigning an operator
// ---------------------------------------------------------------------------------------------

data class OwnerJobDetailState(
    val job: OwnerJob? = null,
    val loaded: Boolean = false,
    val track: TrackInfo? = null,
    val evidence: JobEvidence? = null,
    val evidenceLoaded: Boolean = false,
    val operators: List<Operator> = emptyList(),
    val operatorsLoaded: Boolean = false
)

sealed interface OwnerJobDetailIntent {
    data class Load(val bookingId: String) : OwnerJobDetailIntent
    // Silent re-sync, every 30 seconds while an active job is open
    data object Poll : OwnerJobDetailIntent
    data object PollTrack : OwnerJobDetailIntent
    data object LoadEvidence : OwnerJobDetailIntent
    data object LoadOperators : OwnerJobDetailIntent
    data class AssignOperator(val operatorId: String, val byCode: Boolean = false) : OwnerJobDetailIntent
}

// ---------------------------------------------------------------------------------------------
// Earnings tab
// ---------------------------------------------------------------------------------------------

data class OwnerEarningsState(
    // "today", "week", "month" or "all"
    val period: String = "today",
    val summary: OwnerEarningsSummary? = null,
    // "week", "month" or "year"
    val range: String = "month",
    val trend: EarningsTrend? = null,
    val jobs: List<OwnerEarningJob> = emptyList(),
    val jobsHasMore: Boolean = false,
    val jobsNextMin: Int = 0,
    val loaded: Boolean = false
)

sealed interface OwnerEarningsIntent {
    data object Load : OwnerEarningsIntent
    data class SelectPeriod(val period: String) : OwnerEarningsIntent
    data class SelectRange(val range: String) : OwnerEarningsIntent
    data object LoadMoreJobs : OwnerEarningsIntent
}
