package com.cranezo.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.cranezo.core.composecore.BaseVMComposeActivity

import com.cranezo.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : BaseVMComposeActivity<SplashViewModel>(SplashViewModel::class.java) {
    @Composable
    override fun Content(vm: SplashViewModel) {
        return SplashScreen(vm)
    }
    @Inject
    lateinit var sm : SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        // 2) Keep the system splash until ViewModel reports it's ready
        splash.setKeepOnScreenCondition {
            // return true to keep splash; false to dismiss
            false
        }

        // 3) (Optional) Exit animation (runs when splash is about to go away)
        splash.setOnExitAnimationListener { provider ->
            // You can animate the splash icon if you like, then:
            provider.remove() // remove immediately when you’re done
        }
  }
}