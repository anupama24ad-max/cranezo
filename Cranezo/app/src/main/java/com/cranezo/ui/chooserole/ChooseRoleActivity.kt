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

    @Inject
    lateinit var sm: SessionManager

    @Composable
    override fun Content(vm: ChooseRoleViewModel) {
        ChooseRoleScreen(
            vm = vm,
            onBackClick = { finish() },
            onContinueClick = { role ->
                sm.setUserRole(role.name)
                val intent = android.content.Intent(this@ChooseRoleActivity, com.cranezo.ui.profile.ProfileActivity::class.java)
                startActivity(intent)
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}
