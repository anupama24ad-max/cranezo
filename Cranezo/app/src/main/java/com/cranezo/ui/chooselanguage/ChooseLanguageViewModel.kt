package com.cranezo.ui.chooselanguage

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.cranezo.api.isRequestCallSuspendSuccess
import com.cranezo.core.BaseViewModel
import com.cranezo.repository.Repository
import com.cranezo.utils.AppMethods
import com.cranezo.utils.AppStrings
import com.cranezo.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
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

    private val TAG = "ChooseLanguageViewModel"

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

    fun dropDownApi(
    ) {
        val jsonObj = JSONObject()
        jsonObj.put(AppStrings.InputData.type, AppStrings.DropDown.language)
        viewModelScope.launch {
            runWhenOnline {
                setIsLoading(true)
                repo.dropDownApi(AppMethods.getToken(sm, true), jsonObj).isRequestCallSuspendSuccess(
                    success = { response ->
                        /*_uiState.update { currentState ->
                            currentState.copy(
                                homeData = response?.data,
                                upcomingAuctions = response?.data?.upcoming_auction ?: emptyList(),
                                latestUpcomingChit = response?.data?.latest_upcoming_chit
                            )
                        }*/
                    },
                    failure = { body, errorType, message ->
                        Log.e(TAG, "dropDownApi: message --> ${message}")
                        showError(message.toString())
                    })
                setIsLoading(false)
            }
        }
    }
}
