package com.cranezo.ui.chooselanguage

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.cranezo.api.isRequestCallSuspendSuccess
import com.cranezo.core.BaseViewModel
import com.cranezo.repository.Repository
import com.cranezo.ui.chooselanguage.model.ChooseLanguageApiResponseItem
import com.cranezo.ui.chooselanguage.model.ChooseLanguageEffect
import com.cranezo.ui.chooselanguage.model.ChooseLanguageIntent
import com.cranezo.ui.chooselanguage.model.ChooseLanguageUiState
import com.cranezo.ui.chooselanguage.model.LanguageItem
import com.cranezo.utils.AppMethods
import com.cranezo.utils.AppStrings
import com.cranezo.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class ChooseLanguageViewModel @Inject constructor(
    var app: Application,
    var repo: Repository,
    var sm: SessionManager
) : BaseViewModel(app) {

    private val TAG = "ChooseLanguageViewModel"

    private val _uiState = MutableStateFlow(
        ChooseLanguageUiState(
            selectedLanguageId = sm.getData(
                AppStrings.SessionValues.languageId,
                AppStrings.Constants.defaultLanguageId
            )
        )
    )
    val uiState: StateFlow<ChooseLanguageUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ChooseLanguageEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: ChooseLanguageIntent) {
        when (intent) {
            ChooseLanguageIntent.LoadLanguages -> {
                if (_uiState.value.languages.isEmpty()) dropDownApi()
            }

            is ChooseLanguageIntent.SelectLanguage -> {
                _uiState.update { it.copy(selectedLanguageId = intent.languageId) }
            }

            ChooseLanguageIntent.Continue -> {
                val languageId = _uiState.value.selectedLanguageId ?: return
                sm.setData(AppStrings.SessionValues.languageId, languageId)
                viewModelScope.launch { _effect.send(ChooseLanguageEffect.NavigateToLogin) }
            }
        }
    }

    private fun dropDownApi() {
        val jsonObj = JSONObject()
        jsonObj.put(AppStrings.InputData.type, AppStrings.DropDown.language)
        viewModelScope.launch {
            runWhenOnline {
                setIsLoading(true)
                repo.dropDownApi(AppMethods.getToken(sm, true), jsonObj).isRequestCallSuspendSuccess(
                    success = { response ->
                        val languages = response?.data.orEmpty().mapNotNull { it.toLanguageItem() }
                        _uiState.update { currentState ->
                            currentState.copy(
                                languages = languages,
                                selectedLanguageId = currentState.selectedLanguageId
                                    ?.takeIf { id -> languages.any { it.id == id } }
                                    ?: languages.firstOrNull()?.id
                            )
                        }
                    },
                    failure = { body, errorType, message ->
                        Log.e(TAG, "dropDownApi: message --> ${message}")
                        showError(message.toString())
                    })
                setIsLoading(false)
            }
        }
    }

    private fun ChooseLanguageApiResponseItem.toLanguageItem(): LanguageItem? {
        val languageId = id ?: return null
        val languageName = dropdownName.orEmpty()
        return LanguageItem(
            id = languageId,
            badge = languageBadges[languageId] ?: languageName.take(1),
            name = languageName,
            subtitle = dropdownDescription.orEmpty()
        )
    }

    companion object {
        // Badge letters are not part of the dropdown API, so they are mapped by language id
        private val languageBadges = mapOf(1 to "Aa", 2 to "ఆ", 3 to "आ")
    }
}
