package com.cranezo.ui.booking.model

data class BookingUiState(
    val options: BookingOptions? = null,

    // What the customer chose in the 5 steps
    val craneType: Int? = null,
    val capacity: Int? = null,
    val siteName: String = "",
    val siteAddress: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isLocating: Boolean = false,
    // "now" or "scheduled"; date is yyyy-MM-dd, time HH:mm
    val startType: String = "now",
    val date: String = "",
    val time: String = "",
    // "hours", "full_day" or "days"
    val durationType: String = "hours",
    val hours: Int = 8,
    val days: Int = 1,
    val paymentMethod: String = "COD",

    // Search
    val results: List<AvailableCrane> = emptyList(),
    val resultsMessage: String? = null,
    val searched: Boolean = false,
    val hasMore: Boolean = false,
    val nextMin: Int = 0,
    val isLoadingMore: Boolean = false,
    // Cheapest crane of a search, for "Estimated Price" on the duration step
    val estimate: AvailableCrane? = null,

    val detail: AvailableCrane? = null,
    val booking: Booking? = null,
    val track: TrackInfo? = null,

    // My bookings
    val bookings: List<Booking> = emptyList(),
    val bookingsLoaded: Boolean = false,
    val bookingsHasMore: Boolean = false,
    val bookingsNextMin: Int = 0,

    // My Jobs
    val counts: JobCounts? = null,
    val jobs: List<MyJobItem> = emptyList(),
    val jobsTab: String = "upcoming",
    val jobsLoaded: Boolean = false,
    val jobsHasMore: Boolean = false,
    val jobsNextMin: Int = 0,

    // Track My Cranes
    val activeTracks: List<ActiveTrack> = emptyList(),
    val activeLoaded: Boolean = false,

    // "Cancellation and Refunds" text for the review screen
    val policy: String? = null
)

sealed interface BookingIntent {
    data object LoadOptions : BookingIntent
    data class SelectCraneType(val id: Int) : BookingIntent
    data class SelectCapacity(val id: Int) : BookingIntent

    data object LocateMe : BookingIntent
    data class SearchAddress(val query: String) : BookingIntent
    data class AddressChanged(val value: String) : BookingIntent
    data class SiteNameChanged(val value: String) : BookingIntent

    data class StartTypeChanged(val value: String) : BookingIntent
    data class DateChanged(val value: String) : BookingIntent
    data class TimeChanged(val value: String) : BookingIntent

    data class DurationTypeChanged(val value: String) : BookingIntent
    data class HoursChanged(val value: Int) : BookingIntent
    data class DaysChanged(val value: Int) : BookingIntent
    data object EstimatePrice : BookingIntent

    data object Search : BookingIntent
    data object LoadMore : BookingIntent
    data class OpenCrane(val craneId: String) : BookingIntent
    data object CreateBooking : BookingIntent

    // Silent refreshes while a screen is open
    data class PollBooking(val bookingId: String) : BookingIntent
    data class LoadTrack(val bookingId: String) : BookingIntent
    data object CancelBooking : BookingIntent

    data class OpenBooking(val bookingId: String) : BookingIntent
    data class LoadBookings(val group: String) : BookingIntent
    data object LoadMoreBookings : BookingIntent
    // Back to a clean flow
    data object LoadPolicy : BookingIntent

    // After the booking is confirmed
    data object ConfirmArrival : BookingIntent
    data class RequestExtra(val minutes: Int, val message: String) : BookingIntent
    data object CancelExtra : BookingIntent
    data class Pay(val method: String) : BookingIntent
    data class Rate(val stars: Int, val comment: String) : BookingIntent
    data class LoadJobs(val tab: String) : BookingIntent
    data object LoadMoreJobs : BookingIntent
    data object LoadActiveTracking : BookingIntent
    data object BookAgain : BookingIntent
    data object Reset : BookingIntent
}

sealed interface BookingEffect {
    data class Go(val route: String, val popUpTo: String? = null) : BookingEffect
    data object Close : BookingEffect
    // The crane was booked by someone else: the list behind has to be refreshed
    data object BackToResults : BookingEffect
}
