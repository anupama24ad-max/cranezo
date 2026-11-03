package com.cranezo.ui.mainhome

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.cranezo.push.PushNotifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.cranezo.core.composecore.BaseVMComposeActivity
import com.cranezo.ui.login.LoginActivity
import com.cranezo.ui.mainhome.model.MainHomeEffect
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainHomeActivity :
    BaseVMComposeActivity<MainHomeViewModel>(MainHomeViewModel::class.java) {

    @Composable
    override fun Content(vm: MainHomeViewModel) {
        // Android 13+: ask once for permission to show job alerts
        val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
        LaunchedEffect(Unit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !PushNotifications.canPost(this@MainHomeActivity)) {
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        LaunchedEffect(Unit) {
            vm.effect.collect { effect ->
                when (effect) {
                    MainHomeEffect.NavigateToLogin -> startActivity(
                        Intent(this@MainHomeActivity, LoginActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )

                    MainHomeEffect.LanguageChanged -> recreate()
                }
            }
        }

        MainHomeScreen(vm = vm)
    }
}
