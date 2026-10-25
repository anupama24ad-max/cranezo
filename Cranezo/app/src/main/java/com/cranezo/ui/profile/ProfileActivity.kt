package com.cranezo.ui.profile

import android.os.Bundle
import androidx.compose.runtime.Composable
import com.cranezo.core.composecore.BaseVMComposeActivity
import com.cranezo.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileActivity : BaseVMComposeActivity<ProfileViewModel>(
    ProfileViewModel::class.java
) {

    @Inject
    lateinit var sm: SessionManager

    @Composable
    override fun Content(vm: ProfileViewModel) {
        ProfileScreen(
            vm = vm,
            onBackClick = { finish() },
            onAddPhotoClick = {
                // Handle Add / Change photo action
            },
            onContinueClick = {
                vm.saveProfile()
                // Handle Profile Continue Navigation
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val role = sm.getUserRole()
        if (role.isNotEmpty()) {
            val formattedRole = when (role.uppercase()) {
                "CUSTOMER" -> "customer"
                "CRANE_OWNER" -> "crane owner"
                "OPERATOR" -> "crane operator/captain"
                else -> role.lowercase().replace('_', ' ')
            }
            viewModel.roleName.value = formattedRole
        }
    }
}
