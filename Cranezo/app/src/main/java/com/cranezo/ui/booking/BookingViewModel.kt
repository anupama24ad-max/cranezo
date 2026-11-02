package com.cranezo.ui.booking

import android.app.Application
import android.location.Geocoder
import androidx.lifecycle.viewModelScope
import com.cranezo.R
import com.cranezo.api.isRequestCallSuspendSuccess
import com.cranezo.core.BaseViewModel
import com.cranezo.repository.Repository
import com.cranezo.ui.booking.model.AvailableCrane
import com.cranezo.ui.booking.model.BookingEffect
import com.cranezo.ui.booking.model.BookingIntent
import com.cranezo.ui.booking.model.BookingUiState
import com.cranezo.utils.AppMethods
import com.cranezo.utils.SessionManager
import com.cranezo.utils.fetchCurrentLocation
import com.cranezo.utils.toIsoWithOffset
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Locale
import javax.inject.Inject

/**
 * Customer side of Book a Crane. Everything about price, distance, ETA and availability comes from
 * the server; nothing is calculated here.
 */
@HiltViewModel
class BookingViewModel @Inject constructor(
    var app: Application,
    var repo: Repository,
    var sm: SessionManager
) : BaseViewModel(app) {

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    private val _effect = Channel<BookingEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private companion object {
        const val PAGE = 10
    }

    private var bookingsGroup = "active"

    private fun headers() = AppMethods.getToken(sm)

    private fun fail(message: String?) {
        showError(message?.takeIf { it.isNotBlank() } ?: string(R.string.crane_msg_generic_error), autoHideMs = 2500L)
    }

    fun onIntent(intent: BookingIntent) {
        when (intent) {
            BookingIntent.LoadOptions -> loadOptions()
            is BookingIntent.SelectCraneType -> _uiState.update { it.copy(craneType = intent.id) }
            is BookingIntent.SelectCapacity -> _uiState.update { it.copy(capacity = intent.id) }

            BookingIntent.LocateMe -> locateMe()
            is BookingIntent.SearchAddress -> searchAddress(intent.query)
            is BookingIntent.AddressChanged -> _uiState.update { it.copy(siteAddress = intent.value) }
            is BookingIntent.SiteNameChanged -> _uiState.update { it.copy(siteName = intent.value) }

            is BookingIntent.StartTypeChanged -> _uiState.update { it.copy(startType = intent.value) }
            is BookingIntent.DateChanged -> _uiState.update { it.copy(date = intent.value) }
            is BookingIntent.TimeChanged -> _uiState.update { it.copy(time = intent.value) }

            is BookingIntent.DurationTypeChanged -> _uiState.update { it.copy(durationType = intent.value) }
            is BookingIntent.HoursChanged -> _uiState.update { it.copy(hours = intent.value) }
            is BookingIntent.DaysChanged -> _uiState.update { it.copy(days = intent.value) }
            BookingIntent.EstimatePrice -> estimatePrice()

            BookingIntent.Search -> search(reset = true)
            BookingIntent.LoadMore -> search(reset = false)
            is BookingIntent.OpenCrane -> openCrane(intent.craneId)
            BookingIntent.CreateBooking -> createBooking()

            is BookingIntent.PollBooking -> pollBooking(intent.bookingId)
            is BookingIntent.LoadTrack -> loadTrack(intent.bookingId)
            BookingIntent.CancelBooking -> cancelBooking()

            is BookingIntent.OpenBooking -> openBooking(intent.bookingId)
            is BookingIntent.LoadBookings -> loadBookings(intent.group, reset = true)
            BookingIntent.LoadMoreBookings -> loadBookings(bookingsGroup, reset = false)
            BookingIntent.LoadPolicy -> loadPolicy()
            BookingIntent.Reset -> _uiState.update {
                BookingUiState(options = it.options, latitude = it.latitude, longitude = it.longitude,
                    siteAddress = it.siteAddress, siteName = it.siteName)
            }
        }
    }

    // ---------------------------------------------------------------------------------------
    // Options
    // ---------------------------------------------------------------------------------------

    private fun loadOptions() {
        runWhenOnline {
            setIsLoading(true)
            repo.bookingOptionsApi(headers()).isRequestCallSuspendSuccess(
                success = { response ->
                    response?.data?.let { options ->
                        _uiState.update {
                            it.copy(
                                options = options,
                                paymentMethod = options.default_payment_method ?: it.paymentMethod,
                                hours = options.duration_types?.firstOrNull { d -> d.id == "hours" }?.default ?: it.hours,
                                days = options.duration_types?.firstOrNull { d -> d.id == "days" }?.default ?: it.days
                            )
                        }
                    }
                },
                failure = { _, _, message -> fail(message) }
            )
            setIsLoading(false)
        }
    }

    // ---------------------------------------------------------------------------------------
    // Location: phone GPS, or a typed address turned into a position by the system geocoder
    // ---------------------------------------------------------------------------------------

    private fun locateMe() {
        _uiState.update { it.copy(isLocating = true) }
        viewModelScope.launch {
            val fix = fetchCurrentLocation(app)
            if (fix == null) {
                _uiState.update { it.copy(isLocating = false) }
                showError(string(R.string.bk_msg_location_unavailable))
                return@launch
            }
            val address = reverseGeocode(fix.latitude, fix.longitude) ?: fix.address.orEmpty()
            _uiState.update {
                it.copy(
                    isLocating = false,
                    latitude = fix.latitude,
                    longitude = fix.longitude,
                    siteAddress = address,
                    siteName = it.siteName.ifBlank { fix.address.orEmpty() }
                )
            }
        }
    }

    private fun searchAddress(query: String) {
        if (query.isBlank()) return
        _uiState.update { it.copy(isLocating = true) }
        viewModelScope.launch {
            val found = withContext(Dispatchers.IO) {
                runCatching {
                    @Suppress("DEPRECATION")
                    Geocoder(app, Locale.getDefault()).getFromLocationName(query, 1)?.firstOrNull()
                }.getOrNull()
            }
            if (found == null) {
                _uiState.update { it.copy(isLocating = false) }
                showError(string(R.string.bk_msg_address_not_found))
                return@launch
            }
            _uiState.update {
                it.copy(
                    isLocating = false,
                    latitude = found.latitude,
                    longitude = found.longitude,
                    siteAddress = found.getAddressLine(0) ?: query
                )
            }
        }
    }

    private suspend fun reverseGeocode(latitude: Double, longitude: Double): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                @Suppress("DEPRECATION")
                Geocoder(app, Locale.getDefault()).getFromLocation(latitude, longitude, 1)?.firstOrNull()?.getAddressLine(0)
            }.getOrNull()
        }

    // ---------------------------------------------------------------------------------------
    // Search
    // ---------------------------------------------------------------------------------------

    private fun durationJson(s: BookingUiState): JSONObject = JSONObject().put("type", s.durationType).also {
        when (s.durationType) {
            "hours" -> it.put("hours", s.hours)
            "days" -> it.put("days", s.days)
        }
    }

    private fun scheduledAt(s: BookingUiState): String? =
        if (s.startType == "scheduled" && s.date.isNotEmpty() && s.time.isNotEmpty()) toIsoWithOffset(s.date, s.time) else null

    /** Requirement fields shared by search, crane details and create. */
    private fun requirement(s: BookingUiState): JSONObject = JSONObject()
        .put("latitude", s.latitude)
        .put("longitude", s.longitude)
        .put("start_type", s.startType)
        .put("duration", durationJson(s))
        .also { body -> scheduledAt(s)?.let { body.put("scheduled_at", it) } }

    private fun searchBody(s: BookingUiState, min: Int): JSONObject =
        requirement(s).put("crane_type", s.craneType).put("capacity", s.capacity)
            .put("min", min).put("max", min + PAGE)

    private fun search(reset: Boolean) {
        val s = _uiState.value
        if (s.craneType == null || s.capacity == null || s.latitude == null || s.longitude == null) return
        if (!reset && (!s.hasMore || s.isLoadingMore)) return
        val min = if (reset) 0 else s.nextMin
        if (!reset) _uiState.update { it.copy(isLoadingMore = true) }

        runWhenOnline {
            if (reset) setIsLoading(true)
            repo.searchCranesApi(headers(), searchBody(s, min)).isRequestCallSuspendSuccess(
                success = { response ->
                    val page = response?.data.orEmpty()
                    val paging = response?.pagination
                    _uiState.update {
                        val known = if (reset) emptyList() else it.results
                        it.copy(
                            results = known + page.filter { c -> known.none { k -> k.id == c.id } },
                            resultsMessage = response?.message,
                            searched = true,
                            isLoadingMore = false,
                            hasMore = paging?.has_more == true,
                            nextMin = paging?.max ?: (min + PAGE)
                        )
                    }
                    if (reset) _effect.send(BookingEffect.Go(BookingRoutes.RESULTS))
                },
                failure = { _, _, message ->
                    _uiState.update { it.copy(isLoadingMore = false) }
                    fail(message)
                }
            )
            if (reset) setIsLoading(false)
        }
    }

    /** Estimated Price card of the duration step: the cheapest crane of a silent search. */
    private fun estimatePrice() {
        val s = _uiState.value
        if (s.craneType == null || s.capacity == null || s.latitude == null || s.longitude == null) return
        viewModelScope.launch {
            repo.searchCranesApi(headers(), searchBody(s, 0)).isRequestCallSuspendSuccess(
                success = { response ->
                    val cheapest: AvailableCrane? = response?.data.orEmpty()
                        .filter { it.price != null }
                        .minByOrNull { it.hourly_rate ?: Double.MAX_VALUE }
                    _uiState.update { it.copy(estimate = cheapest) }
                },
                failure = { _, _, _ -> _uiState.update { it.copy(estimate = null) } }
            )
        }
    }

    // ---------------------------------------------------------------------------------------
    // Crane details and creating the booking
    // ---------------------------------------------------------------------------------------

    private fun openCrane(craneId: String) {
        val s = _uiState.value
        val body = requirement(s).put("crane_id", craneId).put("site_address", s.siteAddress)
        _uiState.update { it.copy(detail = null) }
        runWhenOnline {
            setIsLoading(true)
            repo.craneForBookingApi(headers(), body).isRequestCallSuspendSuccess(
                success = { response ->
                    response?.data?.let { crane ->
                        _uiState.update { it.copy(detail = crane) }
                        _effect.send(BookingEffect.Go(BookingRoutes.CRANE))
                    }
                },
                failure = { _, _, message -> fail(message) }
            )
            setIsLoading(false)
        }
    }

    private fun createBooking() {
        val s = _uiState.value
        val craneId = s.detail?.id ?: return
        val body = requirement(s)
            .put("crane_id", craneId)
            .put("site_name", s.siteName.ifBlank { s.siteAddress.take(60) })
            .put("site_address", s.siteAddress)
            .put("payment_method", s.paymentMethod)

        runWhenOnline {
            setIsLoading(true)
            repo.bookingActionApi(headers(), "create", body).isRequestCallSuspendSuccess(
                success = { response ->
                    response?.data?.let { booking ->
                        _uiState.update { it.copy(booking = booking) }
                        _effect.send(BookingEffect.Go(BookingRoutes.WAITING))
                    }
                },
                failure = { _, _, message ->
                    fail(message)
                    // Taken by someone else meanwhile: the list must be fetched again
                    if (message?.contains("already booked", ignoreCase = true) == true) {
                        _effect.send(BookingEffect.BackToResults)
                    }
                }
            )
            setIsLoading(false)
        }
    }

    // ---------------------------------------------------------------------------------------
    // The booking after it was created
    // ---------------------------------------------------------------------------------------

    /** POST /customer/booking/get without a loading overlay: it runs every 5 seconds. */
    private fun pollBooking(bookingId: String) {
        viewModelScope.launch {
            repo.bookingActionApi(headers(), "get", JSONObject().put("booking_id", bookingId))
                .isRequestCallSuspendSuccess(
                    success = { response -> response?.data?.let { b -> _uiState.update { it.copy(booking = b) } } }
                )
        }
    }

    private fun openBooking(bookingId: String) {
        runWhenOnline {
            setIsLoading(true)
            repo.bookingActionApi(headers(), "get", JSONObject().put("booking_id", bookingId))
                .isRequestCallSuspendSuccess(
                    success = { response ->
                        response?.data?.let { b ->
                            _uiState.update { it.copy(booking = b) }
                            _effect.send(
                                BookingEffect.Go(
                                    if (b.status == 0) BookingRoutes.WAITING else BookingRoutes.CONFIRMED
                                )
                            )
                        }
                    },
                    failure = { _, _, message -> fail(message) }
                )
            setIsLoading(false)
        }
    }

    private fun loadTrack(bookingId: String) {
        viewModelScope.launch {
            repo.trackApi(headers(), JSONObject().put("booking_id", bookingId)).isRequestCallSuspendSuccess(
                success = { response -> response?.data?.let { t -> _uiState.update { it.copy(track = t) } } }
            )
        }
    }

    private fun cancelBooking() {
        val id = _uiState.value.booking?.booking_id ?: return
        runWhenOnline {
            setIsLoading(true)
            repo.bookingActionApi(headers(), "cancel", JSONObject().put("booking_id", id))
                .isRequestCallSuspendSuccess(
                    success = { response ->
                        response?.data?.let { b -> _uiState.update { it.copy(booking = b) } }
                        showSuccess(string(R.string.bk_msg_cancelled))
                        _effect.send(BookingEffect.Close)
                    },
                    failure = { _, _, message -> fail(message) }
                )
            setIsLoading(false)
        }
    }

    // ---------------------------------------------------------------------------------------
    // My bookings
    // ---------------------------------------------------------------------------------------

    private fun loadBookings(group: String, reset: Boolean) {
        val s = _uiState.value
        if (!reset && !s.bookingsHasMore) return
        bookingsGroup = group
        val min = if (reset) 0 else s.bookingsNextMin
        runWhenOnline {
            if (reset) setIsLoading(true)
            val body = JSONObject().put("group", group).put("min", min).put("max", min + PAGE)
            repo.bookingListApi(headers(), body).isRequestCallSuspendSuccess(
                success = { response ->
                    val page = response?.data.orEmpty()
                    val paging = response?.pagination
                    _uiState.update {
                        val known = if (reset) emptyList() else it.bookings
                        it.copy(
                            bookings = known + page.filter { b -> known.none { k -> k.booking_id == b.booking_id } },
                            bookingsLoaded = true,
                            bookingsHasMore = paging?.has_more == true,
                            bookingsNextMin = paging?.max ?: (min + PAGE)
                        )
                    }
                },
                failure = { _, _, message ->
                    _uiState.update { it.copy(bookingsLoaded = true) }
                    fail(message)
                }
            )
            if (reset) setIsLoading(false)
        }
    }

    /** The "Cancellation and Refunds" section of POST /content/legal { type: terms }. */
    private fun loadPolicy() {
        if (_uiState.value.policy != null) return
        viewModelScope.launch {
            repo.legalApi(emptyMap(), JSONObject().put("type", "terms")).isRequestCallSuspendSuccess(
                success = { response ->
                    val section = response?.data?.sections.orEmpty()
                        .firstOrNull { it.title?.contains("cancellation", ignoreCase = true) == true }
                    section?.body?.let { text -> _uiState.update { it.copy(policy = text) } }
                }
            )
        }
    }
}
