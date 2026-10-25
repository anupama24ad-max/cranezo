package com.cranezo

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import com.cranezo.core.BaseViewModel
import com.cranezo.repository.Repository
import com.cranezo.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    var app: Application,
    var repo: Repository,
    var sm: SessionManager
) : BaseViewModel(app) {

    private val TAG = "LoginViewModel"

    var mobileNumber = mutableStateOf("")
    var countryCode = mutableStateOf("+91")
    var countryFlag = mutableStateOf("🇮🇳")

    fun onMobileChange(number: String) {
        mobileNumber.value = number
    }
}
