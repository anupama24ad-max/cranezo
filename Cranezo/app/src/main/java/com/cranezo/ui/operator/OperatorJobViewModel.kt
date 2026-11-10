package com.cranezo.ui.operator

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import com.cranezo.R
import com.cranezo.api.isRequestCallSuspendSuccess
import com.cranezo.core.BaseViewModel
import com.cranezo.repository.Repository
import com.cranezo.ui.components.SelectOption
import com.cranezo.ui.operator.model.NextAction
import com.cranezo.ui.operator.model.OperatorJob
import com.cranezo.ui.operator.model.OperatorJobEffect
import com.cranezo.ui.operator.model.OperatorJobIntent
import com.cranezo.ui.operator.model.OperatorJobState
import com.cranezo.ui.operator.model.SitePhoto
import com.cranezo.utils.AppMethods
import com.cranezo.utils.AppStrings
import com.cranezo.utils.SessionManager
import com.cranezo.utils.fetchCurrentLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * One job from "New Job Request" to "Job Completed". Every call answers with the whole job, and the
 * screen to show is decided by the job's `next_action`, never by this class.
 */
@HiltViewModel
class OperatorJobViewModel @Inject constructor(
    var app: Application,
    var repo: Repository,
    var sm: SessionManager
) : BaseViewModel(app) {

    private val _uiState = MutableStateFlow(OperatorJobState())
    val uiState: StateFlow<OperatorJobState> = _uiState.asStateFlow()

    private val _effect = Channel<OperatorJobEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var bookingId: String = ""
    private var locationJob: Job? = null

    private fun headers() = AppMethods.getToken(sm)

    private fun body() = JSONObject().put("booking_id", bookingId)

    private fun fail(message: String?) =
        showError(message?.takeIf { it.isNotBlank() } ?: string(R.string.crane_msg_generic_error), autoHideMs = 2500L)

    fun onIntent(intent: OperatorJobIntent) {
        when (intent) {
            is OperatorJobIntent.Load -> {
                bookingId = intent.bookingId
                refresh(showLoading = true)
            }
            OperatorJobIntent.Poll -> refresh(showLoading = false)

            OperatorJobIntent.Accept -> act("jobs/accept")
            is OperatorJobIntent.Decline -> decline(intent.reason)

            // the journey starts from where the operator (and the crane) is now
            OperatorJobIntent.StartJourney -> viewModelScope.launch {
                val fix = if (ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fetchCurrentLocation(app)
                } else null
                val b = body()
                fix?.let {
                    b.put("latitude", it.latitude).put("longitude", it.longitude)
                    _uiState.update { s -> s.copy(myLatitude = it.latitude, myLongitude = it.longitude) }
                }
                act("jobs/start-journey", b)
            }
            // the server checks the phone is at the site before telling the customer
            OperatorJobIntent.Arrived -> viewModelScope.launch {
                val fix = if (ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fetchCurrentLocation(app)
                } else null
                val b = body()
                fix?.let { b.put("latitude", it.latitude).put("longitude", it.longitude) }
                act("jobs/arrived", b)
            }

            is OperatorJobIntent.LoadRoute -> viewModelScope.launch {
                val points = com.cranezo.utils.loadRoutePoints(repo, headers(), intent.fromLat, intent.fromLng, intent.toLat, intent.toLng)
                if (points.isNotEmpty()) _uiState.update { it.copy(route = points) }
            }

            is OperatorJobIntent.AddSitePhoto -> addPhoto(intent.uri, site = true)
            OperatorJobIntent.RemoveSitePhoto -> _uiState.update { it.copy(sitePhotos = emptyList()) }
            OperatorJobIntent.SaveSitePhotos -> saveSitePhotos()

            OperatorJobIntent.LoadChecklist -> loadChecklist()
            is OperatorJobIntent.TickChecklist -> tick(intent.key, intent.done)

            OperatorJobIntent.ConfirmArrival -> act("jobs/confirm-arrival")
            OperatorJobIntent.StartJob -> act("jobs/start")
            is OperatorJobIntent.ToggleWaiting -> toggleWaiting(intent.start)

            is OperatorJobIntent.AddFinalPhoto -> addPhoto(intent.uri, site = false)
            is OperatorJobIntent.RemoveFinalPhoto -> _uiState.update {
                it.copy(finalPhotos = it.finalPhotos.filterIndexed { i, _ -> i != intent.index })
            }
            is OperatorJobIntent.Finish -> finish(intent.notes)

            OperatorJobIntent.RequestConfirmation -> requestConfirmation()
            is OperatorJobIntent.ConfirmCompletion -> act("jobs/confirm-completion", body().put("otp", intent.otp))

            OperatorJobIntent.LoadBreakdownTypes -> loadBreakdownTypes()
            is OperatorJobIntent.ReportBreakdown -> report("jobs/breakdown", intent.description, intent.photoUri, intent.type)
            is OperatorJobIntent.ReportIssue -> report("jobs/issue", intent.description, intent.photoUri, null)
            OperatorJobIntent.ReportEmergency -> report("jobs/emergency", null, null, null)
            is OperatorJobIntent.RespondExtension -> act(
                "jobs/extension/respond",
                body().put("action", if (intent.approve) "approve" else "reject").also { if (intent.reason.isNotBlank()) it.put("reason", intent.reason) }
            )
        }
    }

    // ---------------------------------------------------------------------------------------
    // The job
    // ---------------------------------------------------------------------------------------

    /** Every answer replaces the local copy. Live location is shared only while the crane is on the road. */
    private fun setJob(job: OperatorJob) {
        _uiState.update {
            it.copy(
                job = job,
                loaded = true,
                // What was already saved on the server is the starting point of the photo sets
                sitePhotos = if (it.sitePhotos.isEmpty()) job.site_photos.orEmpty() else it.sitePhotos,
                finalPhotos = if (it.finalPhotos.isEmpty()) job.final_photos.orEmpty() else it.finalPhotos
            )
        }
        if (job.stage == "en_route") startLocationSharing() else stopLocationSharing()
        if (job.stage == "closed") viewModelScope.launch { _effect.send(OperatorJobEffect.Close) }
    }

    /** POST /operator/jobs/get. [showLoading] false for the silent refreshes. */
    private fun refresh(showLoading: Boolean) {
        if (bookingId.isEmpty()) return
        if (!showLoading) {
            viewModelScope.launch {
                repo.opJob(headers(), "jobs/get", body()).isRequestCallSuspendSuccess(success = { r -> r?.data?.let(::setJob) })
            }
            return
        }
        runWhenOnline {
            setIsLoading(true)
            repo.opJob(headers(), "jobs/get", body()).isRequestCallSuspendSuccess(
                success = { r -> r?.data?.let(::setJob) },
                failure = { _, _, m ->
                    _uiState.update { it.copy(loaded = true) }
                    fail(m)
                }
            )
            setIsLoading(false)
        }
    }

    /** A step that answers with the job. A 409 means the server moved on, so the job is fetched again. */
    private fun act(path: String, requestBody: JSONObject = body(), onDone: () -> Unit = {}) {
        runWhenOnline {
            setIsLoading(true)
            var ok = false
            repo.opJob(headers(), path, requestBody).isRequestCallSuspendSuccess(
                success = { r ->
                    r?.data?.let(::setJob)
                    ok = true
                },
                failure = { _, _, m ->
                    fail(m)
                    repo.opJob(headers(), "jobs/get", body()).isRequestCallSuspendSuccess(success = { r -> r?.data?.let(::setJob) })
                }
            )
            setIsLoading(false)
            if (ok) onDone()
        }
    }

    private fun decline(reason: String) {
        runWhenOnline {
            setIsLoading(true)
            repo.opDecline(headers(), body().also { if (reason.isNotBlank()) it.put("reason", reason) })
                .isRequestCallSuspendSuccess(
                    success = { _effect.send(OperatorJobEffect.Close) },
                    failure = { _, _, m -> fail(m) }
                )
            setIsLoading(false)
        }
    }

    // ---------------------------------------------------------------------------------------
    // Live location while on the way
    // ---------------------------------------------------------------------------------------

    /**
     * While on the way: the phone's position every 2 seconds moves the crane on the operator's own map at once,
     * and every 4 seconds it goes to the server (customer and owner follow it). The answer brings the road route
     * from here to the site and the km / minutes left at crane speed.
     */
    private fun startLocationSharing() {
        if (locationJob?.isActive == true) return
        locationJob = viewModelScope.launch {
            while (ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                delay(3_000)
            }
            var lastSent = 0L
            var sending = false
            com.cranezo.utils.locationUpdates(app, 2_000L)
                .catch { }
                .collect { fix ->
                    _uiState.update { it.copy(myLatitude = fix.latitude, myLongitude = fix.longitude) }
                    val now = System.currentTimeMillis()
                    if (!sending && now - lastSent >= 4_000L) {
                        lastSent = now
                        sending = true
                        launch {
                            val b = JSONObject().put("latitude", fix.latitude).put("longitude", fix.longitude).put("booking_id", bookingId)
                            repo.opLocation(headers(), b).isRequestCallSuspendSuccess(success = { r ->
                                r?.data?.let { ack ->
                                    val points = ack.route.orEmpty().mapNotNull { p ->
                                        val la = p.latitude ?: return@mapNotNull null
                                        val lo = p.longitude ?: return@mapNotNull null
                                        com.google.android.gms.maps.model.LatLng(la, lo)
                                    }
                                    _uiState.update {
                                        it.copy(
                                            route = if (points.size > 1) points else it.route,
                                            liveDistanceKm = ack.distance_km ?: it.liveDistanceKm,
                                            liveEtaMinutes = ack.eta_minutes ?: it.liveEtaMinutes
                                        )
                                    }
                                }
                            })
                            sending = false
                        }
                    }
                }
        }
    }

    private fun stopLocationSharing() {
        locationJob?.cancel()
        locationJob = null
    }

    override fun onCleared() {
        stopLocationSharing()
        super.onCleared()
    }

    // ---------------------------------------------------------------------------------------
    // Photos: upload first, then send the returned path (with where and when it was taken)
    // ---------------------------------------------------------------------------------------

    private suspend fun uploadFile(uri: String): String? {
        val part = withContext(Dispatchers.IO) {
            val file = AppMethods.getFileFromUri(app, Uri.parse(uri)) ?: return@withContext null
            val rb = AppMethods.createRequestBody(file, app, Uri.parse(uri)) ?: return@withContext null
            MultipartBody.Part.createFormData(AppStrings.InputData.file, file.name, rb)
        }
        if (part == null) {
            showError(string(R.string.msg_file_unreadable))
            return null
        }
        var path: String? = null
        repo.uploadApi(headers(), part).isRequestCallSuspendSuccess(
            success = { r -> path = r?.data?.file_url },
            failure = { _, _, m -> fail(m) }
        )
        return path
    }

    private fun nowIso(): String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date())

    private fun addPhoto(uri: String, site: Boolean) {
        _uiState.update { it.copy(isUploading = true) }
        viewModelScope.launch {
            val fix = if (ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation(app)
            } else null
            val captured = nowIso()
            runWhenOnline {
                val path = uploadFile(uri)
                _uiState.update { state ->
                    if (path == null) return@update state.copy(isUploading = false)
                    val photo = SitePhoto(photo_url = path, latitude = fix?.latitude, longitude = fix?.longitude, captured_at = captured)
                    if (site) state.copy(isUploading = false, sitePhotos = listOf(photo))
                    else state.copy(isUploading = false, finalPhotos = (state.finalPhotos + photo).take(10))
                }
            }
        }
    }

    private fun photosJson(photos: List<SitePhoto>) = JSONArray().also { array ->
        photos.forEach { p ->
            array.put(
                JSONObject().put("photo_url", p.photo_url)
                    .also { o ->
                        p.latitude?.let { o.put("latitude", it) }
                        p.longitude?.let { o.put("longitude", it) }
                        p.captured_at?.let { o.put("captured_at", it) }
                    }
            )
        }
    }

    /** Replaces the whole set on the server, so the full list is sent. */
    private fun saveSitePhotos() {
        val photos = _uiState.value.sitePhotos
        if (photos.isEmpty()) {
            showError(string(R.string.op_msg_photo_required))
            return
        }
        act("jobs/site-photos", body().put("photos", photosJson(photos)))
    }

    private fun finish(notes: String) {
        val b = body().put("final_photos", photosJson(_uiState.value.finalPhotos))
        if (notes.isNotBlank()) b.put("notes", notes)
        // One step less for the operator: the customer code is sent right after finishing,
        // so the app goes straight to "Enter customer code". If that call fails, the summary screen
        // (next_action = request_customer_confirmation) still offers the button.
        act("jobs/finish", b) { requestConfirmation() }
    }

    // ---------------------------------------------------------------------------------------
    // Checklist, waiting time, OTP
    // ---------------------------------------------------------------------------------------

    private fun loadChecklist() {
        runWhenOnline {
            setIsLoading(true)
            repo.opChecklist(headers(), "jobs/checklist", body()).isRequestCallSuspendSuccess(
                success = { r -> _uiState.update { it.copy(checklist = r?.data) } },
                failure = { _, _, m -> fail(m) }
            )
            setIsLoading(false)
        }
    }

    private fun tick(key: String, done: Boolean) {
        // Shown at once, the server's answer replaces it
        _uiState.update { s ->
            s.copy(checklist = s.checklist?.let { c ->
                c.copy(items = c.items.orEmpty().map { if (it.key == key) it.copy(done = done) else it })
            })
        }
        viewModelScope.launch {
            val b = body().put("items", JSONObject().put(key, done))
            repo.opChecklist(headers(), "jobs/checklist/save", b).isRequestCallSuspendSuccess(
                success = { r ->
                    _uiState.update { it.copy(checklist = r?.data) }
                    // Completing the list moves the job on: the next action is read again
                    if (r?.data?.completed == true) refresh(showLoading = false)
                },
                failure = { _, _, m ->
                    fail(m)
                    loadChecklist()
                }
            )
        }
    }

    private fun toggleWaiting(start: Boolean) {
        viewModelScope.launch {
            val b = body().put("action", if (start) "start" else "stop")
            repo.opWaiting(headers(), b).isRequestCallSuspendSuccess(
                success = { r ->
                    _uiState.update { s -> s.copy(job = s.job?.let { j -> j.copy(timers = r?.data ?: j.timers) }) }
                },
                failure = { _, _, m ->
                    fail(m)
                    refresh(showLoading = false)
                }
            )
        }
    }

    private fun requestConfirmation() {
        runWhenOnline {
            setIsLoading(true)
            repo.opRequestConfirmation(headers(), body()).isRequestCallSuspendSuccess(
                success = { r ->
                    _uiState.update { it.copy(otpSent = true) }
                    r?.data?.message?.let { showSuccess(it, autoHideMs = 2000L) }
                    // The job moves to the OTP step
                    refresh(showLoading = false)
                },
                failure = { _, _, m -> fail(m) }
            )
            setIsLoading(false)
        }
    }

    // ---------------------------------------------------------------------------------------
    // Breakdown, issue, emergency
    // ---------------------------------------------------------------------------------------

    private fun loadBreakdownTypes() {
        if (_uiState.value.breakdownTypes.isNotEmpty()) return
        runWhenOnline {
            repo.dropDownApi(headers(), JSONObject().put(AppStrings.InputData.type, 15)).isRequestCallSuspendSuccess(
                success = { r ->
                    val list = r?.data.orEmpty().mapNotNull { i -> i.id?.let { SelectOption(it, i.dropdownName.orEmpty()) } }
                    _uiState.update { it.copy(breakdownTypes = list) }
                },
                failure = { _, _, m -> fail(m) }
            )
        }
    }

    private fun report(path: String, description: String?, photoUri: String?, type: Int?) {
        viewModelScope.launch {
            val fix = if (ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation(app)
            } else null
            runWhenOnline {
                setIsLoading(true)
                val b = body()
                fix?.let { b.put("latitude", it.latitude).put("longitude", it.longitude) }
                type?.let { b.put("breakdown_type", it) }
                description?.takeIf { it.isNotBlank() }?.let { b.put("description", it) }
                if (photoUri != null) {
                    uploadFile(photoUri)?.let { b.put("photos", JSONArray().put(it)) }
                }
                repo.opIssue(headers(), path, b).isRequestCallSuspendSuccess(
                    success = { r ->
                        showSuccess(r?.data?.message ?: string(R.string.op_msg_report_sent), autoHideMs = 2500L)
                        _effect.send(OperatorJobEffect.ReportSent)
                    },
                    failure = { _, _, m -> fail(m) }
                )
                setIsLoading(false)
            }
        }
    }
}
