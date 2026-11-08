package com.cranezo.ui.splash

import android.app.Application
import com.cranezo.core.BaseViewModel
import com.cranezo.repository.Repository
import com.cranezo.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    var app: Application,
    var sm: SessionManager,
    var repo: Repository
) : BaseViewModel(app) {
}