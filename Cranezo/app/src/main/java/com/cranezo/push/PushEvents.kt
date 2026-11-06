package com.cranezo.push

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Every push that arrives while the app is open, so the screen on view can reload itself:
 * a job screen moves to its new stage (crane arrived, job started, completed...), the request
 * screen moves to "pay the advance" when an owner accepts, lists and home numbers update.
 */
object PushEvents {
    private val _updates = MutableSharedFlow<Map<String, String>>(extraBufferCapacity = 8)
    val updates = _updates.asSharedFlow()

    fun emit(data: Map<String, String>) {
        _updates.tryEmit(data)
    }
}

/** Runs [onPush] for every push while this screen is in front */
@Composable
fun OnPush(onPush: (Map<String, String>) -> Unit) {
    val latest by rememberUpdatedState(onPush)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            PushEvents.updates.collect { latest(it) }
        }
    }
}
