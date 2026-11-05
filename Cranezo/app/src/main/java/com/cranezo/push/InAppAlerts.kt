package com.cranezo.push

import android.os.Bundle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.cranezo.R
import com.cranezo.ui.components.CommonGradientButton
import com.cranezo.ui.components.Textview
import com.cranezo.ui.mainhome.components.IconBadge
import com.cranezo.ui.mainhome.components.SuccessGreen
import com.cranezo.ui.theme.*
import com.cranezo.utils.SessionManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Job updates that arrive while the app is open (crane arrived, completion code, job started, ...) slide up as
 * a bottom sheet over whatever screen is open, with "View details". Nothing jumps to another screen by itself.
 * When no screen is open the normal system notification is shown instead.
 */
object InAppAlerts {
    data class Alert(val title: String, val body: String, val data: Map<String, String>)

    private val SHEET_TYPES = setOf(
        "operator_arrived", "completion_requested", "job_started", "job_completed", "operator_en_route",
        "request_accepted", "advance_paid", "booking_confirmed", "job_assigned", "job_accepted",
        "extension_requested", "extension_approved", "extension_rejected", "payment_received"
    )

    private val _events = MutableSharedFlow<Alert>(extraBufferCapacity = 4)
    val events = _events.asSharedFlow()

    /** True when an open screen took the alert (then no system notification is needed) */
    fun tryShow(title: String, body: String, data: Map<String, String>): Boolean {
        if (data["type"] !in SHEET_TYPES) return false
        if (_events.subscriptionCount.value == 0) return false
        return _events.tryEmit(Alert(title, body, data))
    }
}

/** Hosted by every screen (BaseVMComposeActivity); only the screen in front listens */
@Composable
fun BoxScope.JobAlertHost() {
    var alert by remember { mutableStateOf<InAppAlerts.Alert?>(null) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            InAppAlerts.events.collect { alert = it }
        }
    }
    val context = LocalContext.current
    val current = alert

    // dim the page behind; tapping it closes the sheet
    AnimatedVisibility(visible = current != null, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.matchParentSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { alert = null }
        )
    }
    AnimatedVisibility(
        visible = current != null,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = Modifier.align(Alignment.BottomCenter)
    ) {
        val a = current ?: return@AnimatedVisibility
        val code = a.data["code"]
        val done = a.data["type"] in setOf("operator_arrived", "job_completed", "advance_paid", "booking_confirmed", "payment_received", "request_accepted")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = dimen_24, topEnd = dimen_24))
                .background(appColors.bgBase)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                .navigationBarsPadding()
                .padding(horizontal = dimen_20, vertical = dimen_16),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // grab handle
            Box(modifier = Modifier.width(dimen_40).height(dimen_4).clip(RoundedCornerShape(dimen_4)).background(appColors.textHint))
            Spacer(modifier = Modifier.height(dimen_16))
            IconBadge(
                icon = when {
                    code != null -> Icons.Default.Lock
                    done -> Icons.Default.CheckCircle
                    else -> Icons.Default.Notifications
                },
                accent = if (done) SuccessGreen else AmberPrimary,
                size = dimen_56,
                filled = true
            )
            Spacer(modifier = Modifier.height(dimen_12))
            Textview(a.title, size = font_18, color = appColors.textPrimary, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(dimen_4))
            // the code is shown on its own below, so it is cut from the message
            val body = if (code != null) a.body.replace(code, "").trim().trimEnd(':').trim() else a.body
            Textview(body, size = font_13, color = appColors.textSecondary, textAlign = TextAlign.Center)
            if (code != null) {
                Spacer(modifier = Modifier.height(dimen_16))
                Row(horizontalArrangement = Arrangement.spacedBy(dimen_8)) {
                    code.forEach { ch ->
                        Box(
                            modifier = Modifier
                                .size(dimen_44)
                                .clip(RoundedCornerShape(dimen_10))
                                .background(AmberPrimary.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Textview(ch.toString(), size = font_22, color = appColors.textPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(dimen_8))
                Textview(stringResource(R.string.ia_share_code), size = font_12, color = appColors.textSecondary, textAlign = TextAlign.Center)
            }
            Spacer(modifier = Modifier.height(dimen_20))
            CommonGradientButton(
                text = stringResource(R.string.ia_view_details),
                onClick = {
                    alert = null
                    val extras = Bundle().apply { a.data.forEach { (k, v) -> putString(k, v) } }
                    val role = SessionManager(context).getUserRole()
                    PushNotifications.targetIntent(context, extras, role)?.let { runCatching { context.startActivity(it) } }
                }
            )
            Spacer(modifier = Modifier.height(dimen_8))
            Textview(
                stringResource(R.string.ia_close), size = font_14, color = appColors.textSecondary, fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().clickable { alert = null }.padding(vertical = dimen_10)
            )
        }
    }
}
