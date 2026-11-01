package com.cranezo.ui.verifyotp.model

data class VerifyOtpUiState(
    val otp: String = "",
    val phoneNumber: String = ""
)

sealed interface VerifyOtpIntent {
    data class OtpChanged(val otp: String) : VerifyOtpIntent
    data object VerifyOtp : VerifyOtpIntent
    data object ResendOtp : VerifyOtpIntent
}

sealed interface VerifyOtpEffect {
    data object NavigateToChooseRole : VerifyOtpEffect
    data object NavigateToProfile : VerifyOtpEffect
    data object NavigateToHome : VerifyOtpEffect
}
