package com.cranezo.ui.cranes

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.cranezo.R
import com.cranezo.api.isRequestCallSuspendSuccess
import com.cranezo.core.BaseViewModel
import com.cranezo.interfaces.SnackbarStatus
import com.cranezo.repository.Repository
import com.cranezo.ui.cranes.model.Crane
import com.cranezo.ui.cranes.model.CraneForm
import com.cranezo.ui.cranes.model.CranesEffect
import com.cranezo.ui.cranes.model.CranesIntent
import com.cranezo.ui.cranes.model.CranesUiState
import com.cranezo.ui.cranes.model.DocumentEntry
import com.cranezo.ui.cranes.model.FormMode
import com.cranezo.ui.cranes.model.UploadItem
import com.cranezo.utils.AppMethods
import com.cranezo.utils.AppStrings
import com.cranezo.utils.SessionManager
import com.cranezo.utils.fileUrl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

/**
 * Add Crane, Crane Submitted, Crane Details, Edit Crane and Upload Document share this ViewModel:
 * one activity hosts all of them.
 *
 * APIs (docs: https://cranezo.com/api/docs/user, section "Owner Cranes"):
 * POST /owner/cranes/form-options, POST /owner/cranes, POST /owner/getCranes (details),
 * PUT /owner/cranes/{id}, PUT /owner/cranes/{id}/documents, POST /owner/operators(/add), POST /upload.
 */
@HiltViewModel
class CranesViewModel @Inject constructor(
    var app: Application,
    var repo: Repository,
    var sm: SessionManager
) : BaseViewModel(app) {

    private val TAG = "CranesViewModel"

    private companion object {
        const val MAX_FILE_BYTES = 50L * 1024 * 1024
        // the most the server returns per request (the picker also has a search box)
        const val OPERATOR_PAGE_SIZE = 100

        // "type" of POST /owner/cranes/form-options when only one list is wanted
        const val TYPE_CRANE_TYPES = 6
        const val TYPE_CAPACITIES = 7
        const val TYPE_FUEL_TYPES = 8
        const val TYPE_DOCUMENT_TYPES = 9
    }

    private val _uiState = MutableStateFlow(CranesUiState())
    val uiState: StateFlow<CranesUiState> = _uiState.asStateFlow()

    private val _effect = Channel<CranesEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val nextUploadId = AtomicLong(1)
    private var operatorSearchJob: Job? = null
    private var lastOperatorQuery = ""

    fun onIntent(intent: CranesIntent) {
        when (intent) {
            CranesIntent.LoadOptions -> loadOptions(force = false)
            is CranesIntent.RetryOptionList -> loadOptionList(intent.type)

            CranesIntent.StartAdd -> {
                _uiState.update { it.copy(mode = FormMode.ADD, form = CraneForm(), original = null) }
                loadOptions(force = false)
            }

            is CranesIntent.StartEdit -> startEdit(intent.crane)

            is CranesIntent.CraneTypeChanged -> updateForm { it.copy(craneTypeId = intent.id) }
            is CranesIntent.CapacityChanged -> updateForm { it.copy(capacityId = intent.id) }
            is CranesIntent.FuelTypeChanged -> updateForm { it.copy(fuelTypeId = intent.id) }
            is CranesIntent.ModelChanged -> updateForm { it.copy(modelName = intent.value) }
            is CranesIntent.RegistrationChanged -> updateForm { it.copy(registration = intent.value) }
            // Typing a new location makes the earlier GPS point wrong, so it is dropped
            is CranesIntent.LocationChanged ->
                updateForm { it.copy(location = intent.value, latitude = null, longitude = null) }

            is CranesIntent.GpsLocationFound -> updateForm {
                it.copy(
                    location = intent.location ?: it.location,
                    latitude = intent.latitude,
                    longitude = intent.longitude
                )
            }

            is CranesIntent.HourlyRateChanged -> updateForm { it.copy(hourlyRate = intent.value) }
            is CranesIntent.MinimumHoursChanged -> updateForm { it.copy(minimumHours = intent.value) }
            is CranesIntent.OperatorSelected -> updateForm { it.copy(operator = intent.operator) }
            is CranesIntent.AvailabilityChanged -> updateForm { it.copy(availability = intent.value) }

            is CranesIntent.PhotoPicked -> addPhoto(intent.uri)
            is CranesIntent.PhotoRemoved -> updateForm { f -> f.copy(photos = f.photos.filterNot { it.id == intent.id }) }
            is CranesIntent.PhotoRetry -> _uiState.value.form.photos.firstOrNull { it.id == intent.id }
                ?.let { startUpload(it.id, it.displayUri) }

            is CranesIntent.DocumentPicked -> {
                val item = newUpload(intent.uri)
                updateForm { f ->
                    val old = f.documents[intent.typeId]
                    f.copy(documents = f.documents + (intent.typeId to DocumentEntry(intent.typeId, item, old?.expiry.orEmpty())))
                }
                startUpload(item.id, intent.uri)
            }

            is CranesIntent.DocumentRemoved -> updateForm { it.copy(documents = it.documents - intent.typeId) }
            is CranesIntent.DocumentRetry -> _uiState.value.form.documents[intent.typeId]?.upload
                ?.let { startUpload(it.id, it.displayUri) }

            is CranesIntent.DocumentExpiryChanged -> updateForm { f ->
                val entry = f.documents[intent.typeId] ?: return@updateForm f
                f.copy(documents = f.documents + (intent.typeId to entry.copy(expiry = intent.date)))
            }

            CranesIntent.SubmitCrane -> submitCrane()
            CranesIntent.SaveEdit -> saveEdit()

            is CranesIntent.LoadDetail -> loadDetail(intent.craneId)

            CranesIntent.StartDocEdit -> _uiState.update { it.copy(docEdits = emptyMap()) }
            is CranesIntent.DocEditPicked -> {
                val item = newUpload(intent.uri)
                _uiState.update { s ->
                    val old = s.docEdits[intent.typeId]
                    s.copy(docEdits = s.docEdits + (intent.typeId to DocumentEntry(intent.typeId, item, old?.expiry.orEmpty())))
                }
                startUpload(item.id, intent.uri)
            }

            is CranesIntent.DocEditRetry -> _uiState.value.docEdits[intent.typeId]?.upload
                ?.let { startUpload(it.id, it.displayUri) }

            is CranesIntent.DocEditExpiryChanged -> _uiState.update { s ->
                val entry = s.docEdits[intent.typeId] ?: return@update s
                s.copy(docEdits = s.docEdits + (intent.typeId to entry.copy(expiry = intent.date)))
            }

            CranesIntent.SaveDocuments -> saveDocuments()

            is CranesIntent.SearchOperators -> searchOperators(intent.query)
            is CranesIntent.AddOperator -> addOperator(intent)
        }
    }

    private fun updateForm(change: (CraneForm) -> CraneForm) =
        _uiState.update { it.copy(form = change(it.form)) }

    private fun errorText(message: String?): String =
        message?.takeIf { it.isNotBlank() } ?: string(R.string.crane_msg_generic_error)

    // ---------------------------------------------------------------------------------------
    // Dropdowns: one call, kept for the life of the screen. A single list can be refreshed.
    // ---------------------------------------------------------------------------------------

    private fun loadOptions(force: Boolean) {
        val state = _uiState.value
        if (state.optionsLoading || (!force && state.options != null)) return
        _uiState.update { it.copy(optionsLoading = true) }

        runWhenOnline {
            repo.craneFormOptionsApi(AppMethods.getToken(sm)).isRequestCallSuspendSuccess(
                success = { response ->
                    val data = response?.data
                    _uiState.update {
                        it.copy(
                            options = data,
                            optionsLoading = false,
                            operators = data?.operators.orEmpty()
                        )
                    }
                },
                failure = { _, _, message ->
                    Log.e(TAG, "loadOptions: message --> $message")
                    _uiState.update { it.copy(optionsLoading = false) }
                    showError(errorText(message))
                })
        }
    }

    private fun loadOptionList(type: Int) {
        if (type !in TYPE_CRANE_TYPES..TYPE_DOCUMENT_TYPES) return
        runWhenOnline {
            val body = JSONObject().put("type", type)
            repo.craneFormOptionsByTypeApi(AppMethods.getToken(sm), body).isRequestCallSuspendSuccess(
                success = { response ->
                    val list = response?.data.orEmpty()
                    _uiState.update { s ->
                        val options = s.options ?: com.cranezo.ui.cranes.model.CraneFormOptionsResponse()
                        s.copy(
                            options = when (type) {
                                TYPE_CRANE_TYPES -> options.copy(crane_types = list)
                                TYPE_CAPACITIES -> options.copy(capacities = list)
                                TYPE_FUEL_TYPES -> options.copy(fuel_types = list)
                                else -> options.copy(document_types = list)
                            }
                        )
                    }
                },
                failure = { _, _, message -> showError(errorText(message)) })
        }
    }

    // ---------------------------------------------------------------------------------------
    // Uploads: POST /upload, one file per request, as soon as the user picks the file
    // ---------------------------------------------------------------------------------------

    private fun newUpload(uri: String) = UploadItem(nextUploadId.getAndIncrement(), uri, isUploading = true)

    private fun addPhoto(uri: String) {
        val item = newUpload(uri)
        updateForm { it.copy(photos = it.photos + item) }
        startUpload(item.id, uri)
    }

    /** Applies [change] to the upload with [id], wherever it is (photos, documents, replaced documents). */
    private fun updateUpload(id: Long, change: (UploadItem) -> UploadItem) {
        fun DocumentEntry.touched(): DocumentEntry =
            if (upload != null && upload.id == id) copy(upload = change(upload)) else this
        _uiState.update { s ->
            s.copy(
                form = s.form.copy(
                    photos = s.form.photos.map { if (it.id == id) change(it) else it },
                    documents = s.form.documents.mapValues { it.value.touched() }
                ),
                docEdits = s.docEdits.mapValues { it.value.touched() }
            )
        }
    }

    private fun startUpload(id: Long, uri: String) {
        updateUpload(id) { it.copy(isUploading = true, failed = false) }
        viewModelScope.launch {
            val part = withContext(Dispatchers.IO) { buildFilePart(Uri.parse(uri)) }
            if (part == null) {
                showError(string(R.string.msg_file_unreadable))
                updateUpload(id) { it.copy(isUploading = false, failed = true) }
                return@launch
            }
            if (part.body.contentLength() > MAX_FILE_BYTES) {
                showError(string(R.string.crane_msg_file_too_large))
                updateUpload(id) { it.copy(isUploading = false, failed = true) }
                return@launch
            }

            runWhenOnline {
                var path: String? = null
                repo.uploadApi(AppMethods.getToken(sm), part).isRequestCallSuspendSuccess(
                    success = { response ->
                        path = response?.data?.file_url
                        if (path == null) showError(string(R.string.msg_upload_failed))
                    },
                    failure = { _, _, message ->
                        Log.e(TAG, "upload: message --> $message")
                        showError(errorText(message))
                    })
                // A failed upload stays in the list so the owner can retry or remove it
                val result = path
                updateUpload(id) {
                    if (result != null) it.copy(path = result, isUploading = false, failed = false)
                    else it.copy(isUploading = false, failed = true)
                }
            }
        }
    }

    private fun buildFilePart(uri: Uri): MultipartBody.Part? {
        val file = AppMethods.getFileFromUri(app, uri) ?: return null
        val body = AppMethods.createRequestBody(file, app, uri) ?: return null
        return MultipartBody.Part.createFormData(AppStrings.InputData.file, file.name, body)
    }

    // ---------------------------------------------------------------------------------------
    // Add crane
    // ---------------------------------------------------------------------------------------

    /** Format checks that mirror the server. Returns the message to show, or null when all is fine. */
    private fun validate(state: CranesUiState): String? {
        val f = state.form
        val rate = f.hourlyRate.trim().toDoubleOrNull()
        val minHours = f.minimumHours.trim()
        return when {
            f.craneTypeId == null -> string(R.string.crane_msg_select_type)
            f.capacityId == null -> string(R.string.crane_msg_select_capacity)
            f.registration.isBlank() -> string(R.string.crane_msg_enter_registration)
            f.photos.isEmpty() -> string(R.string.crane_msg_add_photo)
            state.mode == FormMode.ADD && state.documentTypes.any { it.required != false && f.documents[it.id]?.upload?.path == null } ->
                string(R.string.crane_msg_upload_documents)

            f.location.isBlank() -> string(R.string.crane_msg_enter_location)
            rate == null || rate <= 0 -> string(R.string.crane_msg_rate_invalid)
            minHours.isNotEmpty() && (minHours.toIntOrNull() ?: 0) < 1 -> string(R.string.crane_msg_min_hours_invalid)
            else -> null
        }
    }

    private fun submitCrane() {
        val state = _uiState.value
        if (state.isSubmitting) return
        if (state.isUploading) {
            showBanner(string(R.string.msg_files_uploading), SnackbarStatus.informative)
            return
        }
        validate(state)?.let {
            showError(it)
            return
        }
        // A photo that failed to upload has no path: the owner has to retry or remove it
        if (state.form.photos.any { it.path == null }) {
            showError(string(R.string.crane_msg_fix_uploads))
            return
        }

        val body = JSONObject().apply {
            val f = state.form
            put("crane_type", f.craneTypeId)
            put("capacity", f.capacityId)
            putIfPresent("fuel_type", f.fuelTypeId)
            putIfPresent("model_name", f.modelName)
            put("registration_number", f.registration.trim())
            put("location", f.location.trim())
            if (f.latitude != null && f.longitude != null) {
                put("latitude", f.latitude)
                put("longitude", f.longitude)
            }
            put("hourly_rate", f.hourlyRate.trim().toDouble())
            f.minimumHours.trim().toIntOrNull()?.let { put("minimum_hours", it) }
            putIfPresent("operator_id", f.operator?.id)
            put("availability_status", f.availability)
            put("photos", photoPaths(f))
            put("documents", documentsArray(f.documents.values))
        }

        _uiState.update { it.copy(isSubmitting = true) }
        runWhenOnline {
            repo.addCraneApi(AppMethods.getToken(sm), body).isRequestCallSuspendSuccess(
                success = { response ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submitted = response?.data,
                            submittedMessage = response?.message
                        )
                    }
                    _effect.send(CranesEffect.NavigateToSubmitted)
                },
                // The form stays filled so the owner can fix it and try again
                failure = { _, _, message ->
                    Log.e(TAG, "addCrane: message --> $message")
                    _uiState.update { it.copy(isSubmitting = false) }
                    showError(errorText(message))
                })
        }
    }

    private fun photoPaths(form: CraneForm) = JSONArray(form.photos.mapNotNull { it.path })

    private fun documentsArray(entries: Collection<DocumentEntry>) = JSONArray().apply {
        entries.forEach { entry ->
            val path = entry.upload?.path ?: return@forEach
            put(JSONObject().apply {
                put("document_type", entry.typeId)
                put("file_url", path)
                putIfPresent("expiry_date", entry.expiry)
            })
        }
    }

    /** Optional fields are left out instead of being sent as null or "". */
    private fun JSONObject.putIfPresent(key: String, value: Any?) {
        when (value) {
            null -> Unit
            is String -> if (value.isNotBlank()) put(key, value.trim())
            else -> put(key, value)
        }
    }

    // ---------------------------------------------------------------------------------------
    // Edit crane: only the changed fields are sent
    // ---------------------------------------------------------------------------------------

    private fun startEdit(crane: Crane) {
        _uiState.update {
            it.copy(
                mode = FormMode.EDIT,
                original = crane,
                form = CraneForm(
                    craneTypeId = crane.crane_type,
                    capacityId = crane.capacity,
                    fuelTypeId = crane.fuel_type,
                    modelName = crane.model_name.orEmpty(),
                    registration = crane.registration_number.orEmpty(),
                    location = crane.location.orEmpty(),
                    latitude = crane.latitude,
                    longitude = crane.longitude,
                    hourlyRate = crane.hourly_rate?.let { r -> if (r % 1.0 == 0.0) r.toLong().toString() else r.toString() }.orEmpty(),
                    minimumHours = crane.minimum_hours?.toString().orEmpty(),
                    operator = crane.operator?.let { op ->
                        com.cranezo.ui.cranes.model.Operator(
                            id = op.id, member_id = op.member_id, full_name = op.full_name,
                            mobile_number = op.mobile_number, country_code = op.country_code,
                            profile_image = op.profile_image
                        )
                    },
                    availability = crane.availability_status ?: 0,
                    // Cover photo first, as the API sends primary_photo
                    photos = crane.photoUrls.map { path ->
                        UploadItem(nextUploadId.getAndIncrement(), fileUrl(path).orEmpty(), path = path)
                    }
                )
            )
        }
        loadOptions(force = false)
    }

    /** Builds the PUT body, or null when nothing changed. */
    private fun buildEditBody(state: CranesUiState): JSONObject? {
        val f = state.form
        val o = state.original ?: return null
        val body = JSONObject()

        if (f.craneTypeId != o.crane_type) body.put("crane_type", f.craneTypeId)
        if (f.capacityId != o.capacity) body.put("capacity", f.capacityId)
        if (f.fuelTypeId != null && f.fuelTypeId != o.fuel_type) body.put("fuel_type", f.fuelTypeId)
        if (f.modelName.isNotBlank() && f.modelName.trim() != o.model_name.orEmpty()) body.putIfPresent("model_name", f.modelName)
        if (!f.registration.trim().equals(o.registration_number.orEmpty(), ignoreCase = true)) {
            body.put("registration_number", f.registration.trim())
        }
        if (f.location.trim() != o.location.orEmpty()) {
            body.put("location", f.location.trim())
        }
        if (f.latitude != null && f.longitude != null &&
            (f.latitude != o.latitude || f.longitude != o.longitude)
        ) {
            body.put("latitude", f.latitude)
            body.put("longitude", f.longitude)
        }
        val rate = f.hourlyRate.trim().toDouble()
        if (rate != o.hourly_rate) body.put("hourly_rate", rate)
        f.minimumHours.trim().toIntOrNull()?.let { if (it != o.minimum_hours) body.put("minimum_hours", it) }
        // The API has no way to clear the operator, so only a chosen one is sent
        f.operator?.id?.let { if (it != o.operator?.id) body.put("operator_id", it) }
        if (f.availability != o.availability_status) body.put("availability_status", f.availability)
        // photos replaces the whole list: send the full final list
        if (f.photos.mapNotNull { it.path } != o.photoUrls) body.put("photos", photoPaths(f))

        return if (body.length() == 0) null else body
    }

    private fun saveEdit() {
        val state = _uiState.value
        val craneId = state.original?.id ?: return
        if (state.isSubmitting) return
        if (state.isUploading) {
            showBanner(string(R.string.msg_files_uploading), SnackbarStatus.informative)
            return
        }
        validate(state)?.let {
            showError(it)
            return
        }
        if (state.form.photos.any { it.path == null }) {
            showError(string(R.string.crane_msg_fix_uploads))
            return
        }
        val body = buildEditBody(state)
        if (body == null) {
            showBanner(string(R.string.crane_msg_no_changes), SnackbarStatus.informative)
            return
        }

        _uiState.update { it.copy(isSubmitting = true) }
        runWhenOnline {
            repo.editCraneApi(AppMethods.getToken(sm), craneId, body).isRequestCallSuspendSuccess(
                success = { response ->
                    _uiState.update { it.copy(isSubmitting = false, detail = response?.data ?: it.detail) }
                    showSuccess(response?.message)
                    _effect.send(CranesEffect.NavigateBackToDetails)
                },
                failure = { _, _, message ->
                    Log.e(TAG, "editCrane: message --> $message")
                    _uiState.update { it.copy(isSubmitting = false) }
                    showError(errorText(message))
                })
        }
    }

    // ---------------------------------------------------------------------------------------
    // Crane details: POST /owner/getCranes { crane_id }
    // ---------------------------------------------------------------------------------------

    private fun loadDetail(craneId: String) {
        _uiState.update {
            // Keep a crane that is already on screen (e.g. just edited) while it refreshes
            it.copy(detailLoading = true, detailError = null, detail = it.detail?.takeIf { d -> d.id == craneId })
        }
        runWhenOnline {
            val body = JSONObject().put("crane_id", craneId)
            repo.getCraneDetailsApi(AppMethods.getToken(sm), body).isRequestCallSuspendSuccess(
                success = { response ->
                    _uiState.update { it.copy(detail = response?.data, detailLoading = false) }
                },
                failure = { _, _, message ->
                    Log.e(TAG, "loadDetail: message --> $message")
                    _uiState.update { it.copy(detailLoading = false, detailError = errorText(message)) }
                })
        }
    }

    // ---------------------------------------------------------------------------------------
    // Upload Document: PUT /owner/cranes/{id}/documents, only the replaced documents
    // ---------------------------------------------------------------------------------------

    private fun saveDocuments() {
        val state = _uiState.value
        val craneId = state.detail?.id ?: return
        if (state.isSavingDocs) return
        if (state.isUploadingDocs) {
            showBanner(string(R.string.msg_files_uploading), SnackbarStatus.informative)
            return
        }
        val ready = state.docEdits.values.filter { it.upload?.path != null }
        if (ready.isEmpty() || ready.size != state.docEdits.size) {
            showError(string(R.string.crane_msg_choose_document))
            return
        }

        val body = JSONObject().put("documents", documentsArray(ready))
        _uiState.update { it.copy(isSavingDocs = true) }
        runWhenOnline {
            repo.craneDocumentsApi(AppMethods.getToken(sm), craneId, body).isRequestCallSuspendSuccess(
                success = { response ->
                    _uiState.update {
                        it.copy(isSavingDocs = false, docEdits = emptyMap(), detail = response?.data ?: it.detail)
                    }
                    // The crane is back in review: show the text the API gives for it
                    showSuccess(response?.data?.status_message ?: response?.message, autoHideMs = 2500L)
                    _effect.send(CranesEffect.NavigateBackToDetails)
                },
                failure = { _, _, message ->
                    Log.e(TAG, "saveDocuments: message --> $message")
                    _uiState.update { it.copy(isSavingDocs = false) }
                    showError(errorText(message))
                })
        }
    }

    // ---------------------------------------------------------------------------------------
    // Operators
    // ---------------------------------------------------------------------------------------

    private fun searchOperators(query: String) {
        operatorSearchJob?.cancel()
        operatorSearchJob = viewModelScope.launch {
            delay(350)
            fetchOperators(query.trim())
        }
    }

    private fun fetchOperators(query: String) {
        lastOperatorQuery = query
        _uiState.update { it.copy(operatorsLoading = true) }
        runWhenOnline {
            val body = JSONObject().put("min", 0).put("max", OPERATOR_PAGE_SIZE)
            if (query.isNotEmpty()) body.put("search", query)
            repo.ownerOperatorsApi(AppMethods.getToken(sm), body).isRequestCallSuspendSuccess(
                success = { response ->
                    // A newer search may have started while this one was running
                    if (query == lastOperatorQuery) {
                        _uiState.update { it.copy(operators = response?.data.orEmpty(), operatorsLoading = false) }
                    }
                },
                failure = { _, _, message ->
                    _uiState.update { it.copy(operatorsLoading = false) }
                    showError(errorText(message))
                })
        }
    }

    private fun addOperator(intent: CranesIntent.AddOperator) {
        if (_uiState.value.isAddingOperator) return
        val name = intent.fullName.trim()
        val mobile = intent.mobile.trim()
        val email = intent.email.trim()
        when {
            name.isEmpty() -> return showError(string(R.string.crane_msg_operator_name))
            mobile.length !in 6..15 || !mobile.all { it.isDigit() } ->
                return showError(string(R.string.crane_msg_operator_mobile))

            email.isNotEmpty() && !AppMethods.isValidEmail(email) ->
                return showError(string(R.string.msg_invalid_email))
        }

        val body = JSONObject()
            .put("full_name", name)
            .put("mobile_number", mobile)
            .put("country_code", sm.getData(AppStrings.SessionValues.countryCode, AppStrings.Constants.defaultCountryCode))
        body.putIfPresent("user_email", email)

        _uiState.update { it.copy(isAddingOperator = true) }
        runWhenOnline {
            repo.addOperatorApi(AppMethods.getToken(sm), body).isRequestCallSuspendSuccess(
                success = { response ->
                    val operator = response?.data
                    _uiState.update { s ->
                        s.copy(
                            isAddingOperator = false,
                            // Show the new operator right away, then refresh the list from the server
                            operators = listOfNotNull(operator) + s.operators.filterNot { it.id == operator?.id },
                            form = s.form.copy(operator = operator ?: s.form.operator)
                        )
                    }
                    showSuccess(response?.message)
                    fetchOperators("")
                    _effect.send(CranesEffect.OperatorAdded)
                },
                // 409: the mobile number is already taken, the message says so
                failure = { _, _, message ->
                    _uiState.update { it.copy(isAddingOperator = false) }
                    showError(errorText(message))
                })
        }
    }

    /** Called when the operator picker opens, so the list is fresh and not filtered by an old search. */
    fun refreshOperators() = fetchOperators("")

    /** Clears the Crane Submitted data once the screen has been left. */
    fun clearSubmitted() = _uiState.update { it.copy(submitted = null, submittedMessage = null) }
}
