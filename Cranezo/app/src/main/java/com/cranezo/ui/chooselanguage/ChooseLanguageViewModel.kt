package com.cranezo.ui.chooselanguage

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import com.cranezo.core.BaseViewModel
import com.cranezo.repository.Repository
import com.cranezo.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class LanguageItem(
    val code: String,
    val badge: String,
    val name: String,
    val subtitle: String
)

@HiltViewModel
class ChooseLanguageViewModel @Inject constructor(
    var app: Application,
    var repo: Repository,
    var sm: SessionManager
) : BaseViewModel(app) {

    val selectedLanguage = mutableStateOf("en")

    val languageList = listOf(
        LanguageItem(
            code = "en",
            badge = "Aa",
            name = "English",
            subtitle = "Continue in English"
        ),
        LanguageItem(
            code = "te",
            badge = "ఆ",
            name = "తెలుగు",
            subtitle = "తెలుగులో కొనసాగించండి"
        ),
        LanguageItem(
            code = "hi",
            badge = "आ",
            name = "हिन्दी",
            subtitle = "हिंदी में जारी रखें"
        )
    )

    fun onLanguageSelected(code: String) {
        selectedLanguage.value = code
        sm.setData("selected_language", code)
    }
}
