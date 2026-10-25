package com.cranezo.ui.profiletheme

import android.app.Application
import androidx.compose.runtime.State
import com.cranezo.core.BaseViewModel
import com.cranezo.repository.Repository
import com.cranezo.ui.theme.AppThemeMode
import com.cranezo.ui.theme.AppThemeState
import com.cranezo.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileThemeViewModel @Inject constructor(
    var app: Application,
    var sm: SessionManager,
    var repo: Repository
) : BaseViewModel(app) {

    val selectedTheme: State<AppThemeMode> = AppThemeState.currentThemeMode

    fun onThemeSelected(mode: AppThemeMode) {
        AppThemeState.setThemeMode(mode, app)
    }
}