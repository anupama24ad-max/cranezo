package com.cranezo.utils.networkCheck

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// NetNoticeHost.kt
@Stable
class NetNoticeHostState {
    private val _current = MutableStateFlow<NetNotice?>(null)
    val current: StateFlow<NetNotice?> = _current
    fun show(notice: NetNotice) { _current.value = notice }
    fun dismiss() { _current.value = null }
}

@Composable
fun rememberNetNoticeHostState() = remember { NetNoticeHostState() }

@Composable
fun NetNoticeHost(
    hostState: NetNoticeHostState,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    val notice by hostState.current.collectAsState(initial = null)

    AnimatedVisibility(
        visible = notice != null,
        enter = slideInVertically { -it } + fadeIn(),
        exit  = slideOutVertically { -it } + fadeOut(),
        modifier = modifier.fillMaxWidth()
    ) {
        notice?.let { n ->
            // AUTO HIDE logic
            LaunchedEffect(n) {
                if (n.autoHideMs != null && n.autoHideMs > 0) {
                    kotlinx.coroutines.delay(n.autoHideMs)
                    onDismiss()
                    hostState.dismiss()
                }
            }

            val bg = when (n.type) {
                is NetNoticeType.Offline -> Color(0xFFB00020)
                is NetNoticeType.Slow    -> Color(0xFFFFA000)
                is NetNoticeType.Online  -> Color(0xFF09FF14) // green
            }

            Box(
                Modifier.fillMaxWidth().background(bg)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        n.message,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    if (n.sticky) { // Back online banner is not sticky, so no Dismiss button
                        Text(
                            "Dismiss",
                            color = Color.White,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onDismiss(); hostState.dismiss() }
                                .padding(6.dp)
                        )
                    }
                }
            }
        }
    }
}
