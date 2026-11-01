package com.cranezo.ui.verifyotp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.cranezo.core.composecore.BaseVMComposeActivity
import com.cranezo.ui.chooserole.ChooseRoleActivity
import com.cranezo.ui.profile.ProfileActivity
import com.cranezo.ui.verifyotp.model.VerifyOtpEffect
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VerifyOtpActivity :
    BaseVMComposeActivity<VerifyOtpViewModel>(VerifyOtpViewModel::class.java) {

    @Composable
    override fun Content(vm: VerifyOtpViewModel) {
        LaunchedEffect(Unit) {
            vm.effect.collect { effect ->
                when (effect) {
                    VerifyOtpEffect.NavigateToChooseRole ->
                        startActivity(Intent(this@VerifyOtpActivity, ChooseRoleActivity::class.java))

                    VerifyOtpEffect.NavigateToProfile ->
                        startActivity(Intent(this@VerifyOtpActivity, ProfileActivity::class.java))

                    VerifyOtpEffect.NavigateToHome -> {
                        // TODO: open the home screen once it exists
                    }
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
