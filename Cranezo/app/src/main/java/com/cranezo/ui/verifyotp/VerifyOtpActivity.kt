package com.cranezo.ui.verifyotp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.cranezo.core.composecore.BaseVMComposeActivity
import com.cranezo.ui.chooserole.ChooseRoleActivity
import com.cranezo.ui.mainhome.MainHomeActivity
import com.cranezo.ui.profile.ProfileActivity
import com.cranezo.ui.verifyotp.model.VerifyOtpEffect
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VerifyOtpActivity :
    BaseVMComposeActivity<VerifyOtpViewModel>(VerifyOtpViewModel::class.java) {
    /** Sign-in and sign-up screens are always dark, whatever the app theme is (also after logout) */
    override val forceDarkTheme: Boolean? = true


    @Composable
    override fun Content(vm: VerifyOtpViewModel) {
        LaunchedEffect(Unit) {
            vm.effect.collect { effect ->
                when (effect) {
                    VerifyOtpEffect.NavigateToChooseRole ->
                        startActivity(Intent(this@VerifyOtpActivity, ChooseRoleActivity::class.java))

                    VerifyOtpEffect.NavigateToProfile ->
                        startActivity(Intent(this@VerifyOtpActivity, ProfileActivity::class.java))

                    VerifyOtpEffect.NavigateToHome -> startActivity(
                        Intent(this@VerifyOtpActivity, MainHomeActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                }
            }
        }

        VerifyOtpScreen(
            vm = vm,
            onBackClick = { finish() },
            onEditPhoneClick = { finish() },
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
