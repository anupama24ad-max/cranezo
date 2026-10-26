package com.cranezo.ui.chooselanguage

import android.os.Bundle
import androidx.compose.runtime.Composable
import com.cranezo.core.composecore.BaseVMComposeActivity
import com.cranezo.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChooseLanguageActivity : BaseVMComposeActivity<ChooseLanguageViewModel>(ChooseLanguageViewModel::class.java) {

    @Inject
    lateinit var sm: SessionManager

    @Composable
    override fun Content(vm: ChooseLanguageViewModel) {
        ChooseLanguageScreen(vm = vm)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUi()
    }

    private fun initUi() {
    }
}
