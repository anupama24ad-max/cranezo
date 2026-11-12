package com.cranezo.utils.networkCheck

import com.cranezo.R
import androidx.compose.ui.res.stringResource
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
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
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
        modifier = modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp)
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

            // one dark floating card for every state; the accent colour and the icon say which state it is
            val accent = when (n.type) {
                is NetNoticeType.Offline -> Color(0xFFEF4444)
                is NetNoticeType.Slow    -> Color(0xFFFFA000)
                is NetNoticeType.Online  -> Color(0xFF22C55E)
            }
            val icon = when (n.type) {
                is NetNoticeType.Offline -> Icons.Default.WifiOff
                is NetNoticeType.Slow    -> Icons.Default.NetworkCheck
                is NetNoticeType.Online  -> Icons.Default.Wifi
            }
            // the icon breathes while there is no (or a bad) connection, and stays still once it is back
            val pulse = rememberInfiniteTransition(label = "net")
            val breathe by pulse.animateFloat(
                initialValue = 0.55f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "breathe"
            )
            val alpha = if (n.type is NetNoticeType.Online) 1f else breathe
            val shape = RoundedCornerShape(18.dp)

            Row(
                Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, shape)
                    .clip(shape)
                    .background(Color(0xFF111827))
                    .border(1.dp, accent.copy(alpha = 0.55f), shape)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(accent.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp).alpha(alpha))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(n.message, color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    if (n.type is NetNoticeType.Offline) {
                        Text(
                            stringResource(R.string.net_offline_hint),
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                if (n.sticky) { // Back online banner is not sticky, so no Dismiss button
                    Text(
                        stringResource(R.string.common_dismiss),
                        color = accent,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onDismiss(); hostState.dismiss() }
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}
