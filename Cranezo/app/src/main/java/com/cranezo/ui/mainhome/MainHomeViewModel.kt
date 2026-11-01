package com.cranezo.ui.mainhome

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.cranezo.core.BaseViewModel
import com.cranezo.R
import com.cranezo.api.isRequestCallSuspendSuccess
import com.cranezo.repository.Repository
import com.cranezo.ui.chooserole.UserRole
import com.cranezo.ui.mainhome.model.MainHomeEffect
import com.cranezo.ui.mainhome.model.MainHomeIntent
import com.cranezo.ui.mainhome.model.MainHomeUiState
import com.cranezo.ui.mainhome.model.MyCranesState
import com.cranezo.utils.AppMethods
import org.json.JSONObject
import com.cranezo.ui.theme.AppThemeState
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
import javax.inject.Inject

@HiltViewModel
class MainHomeViewModel @Inject constructor(
    var app: Application,
    var repo: Repository,
    var sm: SessionManager
) : BaseViewModel(app) {

    private val TAG = "MainHomeViewModel"

    private val role = runCatching { UserRole.valueOf(sm.getUserRole()) }
        .getOrDefault(UserRole.CRANE_OWNER)

    private val _uiState = MutableStateFlow(
        MainHomeUiState(
            role = role,
            userName = sm.getData(AppStrings.SessionValues.name, ""),
            memberId = sm.getData(AppStrings.SessionValues.memberId, ""),
            mobileNumber = "${
                sm.getData(AppStrings.SessionValues.countryCode, AppStrings.Constants.defaultCountryCode)
            } ${sm.getData(AppStrings.SessionValues.contactNumber, "")}".trim()
        )
    )
    val uiState: StateFlow<MainHomeUiState> = _uiState.asStateFlow()

    private companion object {
        const val CRANES_PAGE_SIZE = 10
    }

    private val _cranes = MutableStateFlow(MyCranesState())
    val cranesState: StateFlow<MyCranesState> = _cranes.asStateFlow()

    private val _effect = Channel<MainHomeEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        if (role == UserRole.CRANE_OWNER) loadCranes(reset = true)
    }

    fun onIntent(intent: MainHomeIntent) {
        when (intent) {
            MainHomeIntent.RefreshCranes -> loadCranes(reset = true)

            MainHomeIntent.LoadMoreCranes -> loadCranes(reset = false)

            MainHomeIntent.ToggleOnline -> _uiState.update { it.copy(isOnline = !it.isOnline) }

            is MainHomeIntent.ThemeSelected -> AppThemeState.setThemeMode(intent.mode, app)

            MainHomeIntent.Logout -> logout()
        }
    }

    /**
     * POST /owner/getCranes { min, max }. min is the index of the first record and max the index after
     * the last one, so page 1 is 0..10 and page 2 is 10..20. Every crane comes back with its status;
     * there is no status filter.
     */
    private fun loadCranes(reset: Boolean) {
        val current = _cranes.value
        if (current.isLoading || current.isLoadingMore) return
        if (!reset && !current.hasMore) return

        val min = if (reset) 0 else current.nextMin
        _cranes.update {
            if (reset) it.copy(isLoading = true, error = null) else it.copy(isLoadingMore = true)
        }

        runWhenOnline {
            val body = JSONObject().put("min", min).put("max", min + CRANES_PAGE_SIZE)
            repo.getCranesApi(AppMethods.getToken(sm), body).isRequestCallSuspendSuccess(
                success = { response ->
                    val page = response?.data.orEmpty()
                    val paging = response?.pagination
                    _cranes.update {
                        val known = if (reset) emptyList() else it.items
                        it.copy(
                            items = known + page.filter { crane -> known.none { k -> k.id == crane.id } },
                            isLoading = false,
                            isLoadingMore = false,
                            loaded = true,
                            hasMore = paging?.has_more == true,
                            nextMin = paging?.max ?: (min + CRANES_PAGE_SIZE)
                        )
                    }
                },
                failure = { _, _, message ->
                    val text = message?.takeIf { m -> m.isNotBlank() } ?: string(R.string.crane_msg_generic_error)
                    _cranes.update { it.copy(isLoading = false, isLoadingMore = false, loaded = true, error = text) }
                    showError(text)
                })
        }
    }

    private fun logout() {
        // The theme and language are device preferences, so they survive a logout
        val theme = sm.getAppTheme()
        val languageId = sm.getData(AppStrings.SessionValues.languageId, AppStrings.Constants.defaultLanguageId)
        sm.clearSession()
        sm.setAppTheme(theme)
        sm.setData(AppStrings.SessionValues.languageId, languageId)
        viewModelScope.launch { _effect.send(MainHomeEffect.NavigateToLogin) }
    }
}
