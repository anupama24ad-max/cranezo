package com.cranezo.ui.customerhome

import android.app.Application
import com.cranezo.api.isRequestCallSuspendSuccess
import com.cranezo.core.BaseViewModel
import com.cranezo.repository.Repository
import com.cranezo.ui.customerhome.model.CustomerHome
import com.cranezo.ui.customerhome.model.CustomerHomeState
import com.cranezo.utils.AppMethods
import com.cranezo.utils.SessionManager
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/** Customer Home: everything on it comes from POST /home/customer. The last answer is cached so Home shows at once. */
@HiltViewModel
class CustomerHomeViewModel @Inject constructor(
    var app: Application,
    var repo: Repository,
    var sm: SessionManager
) : BaseViewModel(app) {

    private companion object {
        const val CACHE_KEY = "customer_home_cache"
    }

    private val gson = Gson()

    private val _uiState = MutableStateFlow(
        CustomerHomeState(
            home = sm.getData(CACHE_KEY, "").takeIf { it.isNotEmpty() }
                ?.let { runCatching { gson.fromJson(it, CustomerHome::class.java) }.getOrNull() }
        )
    )
    val uiState: StateFlow<CustomerHomeState> = _uiState.asStateFlow()

    /** Called when Home opens and when the app returns to the foreground. */
    fun refresh() {
        runWhenOnline {
            repo.customerHome(AppMethods.getToken(sm)).isRequestCallSuspendSuccess(
                success = { r ->
                    // Always what the last answer contained: old banners do not stay
                    r?.data?.let { home ->
                        sm.setData(CACHE_KEY, gson.toJson(home))
                        _uiState.update { it.copy(home = home, loaded = true) }
                    }
                },
                failure = { _, _, _ -> _uiState.update { it.copy(loaded = true) } }
            )
        }
    }
}
