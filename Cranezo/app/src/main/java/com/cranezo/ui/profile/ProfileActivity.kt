package com.cranezo.ui.profile

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.cranezo.core.composecore.BaseVMComposeActivity
import com.cranezo.ui.profile.model.ProfileEffect
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
        LaunchedEffect(Unit) {
            vm.effect.collect { effect ->
                when (effect) {
                    ProfileEffect.NavigateToHome -> {
                        // TODO: open the home screen once it exists
                    }
                }
            }
        }

        ProfileScreen(
            vm = vm,
            onBackClick = { finish() },
            onAddPhotoClick = {
                // Handle Add / Change photo action
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}
