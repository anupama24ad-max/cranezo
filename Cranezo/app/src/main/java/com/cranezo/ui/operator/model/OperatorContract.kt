package com.cranezo.ui.operator.model

import com.cranezo.ui.components.SelectOption

// ---------------------------------------------------------------------------------------------
// Operator tabs: Home, Jobs, Earnings
// ---------------------------------------------------------------------------------------------

data class OperatorTabsState(
    val home: OperatorHome? = null,
    val homeLoaded: Boolean = false,

    // "all", "assigned", "in_progress" or "completed"
    val jobsTab: String = "all",
    val jobs: List<JobListItem> = emptyList(),
    val jobsLoaded: Boolean = false,
    val jobsHasMore: Boolean = false,
    val jobsNextMin: Int = 0,

    val earnings: EarningsSummary? = null,
    val earningItems: List<EarningItem> = emptyList(),
    val earningsTotal: Int = 0,
    val earningsLoaded: Boolean = false,
    val earningsHasMore: Boolean = false,
    val earningsNextMin: Int = 0,

    // A job was just assigned: its booking id, shown as "New Job Request"
    val incomingJob: String? = null
)

sealed interface OperatorTabsIntent {
    data object LoadHome : OperatorTabsIntent
    data object ToggleOnline : OperatorTabsIntent
    data class LoadJobs(val tab: String) : OperatorTabsIntent
    data object LoadMoreJobs : OperatorTabsIntent
    data object LoadEarnings : OperatorTabsIntent
    data object LoadMoreEarnings : OperatorTabsIntent
    data object DismissIncoming : OperatorTabsIntent
}

// ---------------------------------------------------------------------------------------------
// One job, from the request to the completed screen
// ---------------------------------------------------------------------------------------------

data class OperatorJobState(
    val job: OperatorJob? = null,
    val loaded: Boolean = false,

    // Photos already uploaded and tagged, not saved yet (saving replaces the whole set)
    val sitePhotos: List<SitePhoto> = emptyList(),
    val finalPhotos: List<SitePhoto> = emptyList(),
    val isUploading: Boolean = false,

    val checklist: Checklist? = null,
    val breakdownTypes: List<SelectOption> = emptyList(),

    // The operator's own position while on the way (shown on the map)
    val myLatitude: Double? = null,
    val myLongitude: Double? = null,
    // the road route from the operator to the site (yellow line)
    val route: List<com.google.android.gms.maps.model.LatLng> = emptyList(),
    // km and minutes left along that road, at crane speed (from the server)
    val liveDistanceKm: Double? = null,
    val liveEtaMinutes: Int? = null,

    // Set while the OTP was sent and the customer has to read it out
    val otpSent: Boolean = false
)

sealed interface OperatorJobIntent {
    data class Load(val bookingId: String) : OperatorJobIntent
    data object Poll : OperatorJobIntent

    data object Accept : OperatorJobIntent
    data class Decline(val reason: String) : OperatorJobIntent

    data object StartJourney : OperatorJobIntent
    data object Arrived : OperatorJobIntent
    data class LoadRoute(val fromLat: Double, val fromLng: Double, val toLat: Double, val toLng: Double) : OperatorJobIntent

    data class AddSitePhoto(val uri: String) : OperatorJobIntent
    data object RemoveSitePhoto : OperatorJobIntent
    data object SaveSitePhotos : OperatorJobIntent

    data object LoadChecklist : OperatorJobIntent
    data class TickChecklist(val key: String, val done: Boolean) : OperatorJobIntent

    data object ConfirmArrival : OperatorJobIntent
    data object StartJob : OperatorJobIntent
    data class ToggleWaiting(val start: Boolean) : OperatorJobIntent

    data class AddFinalPhoto(val uri: String) : OperatorJobIntent
    data class RemoveFinalPhoto(val index: Int) : OperatorJobIntent
    data class Finish(val notes: String) : OperatorJobIntent

    data object RequestConfirmation : OperatorJobIntent
    data class ConfirmCompletion(val otp: String) : OperatorJobIntent

    data object LoadBreakdownTypes : OperatorJobIntent
    data class ReportBreakdown(val type: Int?, val description: String, val photoUri: String?) : OperatorJobIntent
    data class ReportIssue(val description: String, val photoUri: String?) : OperatorJobIntent
    data object ReportEmergency : OperatorJobIntent
    data class RespondExtension(val approve: Boolean, val reason: String) : OperatorJobIntent
}

sealed interface OperatorJobEffect {
    // Leave the job flow (declined, cancelled, finished)
    data object Close : OperatorJobEffect
    // A report was sent: back to monitoring
    data object ReportSent : OperatorJobEffect
}
