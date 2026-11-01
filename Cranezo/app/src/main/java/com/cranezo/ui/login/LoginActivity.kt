package com.cranezo.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.cranezo.LoginViewModel
import com.cranezo.core.composecore.BaseVMComposeActivity
import com.cranezo.ui.login.model.LoginEffect
import com.cranezo.ui.verifyotp.VerifyOtpActivity
import com.cranezo.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : BaseVMComposeActivity<LoginViewModel>(LoginViewModel::class.java) {

    @Inject
    lateinit var sm: SessionManager

    @Composable
    override fun Content(vm: LoginViewModel) {
        LaunchedEffect(Unit) {
            vm.effect.collect { effect ->
                when (effect) {
                    LoginEffect.NavigateToVerifyOtp ->
                        startActivity(Intent(this@LoginActivity, VerifyOtpActivity::class.java))
                }
            }
        }

        LoginScreen(vm = vm)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}
