package com.cranezo.ui.verifyotp

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import com.cranezo.core.BaseViewModel
import com.cranezo.repository.Repository
import com.cranezo.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VerifyOtpViewModel @Inject constructor(
    var app: Application,
    var repo: Repository,
    var sm: SessionManager
) : BaseViewModel(app) {
    private val TAG = "VerifyOtpViewModel"

    var otp = mutableStateOf("")
    var phoneNumber = mutableStateOf("+91 98765 43210")

    fun onOtpChange(newOtp: String) {
        otp.value = newOtp
    }

    fun resendOtp() {
        // Handle API call to resend OTP
    }
}