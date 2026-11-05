package com.cranezo.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.cranezo.core.composecore.BaseVMComposeActivity
import com.cranezo.ui.mainhome.MainHomeActivity
import com.cranezo.ui.profile.model.ProfileEffect
import com.cranezo.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileActivity : BaseVMComposeActivity<ProfileViewModel>(
    ProfileViewModel::class.java
) {
    /** Sign-in and sign-up screens are always dark, whatever the app theme is (also after logout) */
    override val forceDarkTheme: Boolean? = true


    @Inject
    lateinit var sm: SessionManager

    @Composable
    override fun Content(vm: ProfileViewModel) {
        LaunchedEffect(Unit) {
            vm.effect.collect { effect ->
                when (effect) {
                    ProfileEffect.NavigateToHome -> startActivity(
                        Intent(this@ProfileActivity, MainHomeActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                }
            }
        }

        ProfileScreen(
            vm = vm,
            onBackClick = { finish() }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}
