package com.cranezo.ui.operator

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import com.cranezo.R
import com.cranezo.api.isRequestCallSuspendSuccess
import com.cranezo.core.BaseViewModel
import com.cranezo.repository.Repository
import com.cranezo.ui.booking.model.AppNotification
import com.cranezo.ui.operator.model.OperatorTabsIntent
import com.cranezo.ui.operator.model.OperatorTabsState
import com.cranezo.utils.AppMethods
import com.cranezo.utils.SessionManager
import com.cranezo.utils.fetchCurrentLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

/** Operator Home, Jobs and Earnings tabs, plus the "new job assigned" alert. */
@HiltViewModel
class OperatorTabsViewModel @Inject constructor(
    var app: Application,
    var repo: Repository,
    var sm: SessionManager
) : BaseViewModel(app) {

    private val _uiState = MutableStateFlow(OperatorTabsState())
    val uiState: StateFlow<OperatorTabsState> = _uiState.asStateFlow()

    private companion object {
        const val PAGE = 10
    }

    private var lastUnread = -1
    private val seen = mutableSetOf<String>()

    private fun headers() = AppMethods.getToken(sm)

    private fun fail(message: String?) =
        showError(message?.takeIf { it.isNotBlank() } ?: string(R.string.crane_msg_generic_error), autoHideMs = 2500L)

    init {
        // Push needs Firebase (not set up yet), so the app asks for new jobs every 10 seconds
        viewModelScope.launch {
            while (true) {
                runCatching { pollAssignments() }
                delay(10_000)
            }
        }
    }

    fun onIntent(intent: OperatorTabsIntent) {
        when (intent) {
            OperatorTabsIntent.LoadHome -> loadHome()
            OperatorTabsIntent.ToggleOnline -> toggleOnline()
            is OperatorTabsIntent.LoadJobs -> loadJobs(intent.tab, reset = true)
            OperatorTabsIntent.LoadMoreJobs -> loadJobs(_uiState.value.jobsTab, reset = false)
            OperatorTabsIntent.LoadEarnings -> loadEarnings(reset = true)
            OperatorTabsIntent.LoadMoreEarnings -> loadEarnings(reset = false)
            OperatorTabsIntent.DismissIncoming -> _uiState.update { it.copy(incomingJob = null) }
        }
    }

    private fun loadHome() {
        runWhenOnline {
            repo.opHome(headers()).isRequestCallSuspendSuccess(
                success = { r -> _uiState.update { it.copy(home = r?.data, homeLoaded = true) } },
                failure = { _, _, m ->
                    _uiState.update { it.copy(homeLoaded = true) }
                    fail(m)
                }
            )
        }
    }

    private fun toggleOnline() {
        val goOnline = _uiState.value.home?.online != true
        viewModelScope.launch {
            val body = JSONObject().put("online", goOnline)
            // Coordinates are optional: only when the permission is already there
            if (ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation(app)?.let { body.put("latitude", it.latitude).put("longitude", it.longitude) }
            }
            repo.opStatus(headers(), body).isRequestCallSuspendSuccess(
                success = { r ->
                    _uiState.update {
                        it.copy(
                            home = it.home?.copy(
                                online = r?.data?.online ?: goOnline,
                                online_label = r?.data?.online_label ?: it.home.online_label,
                                online_message = null
                            )
                        )
                    }
                    loadHome()
                },
                failure = { _, _, m -> fail(m) }
            )
        }
    }

    private fun loadJobs(tab: String, reset: Boolean) {
        val s = _uiState.value
        if (!reset && !s.jobsHasMore) return
        val min = if (reset) 0 else s.jobsNextMin
        if (reset) _uiState.update { it.copy(jobsTab = tab, jobs = if (tab != it.jobsTab) emptyList() else it.jobs, jobsLoaded = false) }
        runWhenOnline {
            val body = JSONObject().put("tab", tab).put("min", min).put("max", min + PAGE)
            repo.opJobList(headers(), body).isRequestCallSuspendSuccess(
                success = { r ->
                    val page = r?.data.orEmpty()
                    _uiState.update {
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

    private fun loadEarnings(reset: Boolean) {
        val s = _uiState.value
        if (!reset && !s.earningsHasMore) return
        val min = if (reset) 0 else s.earningsNextMin
        runWhenOnline {
            if (reset) {
                repo.opEarningsSummary(headers()).isRequestCallSuspendSuccess(
                    success = { r -> _uiState.update { it.copy(earnings = r?.data) } },
                    failure = { _, _, m -> fail(m) }
                )
            }
            val body = JSONObject().put("min", min).put("max", min + PAGE)
            repo.opEarningsList(headers(), body).isRequestCallSuspendSuccess(
                success = { r ->
                    val page = r?.data.orEmpty()
                    _uiState.update {
                        val known = if (reset) emptyList() else it.earningItems
                        it.copy(
                            earningItems = known + page.filter { e -> known.none { k -> k.booking_id == e.booking_id } },
                            earningsTotal = r?.pagination?.total ?: (known.size + page.size),
                            earningsLoaded = true,
                            earningsHasMore = r?.pagination?.has_more == true,
                            earningsNextMin = r?.pagination?.max ?: (min + PAGE)
                        )
                    }
                },
                failure = { _, _, m ->
                    _uiState.update { it.copy(earningsLoaded = true) }
                    fail(m)
                }
            )
        }
    }

    /** `job_assigned` notifications become the "New Job Request" alert. */
    private suspend fun pollAssignments() {
        if (_uiState.value.incomingJob != null) return
        var unread = -1
        repo.unreadCountApi(headers()).isRequestCallSuspendSuccess(success = { unread = it?.data?.unread ?: -1 })
        if (unread < 0 || unread == lastUnread) return
        lastUnread = unread
        if (unread == 0) return

        var items = emptyList<AppNotification>()
        repo.notificationsApi(headers(), JSONObject().put("unread_only", true).put("min", 0).put("max", 20))
            .isRequestCallSuspendSuccess(success = { items = it?.data.orEmpty() })

        for (item in items) {
            val id = item.id ?: continue
            if (item.type != "job_assigned" || !seen.add(id)) continue
            val bookingId = item.data?.get("booking_id") as? String ?: continue
            repo.markReadApi(headers(), JSONObject().put("ids", JSONArray().put(id)))
            _uiState.update { it.copy(incomingJob = bookingId) }
            loadHome()
            return
        }
    }
}
