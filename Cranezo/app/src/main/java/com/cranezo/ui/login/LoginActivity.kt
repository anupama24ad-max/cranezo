package com.cranezo.ui.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.compose.runtime.Composable
import com.cranezo.LoginViewModel
import com.cranezo.core.composecore.BaseVMComposeActivity
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
        LoginScreen(
            vm = vm,
            onBackClick = { finish() },
            onGetOtpClick = { mobileNumber ->
                val fullPhone = "${vm.countryCode.value} $mobileNumber"
                val intent = Intent(this@LoginActivity, VerifyOtpActivity::class.java).apply {
                    putExtra("phone_number", fullPhone)
                }
                startActivity(intent)
            },
            onManagerLoginClick = {
                // Handle Manager Login
            },
            onEmergencyCallClick = {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:112")
                }
                startActivity(intent)
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}
