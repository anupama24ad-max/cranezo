package com.cranezo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cranezo.interfaces.SnackbarStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

// ----- State -----

/**
 * The one message bar of the app (success / error / info), shown at the bottom of every screen.
 * The class keeps its old name so every caller (BaseViewModel.showError / showSuccess) works unchanged.
 */
@Stable
class TopBannerHostState {
    var current by mutableStateOf<BannerData?>(null)
        private set

    suspend fun show(message: String, status: SnackbarStatus, autoHideMs: Long = 1000) {
        if (message.isBlank()) return
        val data = BannerData(message, status)
        current = data
        if (autoHideMs > 0) {
            // long enough to read: errors and warnings stay a little longer
            val minimum = if (status == SnackbarStatus.error || status == SnackbarStatus.warning) 3500L else 2500L
            delay(maxOf(autoHideMs, minimum))
            if (current === data) current = null
        }
    }

    fun dismiss() {
        current = null
    }
}

data class BannerData(val message: String, val status: SnackbarStatus)

@Composable
fun rememberTopBannerHostState() = remember { TopBannerHostState() }

/**
 * Show a message from anywhere (composables, helpers) without a view model.
 * Replaces Android toasts so every message looks the same.
 */
object AppMessages {
    private val _events = MutableSharedFlow<BannerData>(extraBufferCapacity = 4)
    val events: SharedFlow<BannerData> = _events

    fun show(message: String, status: SnackbarStatus = SnackbarStatus.informative) {
        _events.tryEmit(BannerData(message, status))
    }

    fun error(message: String) = show(message, SnackbarStatus.error)
}

private data class BannerVisuals(val accent: Color, val icon: ImageVector)

private fun bannerVisuals(status: SnackbarStatus): BannerVisuals =
    when (status) {
        SnackbarStatus.success -> BannerVisuals(Color(0xFF22C55E), Icons.Filled.CheckCircle)
        SnackbarStatus.informative -> BannerVisuals(Color(0xFF3B82F6), Icons.Filled.Info)
        SnackbarStatus.delete -> BannerVisuals(Color(0xFFEF4444), Icons.Filled.Info)
        SnackbarStatus.warning -> BannerVisuals(Color(0xFFF59E0B), Icons.Filled.Warning)
        SnackbarStatus.error -> BannerVisuals(Color(0xFFEF4444), Icons.Filled.Error)
    }

/**
 * Compact bar near the bottom (above the bottom buttons), fades in, tap to close.
 * Dark surface with a coloured icon, the same in light and dark mode.
 */
@Composable
fun TopBannerHost(
    hostState: TopBannerHostState,
    modifier: Modifier = Modifier
) {
    val data = hostState.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            // sits just above the usual bottom button / bottom navigation
            .padding(start = 16.dp, end = 16.dp, bottom = 88.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = data != null,
            enter = slideInVertically { it / 2 } + fadeIn(),
            exit = fadeOut()
        ) {
            if (data != null) {
                val visuals = bannerVisuals(data.status)
                val shape = RoundedCornerShape(14.dp)
                Row(
                    modifier = Modifier
                        .widthIn(max = 520.dp)
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .shadow(8.dp, shape)
                        .clip(shape)
                        .background(Color(0xFF1F2937))
                        .border(1.dp, visuals.accent.copy(alpha = 0.55f), shape)
                        .clickable { hostState.dismiss() }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(visuals.icon, contentDescription = null, tint = visuals.accent, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = data.message,
                        color = Color.White,
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 19.sp),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Filled.Close, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
