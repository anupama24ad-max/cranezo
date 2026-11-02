package com.cranezo.ui.ownerjobs

import android.app.Application
import com.cranezo.R
import com.cranezo.api.isRequestCallSuspendSuccess
import com.cranezo.core.BaseViewModel
import com.cranezo.repository.Repository
import com.cranezo.ui.ownerjobs.model.OwnerEarningsIntent
import com.cranezo.ui.ownerjobs.model.OwnerEarningsState
import com.cranezo.ui.ownerjobs.model.OwnerJobDetailIntent
import com.cranezo.ui.ownerjobs.model.OwnerJobDetailState
import com.cranezo.ui.ownerjobs.model.OwnerJobsIntent
import com.cranezo.ui.ownerjobs.model.OwnerJobsState
import com.cranezo.utils.AppMethods
import com.cranezo.utils.SessionManager
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

private const val PAGE = 10

// ---------------------------------------------------------------------------------------------
// Jobs tab: New Requests, Upcoming, Active, Completed
// ---------------------------------------------------------------------------------------------

@HiltViewModel
class OwnerJobsViewModel @Inject constructor(
    var app: Application,
    var repo: Repository,
    var sm: SessionManager
) : BaseViewModel(app) {

    private val _uiState = MutableStateFlow(OwnerJobsState())
    val uiState: StateFlow<OwnerJobsState> = _uiState.asStateFlow()

    private fun headers() = AppMethods.getToken(sm)

    private fun fail(message: String?) =
        showError(message?.takeIf { it.isNotBlank() } ?: string(R.string.crane_msg_generic_error), autoHideMs = 2500L)

    fun onIntent(intent: OwnerJobsIntent) {
        when (intent) {
            OwnerJobsIntent.Refresh -> load(reset = true)
            is OwnerJobsIntent.SelectTab -> {
                _uiState.update { it.copy(tab = intent.tab, jobs = emptyList(), loaded = false) }
                load(reset = true)
            }
            OwnerJobsIntent.ToggleSort -> {
                _uiState.update { it.copy(newestFirst = !it.newestFirst) }
                load(reset = true)
            }
            OwnerJobsIntent.LoadMore -> load(reset = false)
            is OwnerJobsIntent.Accept -> respond(intent.bookingId, accept = true)
            is OwnerJobsIntent.Decline -> respond(intent.bookingId, accept = false)
            OwnerJobsIntent.DismissAccepted -> _uiState.update { it.copy(accepted = null) }
        }
    }

    /** Counts for the tab chips and the list of the selected tab, one after another. */
    private fun load(reset: Boolean) {
        val s = _uiState.value
        if (!reset && !s.hasMore) return
        val min = if (reset) 0 else s.nextMin
        runWhenOnline {
            if (reset) {
                repo.ownerJobCounts(headers()).isRequestCallSuspendSuccess(
                    success = { r -> _uiState.update { it.copy(counts = r?.data) } },
                    failure = { _, _, m -> fail(m) }
                )
            }
            val body = JSONObject().put("tab", s.tab).put("sort", if (s.newestFirst) "newest" else "oldest")
                .put("min", min).put("max", min + PAGE)
            repo.ownerJobList(headers(), body).isRequestCallSuspendSuccess(
                success = { r ->
                    val page = r?.data.orEmpty()
                    _uiState.update {
                        if (it.tab != s.tab) return@update it
                        val known = if (reset) emptyList() else it.jobs
                        it.copy(
                            jobs = known + page.filter { j -> known.none { k -> k.booking_id == j.booking_id } },
                            total = r?.pagination?.total ?: (known.size + page.size),
                            loaded = true,
                            hasMore = r?.pagination?.has_more == true,
                            nextMin = r?.pagination?.max ?: (min + PAGE)
                        )
                    }
                },
                failure = { _, _, m ->
                    _uiState.update { it.copy(loaded = true) }
                    fail(m)
                }
            )
        }
    }

    /** The same accept / decline calls as the buzzer popup. A 409 shows its message and refreshes. */
    private fun respond(bookingId: String, accept: Boolean) {
        runWhenOnline {
            setIsLoading(true)
            val body = JSONObject().put("booking_id", bookingId)
            repo.ownerRequestApi(headers(), if (accept) "accept" else "decline", body).isRequestCallSuspendSuccess(
                success = { r -> if (accept) _uiState.update { it.copy(accepted = r?.data) } },
                failure = { _, _, m -> fail(m) }
            )
            setIsLoading(false)
            load(reset = true)
        }
    }
}

// ---------------------------------------------------------------------------------------------
// One job
// ---------------------------------------------------------------------------------------------

@HiltViewModel
class OwnerJobDetailViewModel @Inject constructor(
    var app: Application,
    var repo: Repository,
    var sm: SessionManager
) : BaseViewModel(app) {

    private val _uiState = MutableStateFlow(OwnerJobDetailState())
    val uiState: StateFlow<OwnerJobDetailState> = _uiState.asStateFlow()

    private var bookingId = ""

    private fun headers() = AppMethods.getToken(sm)

    private fun body() = JSONObject().put("booking_id", bookingId)

    private fun fail(message: String?) =
        showError(message?.takeIf { it.isNotBlank() } ?: string(R.string.crane_msg_generic_error), autoHideMs = 2500L)

    fun onIntent(intent: OwnerJobDetailIntent) {
        when (intent) {
            is OwnerJobDetailIntent.Load -> {
                bookingId = intent.bookingId
                runWhenOnline {
                    setIsLoading(true)
                    fetchJob(silent = false)
                    setIsLoading(false)
                }
            }
            OwnerJobDetailIntent.Poll -> viewModelScope.launch { fetchJob(silent = true) }
            OwnerJobDetailIntent.PollTrack -> viewModelScope.launch {
                repo.ownerJobTrack(headers(), body()).isRequestCallSuspendSuccess(
                    success = { r -> r?.data?.let { t -> _uiState.update { it.copy(track = t) } } }
                )
            }
            OwnerJobDetailIntent.LoadEvidence -> runWhenOnline {
                setIsLoading(true)
                repo.ownerJobEvidence(headers(), body()).isRequestCallSuspendSuccess(
                    success = { r -> _uiState.update { it.copy(evidence = r?.data, evidenceLoaded = true) } },
                    failure = { _, _, m ->
                        _uiState.update { it.copy(evidenceLoaded = true) }
                        fail(m)
                    }
                )
                setIsLoading(false)
            }
            OwnerJobDetailIntent.LoadOperators -> runWhenOnline {
                setIsLoading(true)
                repo.ownerOperatorsApi(headers(), JSONObject()).isRequestCallSuspendSuccess(
                    success = { r -> _uiState.update { it.copy(operators = r?.data.orEmpty(), operatorsLoaded = true) } },
                    failure = { _, _, m ->
                        _uiState.update { it.copy(operatorsLoaded = true) }
                        fail(m)
                    }
                )
                setIsLoading(false)
            }
            is OwnerJobDetailIntent.AssignOperator -> runWhenOnline {
                setIsLoading(true)
                repo.ownerAssignOperator(headers(), body().put("operator_id", intent.operatorId)).isRequestCallSuspendSuccess(
                    success = { showSuccess(string(R.string.oj_msg_operator_assigned), autoHideMs = 2000L) },
                    failure = { _, _, m -> fail(m) }
                )
                fetchJob(silent = false)
                setIsLoading(false)
            }
        }
    }

    private suspend fun fetchJob(silent: Boolean) {
        repo.ownerJobGet(headers(), body()).isRequestCallSuspendSuccess(
            success = { r -> _uiState.update { it.copy(job = r?.data, loaded = true) } },
            failure = { _, _, m ->
                _uiState.update { it.copy(loaded = true) }
                if (!silent) fail(m)
            }
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Earnings tab
// ---------------------------------------------------------------------------------------------

@HiltViewModel
class OwnerEarningsViewModel @Inject constructor(
    var app: Application,
    var repo: Repository,
    var sm: SessionManager
) : BaseViewModel(app) {

    private val _uiState = MutableStateFlow(OwnerEarningsState())
    val uiState: StateFlow<OwnerEarningsState> = _uiState.asStateFlow()

    private fun headers() = AppMethods.getToken(sm)

    private fun fail(message: String?) =
        showError(message?.takeIf { it.isNotBlank() } ?: string(R.string.crane_msg_generic_error), autoHideMs = 2500L)

    fun onIntent(intent: OwnerEarningsIntent) {
        when (intent) {
            OwnerEarningsIntent.Load -> loadAll()
            is OwnerEarningsIntent.SelectPeriod -> {
                _uiState.update { it.copy(period = intent.period) }
                runWhenOnline { fetchSummary() }
            }
            is OwnerEarningsIntent.SelectRange -> {
                _uiState.update { it.copy(range = intent.range) }
                runWhenOnline { fetchTrend() }
            }
            OwnerEarningsIntent.LoadMoreJobs -> runWhenOnline { fetchJobs(reset = false) }
        }
    }

    private fun loadAll() {
        runWhenOnline {
            fetchSummary()
            fetchTrend()
            fetchJobs(reset = true)
            _uiState.update { it.copy(loaded = true) }
        }
    }

    private suspend fun fetchSummary() {
        repo.ownerEarningsSummary(headers(), JSONObject().put("period", _uiState.value.period)).isRequestCallSuspendSuccess(
            success = { r -> _uiState.update { it.copy(summary = r?.data) } },
            failure = { _, _, m -> fail(m) }
        )
    }

    private suspend fun fetchTrend() {
        repo.ownerEarningsTrend(headers(), JSONObject().put("range", _uiState.value.range)).isRequestCallSuspendSuccess(
            success = { r -> _uiState.update { it.copy(trend = r?.data) } },
            failure = { _, _, m -> fail(m) }
        )
    }

    private suspend fun fetchJobs(reset: Boolean) {
        val s = _uiState.value
        if (!reset && !s.jobsHasMore) return
        val min = if (reset) 0 else s.jobsNextMin
        repo.ownerEarningJobs(headers(), JSONObject().put("min", min).put("max", min + PAGE)).isRequestCallSuspendSuccess(
            success = { r ->
                val page = r?.data.orEmpty()
                _uiState.update {
                    val known = if (reset) emptyList() else it.jobs
                    it.copy(
                        jobs = known + page.filter { j -> known.none { k -> k.booking_id == j.booking_id } },
                        jobsHasMore = r?.pagination?.has_more == true,
                        jobsNextMin = r?.pagination?.max ?: (min + PAGE)
                    )
                }
            },
            failure = { _, _, m -> fail(m) }
        )
    }
}
