package com.cranezo.ui.profile

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.cranezo.api.isRequestCallSuspendSuccess
import com.cranezo.core.BaseViewModel
import com.cranezo.repository.Repository
import com.cranezo.ui.chooserole.UserRole
import com.cranezo.ui.profile.model.ProfileEffect
import com.cranezo.ui.profile.model.ProfileIntent
import com.cranezo.ui.profile.model.ProfileUiState
import com.cranezo.utils.AppMethods
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
            fullName = sm.getData(AppStrings.SessionValues.name, ""),
            mobileNumber = formatPhoneNumber(
                sm.getData(AppStrings.SessionValues.countryCode, AppStrings.Constants.defaultCountryCode),
                sm.getData(AppStrings.SessionValues.contactNumber, "")
            ),
            companyName = sm.getData(AppStrings.SessionValues.companyName, ""),
            roleName = role.displayName()
        )
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ProfileEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.FullNameChanged -> _uiState.update { it.copy(fullName = intent.name) }

            is ProfileIntent.CompanyNameChanged -> _uiState.update { it.copy(companyName = intent.company) }

            ProfileIntent.SaveProfile -> {
                val state = _uiState.value
                when {
                    state.fullName.isBlank() -> showError("Please enter your full name")
                    state.companyName.isBlank() -> showError("Please enter your company name")
                    else -> updateProfileApi()
                }
            }
        }
    }

    private fun updateProfileApi() {
        val state = _uiState.value
        val jsonObj = JSONObject()
        jsonObj.put(AppStrings.InputData.user_id, sm.getUserId())
        jsonObj.put(AppStrings.InputData.role_id, role.id)
        jsonObj.put(AppStrings.InputData.full_name, state.fullName.trim())
        jsonObj.put(AppStrings.InputData.company_name, state.companyName.trim())
        viewModelScope.launch {
            runWhenOnline {
                setIsLoading(true)
                repo.updateProfileApi(AppMethods.getToken(sm), jsonObj).isRequestCallSuspendSuccess(
                    success = { response ->
                        val data = response?.data
                        (data?.access_token ?: data?.token)?.let { sm.setData(AppStrings.SessionValues.accessToken, it) }
                        data?.refresh_token?.let { sm.setData(AppStrings.SessionValues.refreshToken, it) }
                        data?.member_id?.let { sm.setData(AppStrings.SessionValues.memberId, it) }
                        sm.setData(AppStrings.SessionValues.name, state.fullName.trim())
                        sm.setData(AppStrings.SessionValues.companyName, state.companyName.trim())
                        sm.setData(AppStrings.SessionValues.roleId, role.id)
                        sm.setUserLoggedIn(true)
                        showSuccess(response?.message)
                        _effect.send(ProfileEffect.NavigateToHome)
                    },
                    failure = { body, errorType, message ->
                        Log.e(TAG, "updateProfileApi: message --> ${message}")
                        showError(message.toString())
                    })
                setIsLoading(false)
            }
        }
    }

    private fun UserRole.displayName(): String = when (this) {
        UserRole.CUSTOMER -> "customer"
        UserRole.CRANE_OWNER -> "crane owner"
        UserRole.OPERATOR -> "crane operator/captain"
    }

    private fun formatPhoneNumber(countryCode: String, number: String): String {
        val formatted = if (number.length == 10) "${number.take(5)} ${number.drop(5)}" else number
        return "$countryCode $formatted".trim()
    }
}
