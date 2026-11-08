package com.cranezo.ui.mainhome

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.cranezo.core.BaseViewModel
import com.cranezo.R
import com.cranezo.api.isRequestCallSuspendSuccess
import com.cranezo.repository.Repository
import com.cranezo.ui.chooserole.UserRole
import com.cranezo.ui.booking.model.AppNotification
import com.cranezo.ui.booking.model.OwnerRequest
import kotlinx.coroutines.delay
import com.cranezo.ui.mainhome.model.MainHomeEffect
import com.cranezo.ui.mainhome.model.MainHomeIntent
import com.cranezo.ui.mainhome.model.MainHomeUiState
import com.cranezo.ui.mainhome.model.MyCranesState
import com.cranezo.ui.myprofile.ProfileStore
import com.cranezo.ui.myprofile.model.ProfileData
import com.cranezo.utils.AppMethods
import org.json.JSONObject
import com.cranezo.ui.theme.AppThemeState
import com.cranezo.utils.AppStrings
import com.cranezo.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class MainHomeViewModel @Inject constructor(
    var app: Application,
    var repo: Repository,
    var sm: SessionManager,
    private val store: ProfileStore
) : BaseViewModel(app) {

    private val TAG = "MainHomeViewModel"

    private val role = runCatching { UserRole.valueOf(sm.getUserRole()) }
        .getOrDefault(UserRole.CRANE_OWNER)

    private val _uiState = MutableStateFlow(
        MainHomeUiState(
            role = role,
            userName = sm.getData(AppStrings.SessionValues.name, ""),
            memberId = sm.getData(AppStrings.SessionValues.memberId, ""),
            mobileNumber = "${
                sm.getData(AppStrings.SessionValues.countryCode, AppStrings.Constants.defaultCountryCode)
            } ${sm.getData(AppStrings.SessionValues.contactNumber, "")}".trim()
        )
    )
    val uiState: StateFlow<MainHomeUiState> = _uiState.asStateFlow()

    /** The signed-in user's profile from POST /profile/me (null until it has been loaded). */
    val profile: StateFlow<ProfileData?> = store.profile

    private companion object {
        const val CRANES_PAGE_SIZE = 10
        const val LOGOUT_TIMEOUT_MS = 4000L
    }

    private val _cranes = MutableStateFlow(MyCranesState())
    val cranesState: StateFlow<MyCranesState> = _cranes.asStateFlow()

    private val _effect = Channel<MainHomeEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        // Keep this phone's push token on the server (job requests, arrivals, payments arrive as push)
        com.cranezo.push.CranezoMessagingService.syncToken(viewModelScope, repo, sm)
        // One network check for both calls: calls started in parallel would cancel each other's
        val loadCranes = role == UserRole.CRANE_OWNER
        if (loadCranes) _cranes.update { it.copy(isLoading = true, error = null) }
        runWhenOnline {
            fetchProfile()
            if (loadCranes) fetchCranes(min = 0, reset = true)
        }
        if (role == UserRole.CRANE_OWNER) {
            viewModelScope.launch {
                while (true) {
                    runCatching { pollRequests() }
                    delay(10_000)
                }
            }
        }
    }

    /** Owner Home dashboard: live numbers from POST /owner/home */
    private fun loadOwnerHome() {
        runWhenOnline {
            repo.financePost("owner/home", AppMethods.getToken(sm)).isRequestCallSuspendSuccess(
                success = { r ->
                    val d = r?.data?.takeIf { it.isJsonObject }?.asJsonObject ?: return@isRequestCallSuspendSuccess
                    val fleet = d.getAsJsonObject("fleet")
                    fun int(o: com.google.gson.JsonObject?, k: String) = o?.get(k)?.takeIf { !it.isJsonNull }?.asInt ?: 0
                    val earnings = d.get("todays_earnings")?.takeIf { !it.isJsonNull }?.asDouble ?: 0.0
                    _uiState.update {
                        it.copy(
                            owner = it.owner.copy(
                                fleet = com.cranezo.ui.mainhome.model.FleetOverview(
                                    totalCranes = int(fleet, "total_cranes"),
                                    availableCranes = int(fleet, "available_cranes"),
                                    busyCranes = int(fleet, "busy_cranes"),
                                    pendingVerification = int(fleet, "pending_verification")
                                ),
                                todaysJobs = int(d, "todays_jobs"),
                                todaysEarnings = com.cranezo.utils.rupees(earnings),
                                craneRequests = int(d, "crane_requests"),
                                jobsNeedingOperator = int(d, "jobs_needing_operator"),
                                dueAmount = d.getAsJsonObject("operator_dues")?.get("amount")?.takeIf { v -> !v.isJsonNull }?.asDouble ?: 0.0,
                                dueOperators = int(d.getAsJsonObject("operator_dues"), "operators"),
                                dueJobs = int(d.getAsJsonObject("operator_dues"), "jobs"),
                                dueJobsNoCommission = int(d.getAsJsonObject("operator_dues"), "jobs_without_commission"),
                                subState = d.getAsJsonObject("subscription")?.get("state")?.takeIf { v -> !v.isJsonNull }?.asString ?: "none",
                                subLevel = d.getAsJsonObject("subscription")?.get("level")?.takeIf { v -> !v.isJsonNull }?.asString ?: "ok",
                                subMessage = d.getAsJsonObject("subscription")?.get("message")?.takeIf { v -> !v.isJsonNull }?.asString.orEmpty()
                            )
                        )
                    }
                }
            )
        }
    }

    fun onIntent(intent: MainHomeIntent) {
        when (intent) {
            MainHomeIntent.RefreshCranes -> loadCranes(reset = true)

            MainHomeIntent.RefreshOwnerHome -> loadOwnerHome()

            MainHomeIntent.LoadMoreCranes -> loadCranes(reset = false)

            MainHomeIntent.RefreshProfile -> runWhenOnline { fetchProfile() }

            MainHomeIntent.AcceptRequest -> respondToRequest(accept = true)

            MainHomeIntent.DeclineRequest -> respondToRequest(accept = false)

            MainHomeIntent.DismissRequest -> _uiState.update { it.copy(request = null, requestAccepted = false) }

            MainHomeIntent.ToggleOnline -> _uiState.update { it.copy(isOnline = !it.isOnline) }

            is MainHomeIntent.ThemeSelected -> AppThemeState.setThemeMode(intent.mode, app)

            MainHomeIntent.Logout -> logout()
        }
    }

    /**
     * POST /profile/me. It also brings the theme in line with the server. When the language was
     * changed on another device it is applied and the screens restart to show it.
     */
    private suspend fun fetchProfile() {
        val languageChanged = store.refresh(repo, sm, app)
        store.profile.value?.let { data ->
            _uiState.update {
                it.copy(
                    userName = data.full_name ?: it.userName,
                    memberId = data.member_id ?: it.memberId
                )
            }
        }
        if (languageChanged) _effect.send(MainHomeEffect.LanguageChanged)
    }

    /**
     * POST /owner/getCranes { min, max }. min is the index of the first record and max the index after
     * the last one, so page 1 is 0..10 and page 2 is 10..20. Every crane comes back with its status;
     * there is no status filter.
     */
    private fun loadCranes(reset: Boolean) {
        val current = _cranes.value
        if (current.isLoading || current.isLoadingMore) return
        if (!reset && !current.hasMore) return

        val min = if (reset) 0 else current.nextMin
        _cranes.update {
            if (reset) it.copy(isLoading = true, error = null) else it.copy(isLoadingMore = true)
        }

        runWhenOnline { fetchCranes(min, reset) }
    }

    private suspend fun fetchCranes(min: Int, reset: Boolean) {
        val body = JSONObject().put("min", min).put("max", min + CRANES_PAGE_SIZE)
        repo.getCranesApi(AppMethods.getToken(sm), body).isRequestCallSuspendSuccess(
            success = { response ->
                val page = response?.data.orEmpty()
                val paging = response?.pagination
                _cranes.update {
                    val known = if (reset) emptyList() else it.items
                    it.copy(
                        items = known + page.filter { crane -> known.none { k -> k.id == crane.id } },
                        isLoading = false,
                        isLoadingMore = false,
                        loaded = true,
                        hasMore = paging?.has_more == true,
                        nextMin = paging?.max ?: (min + CRANES_PAGE_SIZE)
                    )
                }
            },
            failure = { _, _, message ->
                val text = message?.takeIf { m -> m.isNotBlank() } ?: string(R.string.crane_msg_generic_error)
                _cranes.update { it.copy(isLoading = false, isLoadingMore = false, loaded = true, error = text) }
                showError(text)
            })
    }

    /**
     * POST /auth/logout only detaches this device from push notifications, and tokens are stateless,
     * so a failure (no network, expired token) must never block the logout. What logs the user out
     * is deleting the tokens and the cached profile.
     */
    private fun logout() {
        val headers = AppMethods.getToken(sm)
        viewModelScope.launch {
            withTimeoutOrNull(LOGOUT_TIMEOUT_MS) { runCatching { repo.logoutApi(headers) } }

            // The theme and language are device preferences, so they survive a logout
            val theme = sm.getAppTheme()
            val languageId = sm.getData(AppStrings.SessionValues.languageId, AppStrings.Constants.defaultLanguageId)
            sm.clearSession()
            sm.setAppTheme(theme)
            sm.setData(AppStrings.SessionValues.languageId, languageId)
            store.clear()
            _effect.send(MainHomeEffect.NavigateToLogin)
        }
    }
    // ---------------------------------------------------------------------------------------
    // Owner: the buzzer. Push needs Firebase (not set up yet), so the app asks every 10 seconds.
    // ---------------------------------------------------------------------------------------

    private var lastUnread = -1
    private val seenRequests = mutableSetOf<String>()

    private suspend fun pollRequests() {
        if (_uiState.value.request != null) return // a popup is open
        val headers = AppMethods.getToken(sm)

        var unread = -1
        repo.unreadCountApi(headers).isRequestCallSuspendSuccess(success = { unread = it?.data?.unread ?: -1 })
        if (unread < 0) return
        if (unread == lastUnread) return
        lastUnread = unread
        if (unread == 0) return

        var items = emptyList<AppNotification>()
        repo.notificationsApi(headers, JSONObject().put("unread_only", true).put("min", 0).put("max", 20))
            .isRequestCallSuspendSuccess(success = { items = it?.data.orEmpty() })

        for (item in items) {
            val id = item.id ?: continue
            val bookingId = item.data?.get("booking_id") as? String ?: continue
            if (item.type != "booking_request" || !seenRequests.add(id)) continue

            var request: OwnerRequest? = null
            repo.ownerRequestApi(headers, "get", JSONObject().put("booking_id", bookingId))
                .isRequestCallSuspendSuccess(success = { request = it?.data })
            // Whatever the answer, this notification has been dealt with
            repo.markReadApi(headers, JSONObject().put("ids", org.json.JSONArray().put(id)))

            val open = request
            if (open != null && open.can_respond == true) {
                _uiState.update { it.copy(request = open, requestAccepted = false) }
                return
            }
        }
    }

    private fun respondToRequest(accept: Boolean) {
        val bookingId = _uiState.value.request?.booking_id ?: return
        viewModelScope.launch {
            val headers = AppMethods.getToken(sm)
            repo.ownerRequestApi(headers, if (accept) "accept" else "decline", JSONObject().put("booking_id", bookingId))
                .isRequestCallSuspendSuccess(
                    success = { response ->
                        if (accept) _uiState.update { it.copy(request = response?.data ?: it.request, requestAccepted = true) }
                        else _uiState.update { it.copy(request = null, requestAccepted = false) }
                    },
                    // Already answered, expired or the crane is busy: show the reason and close the popup
                    failure = { _, _, message ->
                        showError(message?.takeIf { m -> m.isNotBlank() } ?: string(R.string.crane_msg_generic_error), autoHideMs = 2500L)
                        _uiState.update { it.copy(request = null, requestAccepted = false) }
                    }
                )
        }
    }
}
