package com.cranezo.ui.splash

import android.app.Application
import com.cranezo.core.BaseViewModel
import com.cranezo.repository.Repository
import com.cranezo.utils.SessionManager
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    var app: Application,
    var sm: SessionManager,
    var repo: Repository
) : BaseViewModel(app) {
    init {
        viewModelScope.launch { com.cranezo.update.AppUpdate.check(repo, force = true) }
    }
}