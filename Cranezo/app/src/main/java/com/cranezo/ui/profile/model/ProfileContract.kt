package com.cranezo.ui.profile.model

data class ProfileUiState(
    val fullName: String = "",
    val mobileNumber: String = "",
    val companyName: String = "",
    val roleName: String = "crane owner",
    val photoRes: Int? = null
)

sealed interface ProfileIntent {
    data class FullNameChanged(val name: String) : ProfileIntent
    data class CompanyNameChanged(val company: String) : ProfileIntent
    data object SaveProfile : ProfileIntent
}

sealed interface ProfileEffect {
    data object NavigateToHome : ProfileEffect
}
