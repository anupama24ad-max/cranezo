package com.cranezo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.cranezo.R
import com.cranezo.interfaces.SnackbarStatus
import kotlinx.coroutines.delay

// ----- State -----

@Stable
class TopBannerHostState {
    var current by mutableStateOf<BannerData?>(null)
        private set

    suspend fun show(message: String, status: SnackbarStatus, autoHideMs: Long = 1000) {
        current = BannerData(message, status)
        if (autoHideMs > 0) {
            delay(autoHideMs)
            if (current?.message == message) current = null
        }
    }

    fun dismiss() {
        current = null
    }
}

data class BannerData(val message: String, val status: SnackbarStatus)

@Composable
fun rememberTopBannerHostState() = remember { TopBannerHostState() }

private data class BannerVisuals(val bg: Color, val iconRes: Int)

@Composable
private fun bannerVisuals(status: SnackbarStatus): BannerVisuals =
    when (status) {
        SnackbarStatus.success -> BannerVisuals(Color(0xFF16A34A), R.drawable.ic_launcher_background)
        SnackbarStatus.informative -> BannerVisuals(Color(0xFF111827), R.drawable.ic_launcher_background)
        SnackbarStatus.delete -> BannerVisuals(Color(0xFF111827), R.drawable.ic_launcher_background)
        SnackbarStatus.warning -> BannerVisuals(Color(0xFFF59E0B), R.drawable.ic_launcher_background)
        SnackbarStatus.error -> BannerVisuals(Color(0xFFDC2626), R.drawable.ic_launcher_background)
    }

/** Stays at the TOP; slides in/out from TOP */
@Composable
fun TopBannerHost(
    hostState: TopBannerHostState,
    modifier: Modifier = Modifier
) {
    val data = hostState.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = data != null,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut()
        ) {
            if (data != null) {
                val visuals = bannerVisuals(data.status)

                // --- icon animation state ---
                val scale = remember { Animatable(0.8f) }
                LaunchedEffect(data.message, data.status) {
                    // (optional) wait a moment so it happens after the slide-in starts
                    delay(120)
                    // quick “pop” + settle
                    scale.snapTo(0.8f)
                    scale.animateTo(
                        1.12f,
                        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
                    )
                    scale.animateTo(
                        1f,
                        animationSpec = spring(dampingRatio = 0.55f, stiffness = 500f)
                    )

                }
                // ----------------------------

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .background(visuals.bg)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(visuals.iconRes),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer( // apply animated scale
                                scaleX = scale.value,
                                scaleY = scale.value
                            )
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = data.message,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}