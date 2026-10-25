package com.cranezo.ui.profiletheme

import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.cranezo.core.composecore.BaseVMComposeActivity
import com.cranezo.ui.splash.SplashActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileThemeActivity : BaseVMComposeActivity<ProfileThemeViewModel>(ProfileThemeViewModel::class.java) {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
    }

    @Composable
    override fun Content(vm: ProfileThemeViewModel) {
        ProfileThemeScreen(
            vm = vm,
            showBackButton = true,
            onBackClick = { finish() },
            onContinueClick = {
                val intent = Intent(this@ProfileThemeActivity, SplashActivity::class.java)
                startActivity(intent)
                finish()
            }
        )
    }
}