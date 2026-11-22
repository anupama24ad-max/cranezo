package com.cranezo.ui.mainhome.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.cranezo.R
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.Textview
import com.cranezo.ui.subscription.SubscriptionActivity
import com.cranezo.ui.theme.*

/**
 * Shown in place of every owner tab (except Profile) once the subscription has expired: the owner can do nothing
 * but renew. Coming back from the plan screen asks for the state again, so the tabs open as soon as the payment went through.
 */
@Composable
fun SubscriptionLockedScreen(onRefresh: () -> Unit) {
    val context = LocalContext.current
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { onRefresh() }
    Box(modifier = Modifier.fillMaxSize().padding(horizontal = dimen_24), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(
                modifier = Modifier.size(dimen_80).clip(CircleShape).background(Red.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Lock, contentDescription = null, tint = Red, modifier = Modifier.size(dimen_40)) }
            Spacer(modifier = Modifier.height(dimen_16))
            Textview(stringResource(R.string.sub_locked_title), size = font_20, color = appColors.textPrimary, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(dimen_8))
            Textview(stringResource(R.string.sub_locked_msg), size = font_13, color = appColors.textSecondary, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(dimen_24))
            CommonGradientButton(
                text = stringResource(R.string.sub_locked_button),
                onClick = { context.startActivity(SubscriptionActivity.intent(context)) }
            )
        }
    }
}
