package com.cranezo.ui.myprofile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.cranezo.R
import com.cranezo.api.isRequestCallSuspendSuccess
import com.cranezo.core.BaseViewModel
import com.cranezo.repository.Repository
import com.cranezo.ui.chooserole.UserRole
import com.cranezo.ui.components.SelectOption
import com.cranezo.ui.myprofile.model.MyProfileEffect
import com.cranezo.ui.myprofile.model.MyProfileIntent
import com.cranezo.ui.myprofile.model.MyProfileUiState
import com.cranezo.ui.myprofile.model.NamedItem
import com.cranezo.ui.myprofile.model.NotificationGroup
import com.cranezo.ui.myprofile.model.PKey
import com.cranezo.ui.myprofile.model.ProfileData
import com.cranezo.ui.profile.model.ProfileValidation
import com.cranezo.ui.theme.AppThemeMode
import com.cranezo.ui.theme.AppThemeState
import com.cranezo.utils.AppMethods
import com.cranezo.utils.AppStrings
import com.cranezo.utils.SessionManager
import com.cranezo.utils.fileUrl
import com.google.gson.Gson
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
import okhttp3.MultipartBody
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class MyProfileViewModel @Inject constructor(
    var app: Application,
    var repo: Repository,
    var sm: SessionManager,
    private val store: ProfileStore
) : BaseViewModel(app) {

    private val role = runCatching { UserRole.valueOf(sm.getUserRole()) }
        .getOrDefault(UserRole.CRANE_OWNER)

    private val _uiState = MutableStateFlow(MyProfileUiState(profile = store.profile.value))
    val uiState: StateFlow<MyProfileUiState> = _uiState.asStateFlow()

    private val _effect = Channel<MyProfileEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // What the form looked like when it was filled from the server: only the differences are sent
    private var initialForm: Map<String, String> = emptyMap()
    private var initialIds: Map<String, Int> = emptyMap()
    private var initialIsBusiness = false

    private val gson = Gson()

    init {
        viewModelScope.launch {
            store.profile.collect { profile -> _uiState.update { it.copy(profile = profile) } }
        }
    }

    fun onIntent(intent: MyProfileIntent) {
        when (intent) {
            MyProfileIntent.LoadPersonalInfo -> loadPersonalInfo()
            is MyProfileIntent.FieldChanged -> changeField(intent.key, intent.value)
            is MyProfileIntent.OptionSelected -> _uiState.update {
                it.copy(
                    form = it.form + (intent.key to intent.option.name),
                    formIds = it.formIds + (intent.key to intent.option.id)
                )
            }
            is MyProfileIntent.UserTypeChanged -> changeUserType(intent.isBusiness)
            is MyProfileIntent.PhotoSelected -> changePhoto(intent.uri)
            MyProfileIntent.SaveProfile -> saveProfile()

            MyProfileIntent.LoadDocuments -> runWhenOnline {
                setIsLoading(true)
                store.refresh(repo, sm, app) { fail(it) }
                setIsLoading(false)
            }
            is MyProfileIntent.DocumentSelected -> replaceDocument(intent.field, intent.uri)

            MyProfileIntent.LoadLanguages -> loadLanguages()
            is MyProfileIntent.SelectLanguage -> _uiState.update { it.copy(selectedLanguageId = intent.id) }
            MyProfileIntent.SaveLanguage -> saveLanguage()

            MyProfileIntent.LoadAppearance -> loadAppearance()
            is MyProfileIntent.SelectAppearance -> {
                _uiState.update { it.copy(selectedAppearance = intent.id) }
                AppThemeState.preview(AppThemeMode.fromKey(intent.id))
            }
            MyProfileIntent.SaveAppearance -> saveAppearance()
            MyProfileIntent.CancelAppearance -> AppThemeState.preview(AppThemeMode.fromKey(sm.getAppTheme()))

            MyProfileIntent.LoadNotifications -> loadNotifications()
            is MyProfileIntent.ToggleNotification -> _uiState.update {
                it.copy(notificationPrefs = it.notificationPrefs + (intent.key to intent.enabled))
            }
            MyProfileIntent.SaveNotifications -> saveNotifications()

            is MyProfileIntent.LoadLegal -> loadLegal(intent.type)
            is MyProfileIntent.LoadAssignedCrane -> loadAssignedCrane(intent.craneId)
        }
    }

    private fun fail(message: String?) {
        showError(message?.takeIf { it.isNotBlank() } ?: string(R.string.crane_msg_generic_error))
    }

    private fun headers() = AppMethods.getToken(sm)

    // ---------------------------------------------------------------------------------------
    // Personal Information
    // ---------------------------------------------------------------------------------------

    private fun loadPersonalInfo() {
        runWhenOnline {
            setIsLoading(true)
            store.refresh(repo, sm, app) { fail(it) }
            loadOptions(dropdownTypes())
            initForm(store.profile.value)
            setIsLoading(false)
        }
    }

    private fun dropdownTypes(): List<Int> = when (role) {
        UserRole.CUSTOMER -> listOf(11, 14, 12, 13)
        UserRole.OPERATOR -> listOf(3, 10)
        UserRole.CRANE_OWNER -> emptyList()
    }

    /** POST /dropdowns { type }, one after another (parallel calls cancel each other's network check). */
    private suspend fun loadOptions(types: List<Int>) {
        types.filter { _uiState.value.options[it].isNullOrEmpty() }.forEach { type ->
            repo.dropDownApi(headers(), JSONObject().put(AppStrings.InputData.type, type))
                .isRequestCallSuspendSuccess(
                    success = { response ->
                        val list = response?.data.orEmpty().mapNotNull { item ->
                            val id = item.id ?: return@mapNotNull null
                            SelectOption(id, item.dropdownName.orEmpty())
                        }
                        _uiState.update { it.copy(options = it.options + (type to list)) }
                    },
                    failure = { _, _, message -> fail(message) }
                )
        }
    }

    /** Fills the form from the profile. Fields that don't apply to the role simply stay empty. */
    private fun initForm(profile: ProfileData?) {
        if (profile == null) return
        val values = mutableMapOf<String, String>()
        val ids = mutableMapOf<String, Int>()
        fun text(key: String, value: String?) {
            values[key] = value.orEmpty()
        }

        fun choice(key: String, item: NamedItem?) {
            values[key] = item?.name.orEmpty()
            item?.id?.let { ids[key] = it }
        }

        text(PKey.FULL_NAME, profile.full_name)
        text(PKey.EMAIL, profile.user_email)

        profile.customer?.let { c ->
            choice(PKey.CUSTOMER_TYPE, c.customer_type)
            c.individual?.let { i ->
                text(PKey.AADHAR, i.aadhar_number)
                text(PKey.PAN, i.pan_number)
                text(PKey.CITY, i.city)
                values[PKey.STATE] = i.state.orEmpty()
                i.state_id?.let { ids[PKey.STATE] = it }
            }
            c.business?.let { b ->
                text(PKey.COMPANY, b.company_name)
                text(PKey.ADDRESS, b.business_address)
                choice(PKey.CONSTITUTION, b.constitution)
                text(PKey.ESTABLISHED, b.company_date_establishment)
                text(PKey.GST, b.gst_number)
                choice(PKey.NATURE, b.nature_of_business)
                text(PKey.DIRECTORS, b.directors_details)
                text(PKey.FINANCE_HEAD, b.finance_head_details)
                text(PKey.BRANCHES, b.branch_offices_details)
            }
        }

        profile.operator?.let { o ->
            text(PKey.LICENSE_NUMBER, o.license_number)
            text(PKey.LICENSE_EXPIRY, o.license_expiry_date)
            choice(PKey.EXPERIENCE, o.years_of_experience)
            choice(PKey.OPERATOR_TYPE, o.operator_type)
            text(PKey.OWNER_ID, o.owner?.member_id)
        }

        val business = profile.customer?.is_business == true
        initialForm = values.toMap()
        initialIds = ids.toMap()
        initialIsBusiness = business
        _uiState.update {
            it.copy(
                formReady = true,
                form = values,
                formIds = ids,
                isBusiness = business,
                photoUri = fileUrl(profile.profile_image),
                showReverifyNote = false
            )
        }
    }

    private fun changeField(key: String, value: String) {
        _uiState.update {
            it.copy(
                form = it.form + (key to value),
                showReverifyNote = it.showReverifyNote || key == PKey.AADHAR || key == PKey.PAN
            )
        }
    }

    /** The radio picks the dropdown entry of the same name, so customer_type is ready to send. */
    private fun changeUserType(business: Boolean) {
        _uiState.update { state ->
            val match = state.options[11].orEmpty().firstOrNull { isBusinessName(it.name) == business }
            state.copy(
                isBusiness = business,
                form = if (match != null) state.form + (PKey.CUSTOMER_TYPE to match.name) else state.form,
                formIds = if (match != null) state.formIds + (PKey.CUSTOMER_TYPE to match.id) else state.formIds
            )
        }
    }

    private fun isBusinessName(name: String) = name.contains("business", ignoreCase = true)

    private fun isChoice(key: String) = key in PKey.DROPDOWN_TYPE

    private fun normalized(key: String, value: String?): String {
        val v = value.orEmpty().trim()
        return when (key) {
            PKey.AADHAR -> ProfileValidation.aadharDigits(v)
            PKey.PAN, PKey.GST -> v.uppercase()
            else -> v
        }
    }

    private fun changed(state: MyProfileUiState, key: String): Boolean =
        if (isChoice(key)) state.formIds[key] != initialIds[key]
        else normalized(key, state.form[key]) != normalized(key, initialForm[key])

    private fun saveProfile() {
        val state = _uiState.value
        if (state.isSaving || state.isPhotoUploading) return

        val switched = role == UserRole.CUSTOMER && state.isBusiness != initialIsBusiness
        validate(state, switched)?.let {
            showError(it)
            return
        }

        val body = buildBody(state, switched)
        if (body.length() == 0) {
            showError(string(R.string.crane_msg_no_changes))
            return
        }
        _uiState.update { it.copy(isSaving = true) }
        runWhenOnline {
            postUpdate(
                body,
                onSuccess = { data ->
                    initForm(data)
                    showSuccess(string(R.string.mp_msg_saved))
                    _uiState.update { it.copy(isSaving = false) }
                    _effect.send(MyProfileEffect.Saved)
                },
                onFail = { _uiState.update { it.copy(isSaving = false) } }
            )
        }
    }

    /** Only what changed. A customer who switches the user type sends the whole set of the new type. */
    private fun buildBody(state: MyProfileUiState, switched: Boolean): JSONObject {
        val body = JSONObject()

        fun send(key: String, force: Boolean = false) {
            if (!force && !changed(state, key)) return
            if (isChoice(key)) {
                state.formIds[key]?.let { body.put(key, it) }
            } else {
                val value = normalized(key, state.form[key])
                if (value.isNotEmpty()) body.put(key, value)
            }
        }

        send(PKey.FULL_NAME)
        send(PKey.EMAIL)

        when (role) {
            UserRole.CUSTOMER -> {
                (if (state.isBusiness) PKey.BUSINESS else PKey.INDIVIDUAL).forEach { send(it, force = switched) }
                if (switched) send(PKey.CUSTOMER_TYPE, force = true)
            }

            UserRole.OPERATOR -> {
                send(PKey.LICENSE_NUMBER)
                send(PKey.LICENSE_EXPIRY)
                send(PKey.EXPERIENCE)
                send(PKey.OPERATOR_TYPE)
                send(PKey.OWNER_ID)
            }

            UserRole.CRANE_OWNER -> Unit
        }
        return body
    }

    /** Format checks that mirror the server's rules. The server's own message is shown for the rest. */
    private fun validate(state: MyProfileUiState, switched: Boolean): String? {
        fun value(key: String) = normalized(key, state.form[key])
        fun checked(key: String) = switched || changed(state, key)

        if (value(PKey.FULL_NAME).isEmpty()) return string(R.string.mp_msg_name_required)
        if (value(PKey.EMAIL).isNotEmpty() && !ProfileValidation.isValidEmail(value(PKey.EMAIL))) {
            return string(R.string.msg_invalid_email)
        }

        when (role) {
            UserRole.CUSTOMER -> if (state.isBusiness) {
                if (switched && PKey.BUSINESS_REQUIRED.any { isMissing(state, it) }) {
                    return string(R.string.msg_fill_required)
                }
                if (checked(PKey.GST) && value(PKey.GST).isNotEmpty() && !ProfileValidation.isValidGst(value(PKey.GST))) {
                    return string(R.string.msg_invalid_gst)
                }
                val date = value(PKey.ESTABLISHED)
                if (checked(PKey.ESTABLISHED) && date.isNotEmpty() && !ProfileValidation.isNotInFuture(date)) {
                    return string(R.string.msg_establishment_future)
                }
            } else {
                if (switched && PKey.INDIVIDUAL_REQUIRED.any { isMissing(state, it) }) {
                    return string(R.string.msg_fill_required)
                }
                if (checked(PKey.AADHAR) && value(PKey.AADHAR).isNotEmpty() && !ProfileValidation.isValidAadhar(value(PKey.AADHAR))) {
                    return string(R.string.msg_invalid_aadhar)
                }
                if (checked(PKey.PAN) && value(PKey.PAN).isNotEmpty() && !ProfileValidation.isValidPan(value(PKey.PAN))) {
                    return string(R.string.msg_invalid_pan)
                }
            }

            UserRole.OPERATOR -> {
                val expiry = value(PKey.LICENSE_EXPIRY)
                if (changed(state, PKey.LICENSE_EXPIRY) && expiry.isNotEmpty() && !ProfileValidation.isNotInPast(expiry)) {
                    return string(R.string.msg_license_expired)
                }
            }

            UserRole.CRANE_OWNER -> Unit
        }
        return null
    }

    private fun isMissing(state: MyProfileUiState, key: String): Boolean =
        if (isChoice(key)) state.formIds[key] == null else normalized(key, state.form[key]).isEmpty()

    // ---------------------------------------------------------------------------------------
    // Photo and document replacement: POST /upload, then POST /profile/update with the path
    // ---------------------------------------------------------------------------------------

    private fun changePhoto(uri: String) {
        val previous = _uiState.value.photoUri
        _uiState.update { it.copy(photoUri = uri, isPhotoUploading = true) }
        uploadAndUpdate(PKey.PROFILE_IMAGE, uri) { data ->
            _uiState.update {
                it.copy(
                    isPhotoUploading = false,
                    photoUri = if (data != null) fileUrl(data.profile_image) ?: uri else previous
                )
            }
            if (data != null) showSuccess(string(R.string.mp_msg_saved))
        }
    }

    private fun replaceDocument(field: String, uri: String) {
        if (_uiState.value.uploadingDocument != null) return
        _uiState.update { it.copy(uploadingDocument = field) }
        uploadAndUpdate(field, uri) { data ->
            _uiState.update { it.copy(uploadingDocument = null) }
            if (data != null) showSuccess(string(R.string.mp_msg_document_replaced))
        }
    }

    /** [onDone] gets the updated profile, or null when the upload or the update failed. */
    private fun uploadAndUpdate(field: String, uri: String, onDone: (ProfileData?) -> Unit) {
        viewModelScope.launch {
            val part = withContext(Dispatchers.IO) { buildFilePart(Uri.parse(uri)) }
            if (part == null) {
                showError(string(R.string.msg_file_unreadable))
                onDone(null)
                return@launch
            }
            runWhenOnline {
                var path: String? = null
                repo.uploadApi(headers(), part).isRequestCallSuspendSuccess(
                    success = { response -> path = response?.data?.file_url },
                    failure = { _, _, message -> fail(message) }
                )
                val uploaded = path
                if (uploaded == null) {
                    onDone(null)
                    return@runWhenOnline
                }
                postUpdate(
                    JSONObject().put(field, uploaded),
                    onSuccess = { data -> onDone(data) },
                    onFail = { onDone(null) }
                )
            }
        }
    }

    private fun buildFilePart(uri: Uri): MultipartBody.Part? {
        val file = AppMethods.getFileFromUri(app, uri) ?: return null
        val body = AppMethods.createRequestBody(file, app, uri) ?: return null
        return MultipartBody.Part.createFormData(AppStrings.InputData.file, file.name, body)
    }

    /** POST /profile/update. The answer is the whole profile, which replaces the local copy. */
    private suspend fun postUpdate(
        body: JSONObject,
        onSuccess: suspend (ProfileData) -> Unit,
        onFail: () -> Unit
    ) {
        var data: ProfileData? = null
        repo.profileUpdateApi(headers(), body).isRequestCallSuspendSuccess(
            success = { response -> data = response?.data },
            failure = { _, _, message -> fail(message) }
        )
        val updated = data
        if (updated == null) {
            onFail()
            return
        }
        store.accept(updated, sm, app)
        onSuccess(updated)
    }

    // ---------------------------------------------------------------------------------------
    // Language
    // ---------------------------------------------------------------------------------------

    private fun loadLanguages() {
        runWhenOnline {
            setIsLoading(true)
            repo.profileLanguagesApi(headers()).isRequestCallSuspendSuccess(
                success = { response ->
                    val list = response?.data.orEmpty()
                    _uiState.update {
                        it.copy(
                            languages = list,
                            selectedLanguageId = list.firstOrNull { l -> l.selected == true }?.id
                                ?: sm.getData(AppStrings.SessionValues.languageId, AppStrings.Constants.defaultLanguageId)
                        )
                    }
                },
                failure = { _, _, message -> fail(message) }
            )
            setIsLoading(false)
        }
    }

    private fun saveLanguage() {
        val id = _uiState.value.selectedLanguageId ?: return
        runWhenOnline {
            setIsLoading(true)
            var saved = false
            repo.profileLanguageSaveApi(headers(), JSONObject().put("language_id", id))
                .isRequestCallSuspendSuccess(
                    success = { saved = true },
                    failure = { _, _, message -> fail(message) }
                )
            if (saved) {
                // The screens read this when they restart, so the message below is already translated
                sm.setData(AppStrings.SessionValues.languageId, id)
                store.refresh(repo, sm, app)
                showSuccess(string(R.string.mp_msg_language_saved))
                setIsLoading(false)
                _effect.send(MyProfileEffect.LanguageSaved)
            } else {
                setIsLoading(false)
            }
        }
    }

    // ---------------------------------------------------------------------------------------
    // Appearance
    // ---------------------------------------------------------------------------------------

    private fun loadAppearance() {
        runWhenOnline {
            setIsLoading(true)
            repo.profileAppearanceApi(headers()).isRequestCallSuspendSuccess(
                success = { response ->
                    val list = response?.data.orEmpty()
                    _uiState.update {
                        it.copy(
                            appearances = list,
                            selectedAppearance = list.firstOrNull { a -> a.selected == true }?.id ?: sm.getAppTheme()
                        )
                    }
                },
                failure = { _, _, message -> fail(message) }
            )
            setIsLoading(false)
        }
    }

    private fun saveAppearance() {
        val id = _uiState.value.selectedAppearance ?: return
        runWhenOnline {
            setIsLoading(true)
            var saved = false
            repo.profileAppearanceSaveApi(headers(), JSONObject().put("appearance", id))
                .isRequestCallSuspendSuccess(
                    success = { saved = true },
                    failure = { _, _, message -> fail(message) }
                )
            setIsLoading(false)
            if (saved) {
                // Applied now and cached, so the next start shows it without waiting for the server
                AppThemeState.setThemeMode(AppThemeMode.fromKey(id), app)
                store.profile.value?.let { store.accept(it.copy(appearance = id), sm, app) }
                showSuccess(string(R.string.mp_msg_appearance_saved))
                _effect.send(MyProfileEffect.Saved)
            }
        }
    }

    // ---------------------------------------------------------------------------------------
    // Notification settings
    // ---------------------------------------------------------------------------------------

    private fun prefsOf(groups: List<NotificationGroup>): Map<String, Boolean> =
        groups.flatMap { it.items.orEmpty() }
            .mapNotNull { item -> item.key?.let { it to (item.enabled == true) } }
            .toMap()

    private fun loadNotifications() {
        runWhenOnline {
            setIsLoading(true)
            repo.profileNotificationsApi(headers()).isRequestCallSuspendSuccess(
                success = { response ->
                    val groups = response?.data.orEmpty()
                    _uiState.update { it.copy(notificationGroups = groups, notificationPrefs = prefsOf(groups)) }
                },
                failure = { _, _, message -> fail(message) }
            )
            setIsLoading(false)
        }
    }

    private fun saveNotifications() {
        val state = _uiState.value
        val original = prefsOf(state.notificationGroups)
        val changes = state.notificationPrefs.filter { (key, value) -> original[key] != value }
        if (changes.isEmpty()) {
            viewModelScope.launch { _effect.send(MyProfileEffect.Saved) }
            return
        }
        runWhenOnline {
            setIsLoading(true)
            val prefs = JSONObject().also { json -> changes.forEach { (key, value) -> json.put(key, value) } }
            repo.profileNotificationsSaveApi(headers(), JSONObject().put("preferences", prefs))
                .isRequestCallSuspendSuccess(
                    success = { response ->
                        val groups = response?.data.orEmpty()
                        _uiState.update { it.copy(notificationGroups = groups, notificationPrefs = prefsOf(groups)) }
                        showSuccess(string(R.string.mp_msg_prefs_saved))
                        _effect.send(MyProfileEffect.Saved)
                    },
                    failure = { _, _, message -> fail(message) }
                )
            setIsLoading(false)
        }
    }

    // ---------------------------------------------------------------------------------------
    // Terms & Conditions / Privacy Policy (no token). The last answer is kept for offline use.
    // ---------------------------------------------------------------------------------------

    private fun loadLegal(type: String) {
        val cacheKey = "legal_$type"
        val cached = sm.getData(cacheKey, "").takeIf { it.isNotEmpty() }
            ?.let { runCatching { gson.fromJson(it, com.cranezo.ui.myprofile.model.LegalContent::class.java) }.getOrNull() }
        _uiState.update { it.copy(legal = cached, legalLoaded = cached != null) }

        runWhenOnline {
            if (cached == null) setIsLoading(true)
            repo.legalApi(emptyMap(), JSONObject().put("type", type)).isRequestCallSuspendSuccess(
                success = { response ->
                    response?.data?.let { content ->
                        sm.setData(cacheKey, gson.toJson(content))
                        _uiState.update { it.copy(legal = content, legalLoaded = true) }
                    }
                },
                failure = { _, _, message -> if (cached == null) fail(message) }
            )
            setIsLoading(false)
        }
    }

    // ---------------------------------------------------------------------------------------
    // Operator: the crane behind "View Details"
    // ---------------------------------------------------------------------------------------

    private fun loadAssignedCrane(craneId: String) {
        _uiState.update { it.copy(assignedCrane = null, craneLoaded = false) }
        runWhenOnline {
            setIsLoading(true)
            repo.assignedCraneDetailsApi(headers(), JSONObject().put("crane_id", craneId))
                .isRequestCallSuspendSuccess(
                    success = { response ->
                        _uiState.update { it.copy(assignedCrane = response?.data, craneLoaded = true) }
                    },
                    failure = { _, _, message ->
                        _uiState.update { it.copy(craneLoaded = true) }
                        fail(message)
                    }
                )
            setIsLoading(false)
        }
    }
}
