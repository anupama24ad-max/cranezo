package com.cranezo.ui.mainhome

import android.content.Intent
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
        LaunchedEffect(Unit) {
            vm.effect.collect { effect ->
                when (effect) {
                    MainHomeEffect.NavigateToLogin -> startActivity(
                        Intent(this@MainHomeActivity, LoginActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                }
            }
        }

        MainHomeScreen(vm = vm)
    }
}
