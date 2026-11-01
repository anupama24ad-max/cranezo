package com.cranezo.ui.verifyotp

import android.annotation.SuppressLint
import android.app.Application
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.cranezo.api.isRequestCallSuspendSuccess
import com.cranezo.core.BaseViewModel
import com.cranezo.repository.Repository
import com.cranezo.ui.chooserole.UserRole
import com.cranezo.ui.verifyotp.model.VerifyOtpApiResponse
import com.cranezo.ui.verifyotp.model.VerifyOtpEffect
import com.cranezo.ui.verifyotp.model.VerifyOtpIntent
import com.cranezo.ui.verifyotp.model.VerifyOtpUiState
import com.cranezo.utils.AppMethods
import com.cranezo.utils.AppStrings
import com.cranezo.utils.CLog
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
class VerifyOtpViewModel @Inject constructor(
    var app: Application,
    var repo: Repository,
    var sm: SessionManager
) : BaseViewModel(app) {
    private val TAG = "VerifyOtpViewModel"

    private val countryCode =
        sm.getData(AppStrings.SessionValues.countryCode, AppStrings.Constants.defaultCountryCode)
    private val mobileNumber = sm.getData(AppStrings.SessionValues.contactNumber, "")

    private val _uiState = MutableStateFlow(
        VerifyOtpUiState(phoneNumber = formatPhoneNumber(countryCode, mobileNumber))
    )
    val uiState: StateFlow<VerifyOtpUiState> = _uiState.asStateFlow()

    private val _effect = Channel<VerifyOtpEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: VerifyOtpIntent) {
        when (intent) {
            is VerifyOtpIntent.OtpChanged -> {
                val digits = intent.otp.filter { it.isDigit() }.take(OTP_LENGTH)
                _uiState.update { it.copy(otp = digits) }
            }

            VerifyOtpIntent.VerifyOtp -> {
                if (_uiState.value.otp.length != OTP_LENGTH) {
                    showError("Please enter the 6-digit OTP")
                    return
                }
                verifyOtpApi()
            }

            VerifyOtpIntent.ResendOtp -> resendOtpApi()
        }
    }

    private fun verifyOtpApi() {
        val jsonObj = JSONObject()
        jsonObj.put(AppStrings.InputData.user_id, sm.getUserId())
        jsonObj.put(AppStrings.InputData.otp_number, _uiState.value.otp)
        viewModelScope.launch {
            runWhenOnline {
                setIsLoading(true)
                repo.verifyOtpApi(AppMethods.getToken(sm, true), jsonObj).isRequestCallSuspendSuccess(
                    success = { response ->
                        val data = response?.data
                        saveSession(data)
                        showSuccess(response?.message)
                        _effect.send(nextScreen(data))
                    },
                    failure = { body, errorType, message ->
                        Log.e(TAG, "verifyOtpApi: message --> ${message}")
                        showError(message.toString())
                    })
                setIsLoading(false)
            }
        }
    }

    @SuppressLint("HardwareIds")
    private fun resendOtpApi() {
        val jsonObj = JSONObject()
        jsonObj.put(AppStrings.InputData.phone_number, mobileNumber)
        jsonObj.put(AppStrings.InputData.country_code, countryCode)
        jsonObj.put(
            AppStrings.InputData.language_id,
            sm.getData(AppStrings.SessionValues.languageId, AppStrings.Constants.defaultLanguageId)
        )
        jsonObj.put(
            AppStrings.InputData.device_id,
            Settings.Secure.getString(app.contentResolver, Settings.Secure.ANDROID_ID).orEmpty()
        )
        jsonObj.put(AppStrings.InputData.platform_type, AppStrings.Constants.platformAndroid)
        viewModelScope.launch {
            runWhenOnline {
                setIsLoading(true)
                repo.registerOrLoginApi(AppMethods.getToken(sm, true), jsonObj).isRequestCallSuspendSuccess(
                    success = { response ->
                        CLog.e(TAG, "resendOtpApi: otp --> ${response?.data?.otp}")
                        response?.data?.user_id?.let { sm.setUserId(it) }
                        _uiState.update { it.copy(otp = "") }
                        showSuccess(response?.message)
                    },
                    failure = { body, errorType, message ->
                        Log.e(TAG, "resendOtpApi: message --> ${message}")
                        showError(message.toString())
                    })
                setIsLoading(false)
            }
        }
    }

    private fun saveSession(data: VerifyOtpApiResponse?) {
        data?.user_id?.let { sm.setUserId(it) }
        (data?.access_token ?: data?.token)?.let { sm.setData(AppStrings.SessionValues.accessToken, it) }
        data?.refresh_token?.let { sm.setData(AppStrings.SessionValues.refreshToken, it) }
        data?.member_id?.let { sm.setData(AppStrings.SessionValues.memberId, it) }
        data?.full_name?.let { sm.setData(AppStrings.SessionValues.name, it) }
        data?.company_name?.let { sm.setData(AppStrings.SessionValues.companyName, it) }
        UserRole.fromId(data?.role_id)?.let { role ->
            sm.setUserRole(role.name)
            sm.setData(AppStrings.SessionValues.roleId, role.id)
        }
    }

    private fun nextScreen(data: VerifyOtpApiResponse?): VerifyOtpEffect {
        val hasRole = data?.role_id_status == ROLE_ASSIGNED && UserRole.fromId(data.role_id) != null
        return when {
            !hasRole -> VerifyOtpEffect.NavigateToChooseRole
            data?.full_name.isNullOrBlank() -> VerifyOtpEffect.NavigateToProfile
            else -> {
                sm.setUserLoggedIn(true)
                VerifyOtpEffect.NavigateToHome
            }
        }
    }

    private fun formatPhoneNumber(countryCode: String, number: String): String {
        val formatted = if (number.length == 10) "${number.take(5)} ${number.drop(5)}" else number
        return "$countryCode $formatted".trim()
    }

    companion object {
        private const val OTP_LENGTH = 6

        // role_id_status: 0 -> user already has a role, 1 -> user does not have a role yet
        private const val ROLE_ASSIGNED = 0
    }
}
