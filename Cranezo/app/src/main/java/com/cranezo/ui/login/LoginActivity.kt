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
    /** Sign-in and sign-up screens are always dark, whatever the app theme is (also after logout) */
    override val forceDarkTheme: Boolean? = true


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

        // Back (screen button or phone button) goes to language selection.
        // After a logout this is the first screen, so the language screen is opened instead of closing the app.
        val back: () -> Unit = {
            if (isTaskRoot) {
                startActivity(Intent(this@LoginActivity, com.cranezo.ui.chooselanguage.ChooseLanguageActivity::class.java))
            }
            finish()
        }
        androidx.activity.compose.BackHandler { back() }
        LoginScreen(vm = vm, onBack = back)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}
