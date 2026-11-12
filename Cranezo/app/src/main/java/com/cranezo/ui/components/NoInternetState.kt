package com.cranezo.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.cranezo.R
import com.cranezo.ui.theme.*
import kotlinx.coroutines.delay

/**
 * "No internet" page with a moving picture: rings spread out from the Wi-Fi icon like a signal being searched for.
 * [onRetry] is the screen's own reload. The button shows "Looking for a connection…" for a moment after a tap,
 * so the person sees that something happens even when the network is still off.
 */
@Composable
fun NoInternetState(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val accent = Red
    var checking by remember { mutableStateOf(false) }
    LaunchedEffect(checking) {
        if (checking) {
            delay(1400)
            checking = false
        }
    }

    Column(
        modifier = modifier.fillMaxWidth().padding(top = dimen_40, bottom = dimen_24, start = dimen_20, end = dimen_20),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SignalRings(accent = accent)
        Spacer(modifier = Modifier.height(dimen_16))
        Textview(stringResource(R.string.net_off_title), size = font_18, color = appColors.textPrimary, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(dimen_6))
        Textview(stringResource(R.string.net_off_body), size = font_14, color = appColors.textSecondary, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(dimen_20))
        CommonGradientButton(
            text = stringResource(if (checking) R.string.net_off_checking else R.string.net_off_retry),
            enabled = !checking,
            onClick = {
                checking = true
                onRetry()
            }
        )
    }
}

/** Three rings that grow and fade one after the other around a Wi-Fi-off icon */
@Composable
private fun SignalRings(accent: androidx.compose.ui.graphics.Color) {
    val transition = rememberInfiniteTransition(label = "rings")
    Box(modifier = Modifier.size(dimen_140), contentAlignment = Alignment.Center) {
        for (i in 0 until 3) {
            val progress by transition.animateFloat(
                initialValue = 0f, targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2400, delayMillis = i * 800, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "ring$i"
            )
            Box(
                modifier = Modifier
                    .size(dimen_80)
                    .scale(0.7f + progress * 0.9f)
                    .alpha((1f - progress) * 0.55f)
                    .border(dimen_2, accent, CircleShape)
            )
        }
        Box(
            modifier = Modifier.size(dimen_72).clip(CircleShape).background(accent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            val bob by transition.animateFloat(
                initialValue = 0.7f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "bob"
            )
            Icon(Icons.Default.WifiOff, contentDescription = null, tint = accent, modifier = Modifier.size(dimen_36).alpha(bob))
        }
    }
}
