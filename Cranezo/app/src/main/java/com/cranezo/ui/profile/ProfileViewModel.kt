package com.cranezo.ui.profile

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import com.cranezo.core.BaseViewModel
import com.cranezo.repository.Repository
import com.cranezo.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    var app: Application,
    var repo: Repository,
    var sm: SessionManager
) : BaseViewModel(app) {
    private val TAG = "ProfileViewModel"

    val fullName = mutableStateOf("")
    val mobileNumber = mutableStateOf("+91 98765 43210")
    val companyName = mutableStateOf("")
    val roleName = mutableStateOf("crane owner")
    val photoRes = mutableStateOf<Int?>(null)

    fun onFullNameChange(name: String) {
        fullName.value = name
    }

    fun onCompanyNameChange(company: String) {
        companyName.value = company
    }

    fun saveProfile() {
        // Handle saving user profile
    }
}
