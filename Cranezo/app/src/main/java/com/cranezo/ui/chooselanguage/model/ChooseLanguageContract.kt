package com.cranezo.ui.chooselanguage.model

data class LanguageItem(
    val id: Int,
    val badge: String,
    val name: String,
    val subtitle: String
)

data class ChooseLanguageUiState(
    val languages: List<LanguageItem> = emptyList(),
    val selectedLanguageId: Int? = null
)

sealed interface ChooseLanguageIntent {
    data object LoadLanguages : ChooseLanguageIntent
    data class SelectLanguage(val languageId: Int) : ChooseLanguageIntent
    data object Continue : ChooseLanguageIntent
}

sealed interface ChooseLanguageEffect {
    data object NavigateToLogin : ChooseLanguageEffect
}
