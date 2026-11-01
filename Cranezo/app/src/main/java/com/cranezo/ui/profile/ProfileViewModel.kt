package com.cranezo.ui.profile

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.cranezo.BuildConfig
import com.cranezo.R
import com.cranezo.api.isRequestCallSuspendSuccess
import com.cranezo.core.BaseViewModel
import com.cranezo.interfaces.SnackbarStatus
import com.cranezo.repository.Repository
import com.cranezo.ui.chooserole.UserRole
import com.cranezo.ui.components.SelectOption
import com.cranezo.ui.profile.model.FileUpload
import com.cranezo.ui.profile.model.OptionSource
import com.cranezo.ui.profile.model.ProfileDocument
import com.cranezo.ui.profile.model.ProfileEffect
import com.cranezo.ui.profile.model.ProfileField
import com.cranezo.ui.profile.model.ProfileFormSchema
import com.cranezo.ui.profile.model.ProfileIntent
import com.cranezo.ui.profile.model.ProfileUiState
import com.cranezo.ui.profile.model.ProfileValidation
import com.cranezo.ui.profile.model.UserTypes
import com.cranezo.utils.AppMethods
import com.cranezo.utils.AppStrings
import com.cranezo.utils.SessionManager
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
class ProfileViewModel @Inject constructor(
    var app: Application,
    var repo: Repository,
    var sm: SessionManager
) : BaseViewModel(app) {
    private val TAG = "ProfileViewModel"

    private val role = runCatching { UserRole.valueOf(sm.getUserRole()) }
        .getOrDefault(UserRole.CRANE_OWNER)

    private val _uiState = MutableStateFlow(
        ProfileUiState(
            role = role,
            mobileNumber = formatPhoneNumber(
                sm.getData(AppStrings.SessionValues.countryCode, AppStrings.Constants.defaultCountryCode),
                sm.getData(AppStrings.SessionValues.contactNumber, "")
            ),
            values = mapOf(
                ProfileField.FULL_NAME to sm.getData(AppStrings.SessionValues.name, ""),
                ProfileField.COMPANY_NAME to sm.getData(AppStrings.SessionValues.companyName, "")
            ),
            // A photo uploaded earlier (the session keeps the server path)
            photo = sm.getData(AppStrings.SessionValues.profileImage, "")
                .ifEmpty { null }
                ?.let { FileUpload(displayUri = absoluteUrl(it), path = it) }
        )
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ProfileEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // Dropdowns being fetched right now, so the same one isn't requested twice
    private val loadingSources = mutableSetOf<OptionSource>()

    init {
        loadOptions(ProfileFormSchema.remoteSources(role))
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.FieldChanged -> _uiState.update {
                it.copy(values = it.values + (intent.field to intent.value))
            }

            is ProfileIntent.OptionSelected -> _uiState.update {
                it.copy(
                    values = it.values + (intent.field to intent.option.name),
                    selectedOptionIds = it.selectedOptionIds + (intent.field to intent.option.id)
                )
            }

            is ProfileIntent.UserTypeChanged -> changeUserType(intent.userType)

            is ProfileIntent.PhotoSelected -> uploadPhoto(intent.uri)

            is ProfileIntent.DocumentSelected -> uploadDocument(intent.document, intent.uri)

            is ProfileIntent.RetryOptions -> loadOptions(listOf(intent.source))

            ProfileIntent.SaveProfile -> saveProfile()
        }
    }

    /** Switching the user type drops the values of the fields it hides, so stale data isn't sent. */
    private fun changeUserType(userType: String) {
        _uiState.update { state ->
            if (state.userType == userType) return@update state
            val hidden = ProfileFormSchema.fields(role, state.userType) -
                    ProfileFormSchema.fields(role, userType)
            state.copy(
                userType = userType,
                values = state.values - hidden,
                selectedOptionIds = state.selectedOptionIds - hidden
            )
        }
    }

    // ---------------------------------------------------------------------------------------
    // Dropdowns
    // ---------------------------------------------------------------------------------------

    /**
     * Fetches dropdowns one after another (POST /api/dropdowns { type }) inside a single
     * runWhenOnline: it restarts the network check on every call, so several parallel calls
     * would cancel each other.
     */
    private fun loadOptions(sources: List<OptionSource>) {
        val toLoad = sources.filter { it.type != null && loadingSources.add(it) }
        if (toLoad.isEmpty()) return

        viewModelScope.launch {
            runWhenOnline {
                setIsLoading(true)
                toLoad.forEach { source ->
                    val jsonObj = JSONObject().put(AppStrings.InputData.type, source.type)
                    repo.dropDownApi(AppMethods.getToken(sm), jsonObj).isRequestCallSuspendSuccess(
                        success = { response ->
                            val options = response?.data.orEmpty().mapNotNull { item ->
                                val id = item.id ?: return@mapNotNull null
                                SelectOption(id, item.dropdownName.orEmpty())
                            }
                            onOptionsLoaded(source, options)
                        },
                        failure = { body, errorType, message ->
                            Log.e(TAG, "loadOptions($source): message --> ${message}")
                            showError(message.toString())
                        })
                    loadingSources.remove(source)
                }
                setIsLoading(false)
            }
        }
    }

    private fun onOptionsLoaded(source: OptionSource, options: List<SelectOption>) {
        _uiState.update { state ->
            var userType = state.userType
            // If the server doesn't have the default user type, fall back to its first option
            if (source == ProfileFormSchema.userTypeSource(role) &&
                options.isNotEmpty() && options.none { it.name == userType }
            ) {
                userType = options.first().name
            }
            state.copy(options = state.options + (source to options), userType = userType)
        }
    }

    // ---------------------------------------------------------------------------------------
    // Uploads: POST /api/upload, one file per request. The returned path is sent to updateProfile.
    // ---------------------------------------------------------------------------------------

    private fun uploadPhoto(uri: String) {
        val previous = _uiState.value.photo
        _uiState.update { it.copy(photo = FileUpload(displayUri = uri, isUploading = true)) }
        uploadFile(uri) { path ->
            if (path != null) sm.setData(AppStrings.SessionValues.profileImage, path)
            _uiState.update {
                it.copy(photo = if (path != null) FileUpload(displayUri = uri, path = path) else previous)
            }
        }
    }

    private fun uploadDocument(document: ProfileDocument, uri: String) {
        val previous = _uiState.value.documents[document]
        _uiState.update {
            it.copy(documents = it.documents + (document to FileUpload(displayUri = uri, isUploading = true)))
        }
        uploadFile(uri) { path ->
            _uiState.update { state ->
                val result = if (path != null) FileUpload(displayUri = uri, path = path) else previous
                state.copy(
                    documents = if (result != null) state.documents + (document to result)
                    else state.documents - document
                )
            }
        }
    }

    /** Uploads [uri] and reports the server path, or null when the upload didn't work. */
    private fun uploadFile(uri: String, onResult: (path: String?) -> Unit) {
        viewModelScope.launch {
            val part = withContext(Dispatchers.IO) { buildFilePart(Uri.parse(uri)) }
            if (part == null) {
                showError(string(R.string.msg_file_unreadable))
                onResult(null)
                return@launch
            }

            runWhenOnline {
                var path: String? = null
                repo.uploadApi(AppMethods.getToken(sm), part).isRequestCallSuspendSuccess(
                    success = { response ->
                        path = response?.data?.file_url
                        if (path == null) showError(string(R.string.msg_upload_failed))
                    },
                    failure = { body, errorType, message ->
                        Log.e(TAG, "uploadFile: message --> ${message}")
                        showError(message.toString())
                    })
                onResult(path)
            }
        }
    }

    private fun buildFilePart(uri: Uri): MultipartBody.Part? {
        val file = AppMethods.getFileFromUri(app, uri) ?: return null
        val body = AppMethods.createRequestBody(file, app, uri) ?: return null
        return MultipartBody.Part.createFormData(AppStrings.InputData.file, file.name, body)
    }

    /** The API returns paths like "/public/uploads/x.png", relative to the server root (not /api/). */
    private fun absoluteUrl(path: String): String {
        if (path.startsWith("http")) return path
        val server = Uri.parse(BuildConfig.SERVER_URL)
        return "${server.scheme}://${server.authority}${if (path.startsWith("/")) "" else "/"}$path"
    }

    // ---------------------------------------------------------------------------------------
    // Save
    // ---------------------------------------------------------------------------------------

    private fun saveProfile() {
        val state = _uiState.value
        if (state.isUploading) {
            showBanner(string(R.string.msg_files_uploading), SnackbarStatus.informative)
            return
        }
        validate(state)?.let {
            showError(it)
            return
        }
        updateProfileApi(state)
    }

    /** Format checks that mirror the server. Required fields are already covered by canContinue. */
    private fun validate(state: ProfileUiState): String? {
        if (!state.canContinue) return string(R.string.msg_fill_required)

        return when (state.role) {
            // Optional fields are only checked when the user filled them
            UserRole.CRANE_OWNER -> {
                val email = state.value(ProfileField.EMAIL)
                val pincode = state.value(ProfileField.PINCODE)
                val established = state.value(ProfileField.ESTABLISHED_ON)
                when {
                    email.isNotBlank() && !ProfileValidation.isValidEmail(email) ->
                        string(R.string.msg_invalid_email)

                    pincode.isNotBlank() && !ProfileValidation.isValidPincode(pincode) ->
                        string(R.string.msg_invalid_pincode)

                    established.isNotBlank() && !ProfileValidation.isNotInFuture(established) ->
                        string(R.string.msg_establishment_future)

                    else -> null
                }
            }

            UserRole.CUSTOMER -> if (state.userType == UserTypes.BUSINESS_USER) {
                when {
                    !ProfileValidation.isValidGst(state.value(ProfileField.GST_NUMBER)) ->
                        string(R.string.msg_invalid_gst)

                    !ProfileValidation.isNotInFuture(state.value(ProfileField.ESTABLISHED_ON)) ->
                        string(R.string.msg_establishment_future)

                    else -> null
                }
            } else {
                when {
                    !ProfileValidation.isValidAadhar(state.value(ProfileField.AADHAR)) ->
                        string(R.string.msg_invalid_aadhar)

                    !ProfileValidation.isValidPan(state.value(ProfileField.PAN)) ->
                        string(R.string.msg_invalid_pan)

                    else -> null
                }
            }

            UserRole.OPERATOR ->
                if (!ProfileValidation.isNotInPast(state.value(ProfileField.LICENSE_EXPIRY))) {
                    string(R.string.msg_license_expired)
                } else null
        }
    }

    /**
     * Builds the updateProfile body for the role. Optional fields the user left empty are left out.
     * Files are sent as the paths returned by the upload API, without the domain.
     */
    private fun buildProfileRequest(state: ProfileUiState): JSONObject {
        val json = JSONObject()
        fun put(key: String, value: Any?) {
            when (value) {
                null -> Unit
                is String -> if (value.isNotBlank()) json.put(key, value.trim())
                else -> json.put(key, value)
            }
        }
        fun text(field: ProfileField) = state.value(field)
        fun optionId(field: ProfileField) = state.selectedOptionIds[field]

        put(AppStrings.InputData.user_id, sm.getUserId())
        put(AppStrings.InputData.role_id, state.role.id)
        put(AppStrings.InputData.full_name, text(ProfileField.FULL_NAME))
        put(AppStrings.InputData.profile_image, state.photo?.path)

        when (state.role) {
            UserRole.CRANE_OWNER -> {
                put(AppStrings.InputData.company_name, text(ProfileField.COMPANY_NAME))
                put(AppStrings.InputData.owner_type, optionId(ProfileField.OWNER_TYPE))
                put(AppStrings.InputData.years_of_experience, optionId(ProfileField.EXPERIENCE))
                put(AppStrings.InputData.number_of_business_partners, optionId(ProfileField.PARTNERS))
                put(AppStrings.InputData.business_ownership_type, optionId(ProfileField.OWNERSHIP_TYPE))
                put(AppStrings.InputData.business_category, text(ProfileField.BUSINESS_CATEGORY))
                put(AppStrings.InputData.company_date_establishment, text(ProfileField.ESTABLISHED_ON))
                put(AppStrings.InputData.business_address, text(ProfileField.COMPANY_ADDRESS))
                put(AppStrings.InputData.city, text(ProfileField.CITY))
                // For owners the state is plain text
                put(AppStrings.InputData.state, text(ProfileField.STATE))
                put(AppStrings.InputData.pincode, text(ProfileField.PINCODE))
                put(AppStrings.InputData.user_email, text(ProfileField.EMAIL))
            }

            UserRole.CUSTOMER -> {
                put(AppStrings.InputData.customer_type, state.userTypeId)
                if (state.userType == UserTypes.BUSINESS_USER) {
                    put(AppStrings.InputData.company_name, text(ProfileField.COMPANY_NAME))
                    put(AppStrings.InputData.business_address, text(ProfileField.COMPANY_ADDRESS))
                    put(AppStrings.InputData.constitution, optionId(ProfileField.CONSTITUTION))
                    put(AppStrings.InputData.company_date_establishment, text(ProfileField.ESTABLISHED_ON))
                    put(AppStrings.InputData.gst_number, text(ProfileField.GST_NUMBER).uppercase())
                    put(AppStrings.InputData.nature_of_business, optionId(ProfileField.NATURE_OF_BUSINESS))
                    put(AppStrings.InputData.directors_details, text(ProfileField.DIRECTORS))
                    put(AppStrings.InputData.finance_head_details, text(ProfileField.ACCOUNTS_HEAD))
                    put(AppStrings.InputData.branch_offices_details, text(ProfileField.BRANCH_OFFICES))
                } else {
                    put(AppStrings.InputData.aadhar_number, ProfileValidation.aadharDigits(text(ProfileField.AADHAR)))
                    put(AppStrings.InputData.pan_number, text(ProfileField.PAN).uppercase())
                    put(AppStrings.InputData.city, text(ProfileField.CITY))
                    put(AppStrings.InputData.state_id, optionId(ProfileField.STATE))
                }
            }

            UserRole.OPERATOR -> {
                put(AppStrings.InputData.operator_license_number, text(ProfileField.LICENSE_NUMBER).uppercase())
                put(AppStrings.InputData.license_expiry_date, text(ProfileField.LICENSE_EXPIRY))
                put(AppStrings.InputData.years_of_experience, optionId(ProfileField.EXPERIENCE))
                put(AppStrings.InputData.operator_type, state.userTypeId)
                if (state.userType == UserTypes.OWNER_BASED) {
                    put(AppStrings.InputData.owner_id, text(ProfileField.OWNER_ID).uppercase())
                }
                state.documents.forEach { (document, upload) -> put(document.apiField, upload.path) }
            }
        }
        return json
    }

    private fun updateProfileApi(state: ProfileUiState) {
        val jsonObj = buildProfileRequest(state)
        viewModelScope.launch {
            runWhenOnline {
                setIsLoading(true)
                repo.updateProfileApi(AppMethods.getToken(sm), jsonObj).isRequestCallSuspendSuccess(
                    success = { response ->
                        val data = response?.data
                        // The role is encoded in the token and has just changed: replace both tokens
                        (data?.access_token ?: data?.token)?.let { sm.setData(AppStrings.SessionValues.accessToken, it) }
                        data?.refresh_token?.let { sm.setData(AppStrings.SessionValues.refreshToken, it) }
                        data?.member_id?.let { sm.setData(AppStrings.SessionValues.memberId, it) }
                        sm.setData(AppStrings.SessionValues.name, state.value(ProfileField.FULL_NAME).trim())
                        sm.setData(AppStrings.SessionValues.companyName, state.value(ProfileField.COMPANY_NAME).trim())
                        sm.setData(AppStrings.SessionValues.roleId, state.role.id)
                        sm.setUserLoggedIn(true)
                        showSuccess(response?.message)
                        _effect.send(ProfileEffect.NavigateToHome)
                    },
                    failure = { body, errorType, message ->
                        // The form stays filled so the user can fix it and try again
                        Log.e(TAG, "updateProfileApi: message --> ${message}")
                        showError(message.toString())
                    })
                setIsLoading(false)
            }
        }
    }

    private fun formatPhoneNumber(countryCode: String, number: String): String {
        val formatted = if (number.length == 10) "${number.take(5)} ${number.drop(5)}" else number
        return "$countryCode $formatted".trim()
    }
}
