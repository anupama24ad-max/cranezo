package com.cranezo.ui.login.model

data class LoginUiState(
    val mobileNumber: String = "",
    val countryCode: String = "+91",
    val countryFlag: String = "🇮🇳"
)

sealed interface LoginIntent {
    data class MobileNumberChanged(val number: String) : LoginIntent
    data object GetOtp : LoginIntent
}

sealed interface LoginEffect {
    data object NavigateToVerifyOtp : LoginEffect
}
