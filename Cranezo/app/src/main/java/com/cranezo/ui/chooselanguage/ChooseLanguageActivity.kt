package com.cranezo.ui.chooselanguage

import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.cranezo.core.composecore.BaseVMComposeActivity
import com.cranezo.ui.chooselanguage.model.ChooseLanguageEffect
import com.cranezo.ui.login.LoginActivity
import com.cranezo.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChooseLanguageActivity : BaseVMComposeActivity<ChooseLanguageViewModel>(ChooseLanguageViewModel::class.java) {

    @Inject
    lateinit var sm: SessionManager

    @Composable
    override fun Content(vm: ChooseLanguageViewModel) {
        LaunchedEffect(Unit) {
            vm.effect.collect { effect ->
                when (effect) {
                    ChooseLanguageEffect.NavigateToLogin ->
                        startActivity(Intent(this@ChooseLanguageActivity, LoginActivity::class.java))
                }
            }
        }

        ChooseLanguageScreen(vm = vm)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUi()
    }

    private fun initUi() {
    }
}
