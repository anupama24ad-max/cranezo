package com.cranezo.ui.verifyotp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.compose.runtime.Composable
import com.cranezo.core.composecore.BaseVMComposeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VerifyOtpActivity :
    BaseVMComposeActivity<VerifyOtpViewModel>(VerifyOtpViewModel::class.java) {

    @Composable
    override fun Content(vm: VerifyOtpViewModel) {
        VerifyOtpScreen(
            vm = vm,
            onBackClick = { finish() },
            onEditPhoneClick = { finish() },
            onVerifyClick = { otp ->
                val intent = Intent(this@VerifyOtpActivity, com.cranezo.ui.chooserole.ChooseRoleActivity::class.java)
                startActivity(intent)
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
        intent.getStringExtra("phone_number")?.let { phone ->
            if (phone.isNotEmpty()) {
                viewModel.phoneNumber.value = phone
            }
        }
    }
}