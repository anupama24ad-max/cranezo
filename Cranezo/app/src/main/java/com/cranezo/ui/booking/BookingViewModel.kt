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
            is BookingIntent.OpenTrack -> openTrack(intent.bookingId)
            BookingIntent.ConfirmArrival -> confirmArrival()
            is BookingIntent.RequestExtra -> extensionCall("extension/request", intent.minutes, intent.message)
            BookingIntent.CancelExtra -> extensionCall("extension/cancel", null, "")
            is BookingIntent.Pay -> payOrRate("pay", JSONObject().put("payment_method", intent.method), BookingRoutes.PAID)
            is BookingIntent.Rate -> payOrRate("rate", JSONObject().put("rating", intent.stars).also { if (intent.comment.isNotBlank()) it.put("comment", intent.comment) }, null)
            is BookingIntent.LoadJobs -> loadJobs(intent.tab, reset = true)
            BookingIntent.LoadMoreJobs -> loadJobs(_uiState.value.jobsTab, reset = false)
            BookingIntent.LoadActiveTracking -> loadActiveTracking()
            BookingIntent.BookAgain -> bookAgain()
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

    // ---- address suggestions while typing (free: the phone's own address search) ----

    data class AddressSuggestion(val label: String, val latitude: Double, val longitude: Double)

    private val _suggestions = MutableStateFlow<List<AddressSuggestion>>(emptyList())
    val suggestions: StateFlow<List<AddressSuggestion>> = _suggestions.asStateFlow()
    private var suggestJob: kotlinx.coroutines.Job? = null

    /** Called on every key press; waits a moment so it does not search on each letter */
    fun suggestAddresses(query: String) {
        suggestJob?.cancel()
        if (query.trim().length < 3) {
            _suggestions.value = emptyList()
            return
        }
        suggestJob = viewModelScope.launch {
            kotlinx.coroutines.delay(400)
            // the server's search first (it knows villages and small towns); the phone's own search is the backup
            var list = serverPlaces(query)
            if (list.isEmpty()) {
                val found = withContext(Dispatchers.IO) {
                    runCatching {
                        @Suppress("DEPRECATION")
                        Geocoder(app, Locale.getDefault()).getFromLocationName("$query, India", 5).orEmpty()
                    }.getOrDefault(emptyList())
                }
                list = found.mapNotNull { a ->
                    val line = a.getAddressLine(0) ?: return@mapNotNull null
                    AddressSuggestion(line, a.latitude, a.longitude)
                }.distinctBy { it.label }
            }
            _suggestions.value = list
        }
    }

    /** Address search on the server (OpenStreetMap data); an empty list when it is not available */
    private suspend fun serverPlaces(query: String): List<AddressSuggestion> {
        var out = emptyList<AddressSuggestion>()
        val st = _uiState.value
        val body = JSONObject().put("q", query)
        st.latitude?.let { body.put("latitude", it) }
        st.longitude?.let { body.put("longitude", it) }
        repo.placesSearchApi(headers(), body).isRequestCallSuspendSuccess(success = { r ->
            out = r?.data.orEmpty().mapNotNull { p ->
                val la = p.latitude ?: return@mapNotNull null
                val lo = p.longitude ?: return@mapNotNull null
                AddressSuggestion(p.label ?: return@mapNotNull null, la, lo)
            }
        })
        return out
    }

    /** The address for a point picked on the map (the phone first, then the server) */
    suspend fun addressAt(latitude: Double, longitude: Double): String? = reverseGeocode(latitude, longitude)

    /** A spot chosen on the map */
    fun pickOnMap(latitude: Double, longitude: Double, address: String?) {
        _suggestions.value = emptyList()
        _uiState.update {
            it.copy(
                latitude = latitude, longitude = longitude,
                siteAddress = address?.takeIf { a -> a.isNotBlank() } ?: it.siteAddress.ifBlank { String.format(Locale.US, "%.5f, %.5f", latitude, longitude) },
                siteName = it.siteName.ifBlank { address.orEmpty().substringBefore(",") }
            )
        }
    }

    fun clearSuggestions() {
        _suggestions.value = emptyList()
    }

    // ---- the yellow route along the roads (crane to site) ----

    private val _route = MutableStateFlow<List<com.google.android.gms.maps.model.LatLng>>(emptyList())
    val route: StateFlow<List<com.google.android.gms.maps.model.LatLng>> = _route.asStateFlow()

    fun loadRoute(fromLat: Double, fromLng: Double, toLat: Double, toLng: Double) {
        viewModelScope.launch {
            val points = com.cranezo.utils.loadRoutePoints(repo, headers(), fromLat, fromLng, toLat, toLng)
            if (points.isNotEmpty()) _route.value = points
        }
    }

    fun pickSuggestion(s: AddressSuggestion) {
        _suggestions.value = emptyList()
        _uiState.update {
            it.copy(latitude = s.latitude, longitude = s.longitude, siteAddress = s.label, siteName = it.siteName.ifBlank { s.label.substringBefore(",") })
        }
    }

    // ---- booking for someone else: the person at the site ----

    private val _contact = MutableStateFlow(Triple(false, "", ""))
    /** (for someone else?, name, phone) */
    val siteContact: StateFlow<Triple<Boolean, String, String>> = _contact.asStateFlow()

    fun setSiteContact(forOther: Boolean, name: String, phone: String) {
        _contact.value = Triple(forOther, name.take(100), phone.filter { it.isDigit() }.take(10))
    }

    /** Location step is complete: a place, and a valid contact when booking for someone else */
    fun locationReady(): Boolean {
        val s = _uiState.value
        val (other, name, phone) = _contact.value
        return s.latitude != null && s.siteAddress.isNotBlank() && (!other || (name.isNotBlank() && phone.length == 10))
    }

    private fun searchAddress(query: String) {
        if (query.isBlank()) return
        _uiState.update { it.copy(isLocating = true) }
        viewModelScope.launch {
            val hit = serverPlaces(query).firstOrNull()
            if (hit != null) {
                _uiState.update { it.copy(isLocating = false, latitude = hit.latitude, longitude = hit.longitude, siteAddress = hit.label) }
                return@launch
            }
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

    private suspend fun reverseGeocode(latitude: Double, longitude: Double): String? {
        val phone = withContext(Dispatchers.IO) {
            runCatching {
                @Suppress("DEPRECATION")
                Geocoder(app, Locale.getDefault()).getFromLocation(latitude, longitude, 1)?.firstOrNull()?.getAddressLine(0)
            }.getOrNull()
        }
        if (!phone.isNullOrBlank()) return phone
        var label: String? = null
        repo.placesReverseApi(headers(), JSONObject().put("latitude", latitude).put("longitude", longitude))
            .isRequestCallSuspendSuccess(success = { r -> label = r?.data?.label })
        return label
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

    /**
     * The 5 steps as one crane request (Rapido style: sent to every nearby owner).
     * Null while a step is still missing.
     */
    fun requestBody(): JSONObject? {
        val s = _uiState.value
        if (s.craneType == null || s.capacity == null || s.latitude == null || s.longitude == null) return null
        val address = s.siteAddress.ifBlank { String.format(Locale.US, "%.5f, %.5f", s.latitude, s.longitude) }
        val (other, contactName, contactPhone) = _contact.value
        return requirement(s)
            .put("crane_type", s.craneType)
            .put("capacity", s.capacity)
            .put("site_address", address)
            .also { body -> if (s.siteName.isNotBlank()) body.put("site_name", s.siteName) }
            .also { body ->
                // booking for someone else: the operator calls this person
                if (other && contactName.isNotBlank() && contactPhone.isNotBlank()) {
                    body.put("contact_name", contactName.trim()).put("contact_phone", contactPhone)
                }
            }
    }

    /** Names shown on the review screen, e.g. "50 Ton" and "Mobile Crane" */
    fun chosenNames(): Pair<String, String> {
        val s = _uiState.value
        val type = s.options?.crane_types?.firstOrNull { it.id == s.craneType }?.name.orEmpty()
        // "16" -> "16 Ton"
        val capacity = CapacityInfo.withUnit(s.options?.capacities?.firstOrNull { it.id == s.capacity }?.name, string(R.string.unit_ton))
        return capacity to type
    }

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
                            _uiState.update { it.copy(booking = b, track = null, trackLoaded = false) }
                            _effect.send(
                                BookingEffect.Go(BookingRoutes.DETAILS)
                            )
                        }
                    },
                    failure = { _, _, message -> fail(message) }
                )
            setIsLoading(false)
        }
    }

    /** Opens the live tracking of a booking directly (the booking is loaded first so the screen knows which one) */
    private fun openTrack(bookingId: String) {
        runWhenOnline {
            setIsLoading(true)
            repo.bookingActionApi(headers(), "get", JSONObject().put("booking_id", bookingId))
                .isRequestCallSuspendSuccess(
                    success = { response ->
                        response?.data?.let { b ->
                            _uiState.update { it.copy(booking = b, track = null, trackLoaded = false) }
                            _effect.send(BookingEffect.Go(BookingRoutes.TRACK))
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
                success = { response -> response?.data?.let { t -> _uiState.update { it.copy(track = t, trackLoaded = true) } } ?: _uiState.update { it.copy(trackLoaded = true) } },
                failure = { _, _, _ -> _uiState.update { it.copy(trackLoaded = true) } }
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
    // ---------------------------------------------------------------------------------------
    // After the booking is confirmed: arrival, extra time, payment, rating, My Jobs
    // ---------------------------------------------------------------------------------------

    private fun currentId() = _uiState.value.booking?.booking_id

    /** "Start Job": the customer accepts the crane on site. The booking is fetched again afterwards. */
    private fun confirmArrival() {
        val id = currentId() ?: return
        runWhenOnline {
            setIsLoading(true)
            repo.bookingActionApi(headers(), "confirm-arrival", JSONObject().put("booking_id", id)).isRequestCallSuspendSuccess(
                success = { pollBooking(id) },
                failure = { _, _, m ->
                    fail(m)
                    pollBooking(id)
                }
            )
            setIsLoading(false)
        }
    }

    /** extension/request (minutes + optional message) or extension/cancel. Both answer with { extension, booking }. */
    private fun extensionCall(path: String, minutes: Int?, message: String) {
        val id = currentId() ?: return
        val body = JSONObject().put("booking_id", id)
        minutes?.let { body.put("minutes", it) }
        if (message.isNotBlank()) body.put("message", message)
        runWhenOnline {
            setIsLoading(true)
            repo.extensionApi(headers(), path, body).isRequestCallSuspendSuccess(
                success = { r ->
                    r?.data?.booking?.let { b -> _uiState.update { it.copy(booking = b) } }
                    _effect.send(if (minutes != null) BookingEffect.Go(BookingRoutes.EXTRA_STATUS) else BookingEffect.Go(BookingRoutes.DETAILS, popUpTo = BookingRoutes.EXTRA_STATUS))
                },
                failure = { _, _, m ->
                    fail(m)
                    pollBooking(id)
                }
            )
            setIsLoading(false)
        }
    }

    /** pay (COD) and rate answer with the booking, which replaces the local copy. */
    /** The final payment went through online (Razorpay, verified by the server): refresh and show Payment Successful */
    fun showPaidOnline() {
        val id = currentId() ?: return
        pollBooking(id)
        viewModelScope.launch { _effect.send(BookingEffect.Go(BookingRoutes.PAID, popUpTo = BookingRoutes.PAY)) }
    }

    /** Booking shown on the final payment screen */
    fun currentBookingId(): String? = currentId()

    private fun payOrRate(action: String, extra: JSONObject, goTo: String?) {
        val id = currentId() ?: return
        runWhenOnline {
            setIsLoading(true)
            repo.bookingActionApi(headers(), action, extra.put("booking_id", id)).isRequestCallSuspendSuccess(
                success = { r ->
                    r?.data?.let { b -> _uiState.update { it.copy(booking = b) } }
                    if (goTo != null) _effect.send(BookingEffect.Go(goTo, popUpTo = BookingRoutes.PAY))
                    else showSuccess(string(R.string.bk_msg_rated), autoHideMs = 2000L)
                },
                failure = { _, _, m ->
                    fail(m)
                    pollBooking(id)
                }
            )
            setIsLoading(false)
        }
    }

    private fun loadJobs(tab: String, reset: Boolean) {
        val s = _uiState.value
        if (!reset && !s.jobsHasMore) return
        val min = if (reset) 0 else s.jobsNextMin
        if (reset) _uiState.update { it.copy(jobsTab = tab, jobs = if (tab != it.jobsTab) emptyList() else it.jobs, jobsLoaded = false) }
        runWhenOnline {
            if (reset) {
                repo.jobCountsApi(headers()).isRequestCallSuspendSuccess(
                    success = { r -> _uiState.update { it.copy(counts = r?.data) } },
                    failure = { _, _, m -> fail(m) }
                )
            }
            val body = JSONObject().put("tab", tab).put("min", min).put("max", min + PAGE)
            repo.myJobsApi(headers(), body).isRequestCallSuspendSuccess(
                success = { r ->
                    val page = r?.data.orEmpty()
                    _uiState.update {
                        if (it.jobsTab != tab) return@update it
                        val known = if (reset) emptyList() else it.jobs
                        it.copy(
                            jobs = known + page.filter { j -> known.none { k -> k.booking_id == j.booking_id } },
                            jobsLoaded = true,
                            jobsHasMore = r?.pagination?.has_more == true,
                            jobsNextMin = r?.pagination?.max ?: (min + PAGE)
                        )
                    }
                },
                failure = { _, _, m ->
                    _uiState.update { it.copy(jobsLoaded = true) }
                    fail(m)
                }
            )
        }
    }

    private fun loadActiveTracking() {
        viewModelScope.launch {
            repo.activeTrackingApi(headers()).isRequestCallSuspendSuccess(
                success = { r -> _uiState.update { it.copy(activeTracks = r?.data.orEmpty(), activeLoaded = true) } },
                failure = { _, _, _ -> _uiState.update { it.copy(activeLoaded = true) } }
            )
        }
    }

    /** Book Again: the steps are pre-filled from the old booking; date and time are chosen again. */
    private fun bookAgain() {
        val r = _uiState.value.booking?.rebook ?: return
        _uiState.update {
            it.copy(
                craneType = r.crane_type ?: it.craneType,
                capacity = r.capacity ?: it.capacity,
                latitude = r.latitude,
                longitude = r.longitude,
                siteName = r.site_name.orEmpty(),
                siteAddress = r.site_address.orEmpty(),
                startType = "now",
                date = "",
                time = "",
                durationType = r.duration?.type ?: "hours",
                hours = r.duration?.hours ?: it.hours,
                days = r.duration?.days ?: it.days,
                results = emptyList(),
                searched = false
            )
        }
        viewModelScope.launch { _effect.send(BookingEffect.Go(BookingRoutes.DATETIME)) }
    }
}
