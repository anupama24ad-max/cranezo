package com.cranezo.ui.chooserole

import android.os.Bundle
import androidx.compose.runtime.Composable
import com.cranezo.core.composecore.BaseVMComposeActivity
import com.cranezo.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChooseRoleActivity : BaseVMComposeActivity<ChooseRoleViewModel>(
    ChooseRoleViewModel::class.java
) {
    /** Sign-in and sign-up screens are always dark, whatever the app theme is (also after logout) */
    override val forceDarkTheme: Boolean? = true


    @Inject
    lateinit var sm: SessionManager

    @Composable
    override fun Content(vm: ChooseRoleViewModel) {
        // Phone back does not return to the (already used) OTP screen; it just sends the app to the background
        androidx.activity.compose.BackHandler { moveTaskToBack(true) }
        ChooseRoleScreen(
            vm = vm,
            onBackClick = { moveTaskToBack(true) },
            onContinueClick = { role ->
                sm.setUserRole(role.name)
                // a crane owner chooses a plan first (free first month, monthly or yearly), then the usual sign-up goes on
                val intent = if (role == UserRole.CRANE_OWNER) com.cranezo.ui.subscription.SubscriptionActivity.intent(this@ChooseRoleActivity, signup = true)
                else android.content.Intent(this@ChooseRoleActivity, com.cranezo.ui.profile.ProfileActivity::class.java)
                startActivity(intent)
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}
