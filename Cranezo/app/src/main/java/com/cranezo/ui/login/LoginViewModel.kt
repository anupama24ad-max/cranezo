package com.cranezo

import android.annotation.SuppressLint
import android.app.Application
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.cranezo.api.isRequestCallSuspendSuccess
import com.cranezo.core.BaseViewModel
import com.cranezo.repository.Repository
import com.cranezo.ui.login.model.LoginEffect
import com.cranezo.ui.login.model.LoginIntent
import com.cranezo.ui.login.model.LoginUiState
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
class LoginViewModel @Inject constructor(
    var app: Application,
    var repo: Repository,
    var sm: SessionManager
) : BaseViewModel(app) {

    private val TAG = "LoginViewModel"

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LoginEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.MobileNumberChanged -> {
                val digits = intent.number.filter { it.isDigit() }.take(MOBILE_NUMBER_LENGTH)
                _uiState.update { it.copy(mobileNumber = digits) }
            }

            LoginIntent.GetOtp -> {
                if (_uiState.value.mobileNumber.length != MOBILE_NUMBER_LENGTH) {
                    showError("Please enter a valid 10-digit mobile number")
                    return
                }
                registerOrLoginApi()
            }
        }
    }

    @SuppressLint("HardwareIds")
    private fun registerOrLoginApi() {
        val state = _uiState.value
        val jsonObj = JSONObject()
        jsonObj.put(AppStrings.InputData.phone_number, state.mobileNumber)
        jsonObj.put(AppStrings.InputData.country_code, state.countryCode)
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
                        val data = response?.data
                        CLog.e(TAG, "registerOrLoginApi: otp --> ${data?.otp}")
                        sm.setUserId(data?.user_id.orEmpty())
                        sm.setData(AppStrings.SessionValues.contactNumber, data?.phone_number ?: state.mobileNumber)
                        sm.setData(AppStrings.SessionValues.countryCode, data?.country_code ?: state.countryCode)
                        sm.setData(AppStrings.SessionValues.memberId, data?.member_id.orEmpty())
                        showSuccess(response?.message)
                        _effect.send(LoginEffect.NavigateToVerifyOtp)
                    },
                    failure = { body, errorType, message ->
                        Log.e(TAG, "registerOrLoginApi: message --> ${message}")
                        showError(message.toString())
                    })
                setIsLoading(false)
            }
        }
    }

    companion object {
        private const val MOBILE_NUMBER_LENGTH = 10
    }
}
