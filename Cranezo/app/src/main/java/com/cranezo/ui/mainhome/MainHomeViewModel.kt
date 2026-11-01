package com.cranezo.ui.mainhome

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.cranezo.core.BaseViewModel
import com.cranezo.repository.Repository
import com.cranezo.ui.chooserole.UserRole
import com.cranezo.ui.mainhome.model.MainHomeEffect
import com.cranezo.ui.mainhome.model.MainHomeIntent
import com.cranezo.ui.mainhome.model.MainHomeUiState
import com.cranezo.ui.theme.AppThemeState
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
import javax.inject.Inject

@HiltViewModel
class MainHomeViewModel @Inject constructor(
    var app: Application,
    var repo: Repository,
    var sm: SessionManager
) : BaseViewModel(app) {

    private val TAG = "MainHomeViewModel"

    private val role = runCatching { UserRole.valueOf(sm.getUserRole()) }
        .getOrDefault(UserRole.CRANE_OWNER)

    private val _uiState = MutableStateFlow(
        MainHomeUiState(
            role = role,
            userName = sm.getData(AppStrings.SessionValues.name, ""),
            memberId = sm.getData(AppStrings.SessionValues.memberId, ""),
            mobileNumber = "${
                sm.getData(AppStrings.SessionValues.countryCode, AppStrings.Constants.defaultCountryCode)
            } ${sm.getData(AppStrings.SessionValues.contactNumber, "")}".trim()
        )
    )
    val uiState: StateFlow<MainHomeUiState> = _uiState.asStateFlow()

    private val _effect = Channel<MainHomeEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: MainHomeIntent) {
        when (intent) {
            MainHomeIntent.ToggleOnline -> _uiState.update { it.copy(isOnline = !it.isOnline) }

            is MainHomeIntent.ThemeSelected -> AppThemeState.setThemeMode(intent.mode, app)

            MainHomeIntent.Logout -> logout()
        }
    }

    private fun logout() {
        // The theme and language are device preferences, so they survive a logout
        val theme = sm.getAppTheme()
        val languageId = sm.getData(AppStrings.SessionValues.languageId, AppStrings.Constants.defaultLanguageId)
        sm.clearSession()
        sm.setAppTheme(theme)
        sm.setData(AppStrings.SessionValues.languageId, languageId)
        viewModelScope.launch { _effect.send(MainHomeEffect.NavigateToLogin) }
    }
}
