package com.cranezo.core

import android.Manifest
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cranezo.interfaces.BannerEvent
import com.cranezo.interfaces.SnackbarStatus
import com.cranezo.utils.networkCheck.NetNotice
import com.cranezo.utils.networkCheck.NetNoticeEvent
import com.cranezo.utils.networkCheck.NetNoticeType
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

// Keep your imports the same

abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {

    val networkLiveData = MutableSharedFlow<Boolean>()
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _networkAlerts = MutableSharedFlow<NetworkState>()
    val networkAlerts = _networkAlerts.asSharedFlow()

    private val _netNoticeEvents = MutableSharedFlow<NetNoticeEvent>(extraBufferCapacity = 1)
    val netNoticeEvents: SharedFlow<NetNoticeEvent> = _netNoticeEvents

    private val _isUiBlocked = MutableLiveData(false)
    val isUiBlocked: LiveData<Boolean> = _isUiBlocked

    private val _bannerEvents = MutableSharedFlow<BannerEvent>(extraBufferCapacity = 1)
    val bannerEvents: SharedFlow<BannerEvent> = _bannerEvents

    fun setIsLoading(isLoading: Boolean?) {
        this._isLoading.postValue(isLoading)
    }

    protected fun showNetNoticeOffline() {
        _netNoticeEvents.tryEmit(
            NetNoticeEvent.Show(
                NetNotice(
                    NetNoticeType.Offline,
                    "No Internet. Auto-retry when online…"
                )
            )
        )
    }


    fun showError(msg: String, autoHideMs: Long = 1000L, blockUi: Boolean = false) =
        showBanner(msg, SnackbarStatus.error, autoHideMs, blockUi)

    fun showSuccess(msg: String? = "", autoHideMs: Long = 1000L, blockUi: Boolean = false) =
        showBanner(msg.toString(), SnackbarStatus.success, autoHideMs, blockUi)


    protected fun showBanner(
        message: String,
        status: SnackbarStatus,
        autoHideMs: Long = 1000L,
        blockUi: Boolean = false
    ) {
        _bannerEvents.tryEmit(BannerEvent.Show(message, status, autoHideMs))

        if (blockUi) {
            _isUiBlocked.postValue(true)
            viewModelScope.launch {
                delay(autoHideMs)
                _isUiBlocked.postValue(false)
            }


        }
    }

    private var lastNoticeType: NetNoticeType? = null
    private var networkCallJob: Job? = null
    var cancelNetworkRequest: Boolean = false

    fun validateNetwork(onNetworkConnected: (suspend () -> Unit)? = null) {
        // cancel any previous run
        networkCallJob?.cancel()
        cancelNetworkRequest = false

        networkCallJob = viewModelScope.launch {
            val result = pollNetworkUntilGoodOrCanceled()
            if (result is NetworkState.GoodConnection && isActive) {
                onNetworkConnected?.invoke()
            }
        }
    }

    fun runWhenOnline(block: suspend () -> Unit) {
        validateNetwork {
            viewModelScope.launch {
                while (isActive) {
                    try {
                        block()
                        break // success
                    } catch (e: java.io.IOException) {
                        // network dropped mid-call → show offline and wait again
                        showNetNoticeOffline()
                        // wait again for connection
                        val waiter = CompletableDeferred<Unit>()
                        validateNetwork { waiter.complete(Unit) }
                        waiter.await()
                        // loop continues
                    }
                }
            }
        }
    }


    private suspend fun pollNetworkUntilGoodOrCanceled(): NetworkState {
        var state = currentNetworkState()
        _networkAlerts.emit(state)

        while (coroutineContext.isActive && !cancelNetworkRequest && state !is NetworkState.GoodConnection) {
            delay(2000)
            state = currentNetworkState()
            _networkAlerts.emit(state)
        }

        if (cancelNetworkRequest) {
            networkCallJob?.cancel()
            cancelNetworkRequest = false
        }
        return state
    }
    fun onUserDismissNetNotice() {
        // allow banner to show again next time we evaluate network state
        lastNoticeType = null
        // also tell UI to hide now (in case it isn't already)
        _netNoticeEvents.tryEmit(NetNoticeEvent.Dismiss)
    }

    private fun currentNetworkState(): NetworkState {
        val cm = getApplication<Application>()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork
        val caps =
            network?.let { cm.getNetworkCapabilities(it) } ?: return NetworkState.NoConnection

        if (!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) return NetworkState.NoConnection
        if (!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) return NetworkState.NoConnection

        val down = caps.linkDownstreamBandwidthKbps
        val up = caps.linkUpstreamBandwidthKbps
        return if (down < 1000 || up < 500) NetworkState.SlowConnection else NetworkState.GoodConnection
    }
}

sealed class NetworkState {
    object NoConnection : NetworkState()
    object SlowConnection : NetworkState()
    object GoodConnection : NetworkState()

}

/*
abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {

    val networkLiveData = MutableSharedFlow<Boolean>()
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading


    private val _networkAlerts = MutableSharedFlow<NetworkState>()
    val networkAlerts = _networkAlerts.asSharedFlow()

    var cancelNetworkRequest = false
    private var networkCallJob: Job? = null


    fun setIsLoading(isLoading: Boolean?) {
        Log.e("TAG", "setIsLoading: value ->${isLoading} ")
        this._isLoading.postValue(isLoading)
    }

    fun validateNetwork(onNetworkConnected: suspend () -> Unit) {
        try {
            networkCallJob = viewModelScope.launch {
                testNetwork()
                if (isActive) {
                    onNetworkConnected()
                }
            }
        } catch (e: TimeoutCancellationException) {
            // Handle timeout
        }
    }

    private suspend fun testNetwork() {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = network?.let { connectivityManager.getNetworkCapabilities(it) }

        val networkState = when {
            networkCapabilities == null -> {
                NetworkState.NoConnection
            }
            !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> {
                NetworkState.NoConnection
            }
            !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) -> {
                // Additional check for internet validation capability
                NetworkState.NoConnection
            }
            else -> {
                val downSpeed = networkCapabilities.linkDownstreamBandwidthKbps
                val upSpeed = networkCapabilities.linkUpstreamBandwidthKbps

                when {
                    downSpeed < 1000 || upSpeed < 500 -> {
                        NetworkState.SlowConnection
                    }
                    else -> {
                        NetworkState.GoodConnection
                    }
                }
            }
        }

        _networkAlerts.emit(networkState)

        if ((networkState == NetworkState.NoConnection || networkState == NetworkState.SlowConnection) && !cancelNetworkRequest) {
            delay(2000)
            testNetwork()
        } else if (cancelNetworkRequest) {
            networkCallJob?.cancel()
            cancelNetworkRequest = false
        }

    }



}

sealed class NetworkState {
    object NoConnection : NetworkState()
    object SlowConnection : NetworkState()
    object GoodConnection : NetworkState()
}*/


/*
abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {

    val networkLiveData = MutableSharedFlow<Boolean>()
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading


    private val _networkAlerts = MutableSharedFlow<Boolean>()
    val networkAlerts = _networkAlerts.asSharedFlow()

    var cancelNetworkRequest = false
    private var networkCallJob: Job? = null


    fun setIsLoading(isLoading: Boolean?) {
        Log.e("TAG", "setIsLoading: value -> ${isLoading} ", )
        this._isLoading.postValue(isLoading)
    }


    init {
    }



    fun validateNetwork(onNetworkConnected: suspend () -> Unit) {
        networkCallJob = viewModelScope.launch {
            testNetwork()
            if (isActive) {
                onNetworkConnected()
            }
        }

    }

    private suspend fun testNetwork() {
        val isConnected = getApplication<Application>().isNetworkAvailable().not()
        _networkAlerts.emit(isConnected)
        if (isConnected && cancelNetworkRequest.not()) {
            delay(timeMillis = 2000)
            testNetwork()
        } else if (cancelNetworkRequest) {
            networkCallJob?.cancel()
            cancelNetworkRequest = false
        }
    }


}*/
