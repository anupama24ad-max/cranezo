package com.cranezo.core.composecore

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.cranezo.R
import com.cranezo.core.BaseViewModel
import com.cranezo.core.NetworkState
import com.cranezo.interfaces.BannerEvent
import com.cranezo.ui.components.TopBannerHost
import com.cranezo.ui.components.dialogUtils.SessionExpiredDialog
import com.cranezo.ui.components.rememberTopBannerHostState
import com.cranezo.ui.theme.CranezoTheme
import com.cranezo.utils.SessionManager
import com.cranezo.utils.networkCheck.NetNoticeEvent
import com.cranezo.utils.networkCheck.NetNoticeHost
import com.cranezo.utils.networkCheck.rememberNetNoticeHostState
import com.cranezo.utils.objects.SessionManagerEvent


abstract class BaseVMComposeActivity<VM : BaseViewModel>(
    private val vmClass: Class<VM>
) : BaseComposeActivity() {

    lateinit var viewModel: VM

    @Composable
    abstract fun Content(vm: VM)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[vmClass]

        setContent {

            val dialogState by SessionManagerEvent.dialogState  // this is MutableState<Pair<Boolean,String>>
            val showSessionDialog = dialogState.first


            CranezoTheme {
                val isLoading by viewModel.isLoading.observeAsState(false)
                val netState by viewModel.networkAlerts
                    .collectAsState(initial = NetworkState.GoodConnection)  // you already have this. :contentReference[oaicite:0]{index=0}
                val isUiBlocked by viewModel.isUiBlocked.observeAsState(false)

                // Close-now / reopen-on-next-emission behavior:
                var snoozed by remember { mutableStateOf(false) }
                LaunchedEffect(netState) { snoozed = false } // re-enable dialog on the next result

                val showDialog =
                    !snoozed && (netState is NetworkState.NoConnection || netState is NetworkState.SlowConnection)
                // ---- Banner host (shared) ----
                val bannerHost = rememberTopBannerHostState()
                LaunchedEffect(Unit) {
                    viewModel.bannerEvents.collect { ev ->
                        when (ev) {
                            is BannerEvent.Show -> bannerHost.show(
                                ev.message,
                                ev.status,
                                ev.autoHideMs
                            )

                            BannerEvent.Dismiss -> bannerHost.dismiss()
                        }
                    }
                }

                val netNoticeHost = rememberNetNoticeHostState()

                LaunchedEffect(Unit) {
                    viewModel.netNoticeEvents.collect { ev ->
                        when (ev) {
                            is NetNoticeEvent.Show -> netNoticeHost.show(ev.notice)
                            is NetNoticeEvent.Dismiss -> netNoticeHost.dismiss()
                        }
                    }
                }
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(White)
//                        .statusBarsPadding()
//                        .navigationBarsPadding()
                ) {
                    Content(viewModel)
                    if (isLoading) LoadingOverlay()

                    if (showDialog) {
                        AlertDialog(
                            onDismissRequest = {
                                snoozed = true
                                viewModel.cancelNetworkRequest = true   // stop checking
                            },
                            title = { Text(if (netState is NetworkState.NoConnection) "Network disconnected" else "Slow connection") },
                            text = {
                                Text(
                                    if (netState is NetworkState.NoConnection)
                                        "Please check your connection and retry."
                                    else
                                        "Your internet seems slow. You can retry or cancel."
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    // Retry: close now, then start a fresh check; if still poor on next emission, dialog re-opens
                                    snoozed = true
                                    viewModel.cancelNetworkRequest = false
                                    viewModel.validateNetwork(onNetworkConnected = null)
                                }) { Text("Retry") }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    // Cancel: close now and stop checks (dialog won’t re-open)
                                    snoozed = true
                                    viewModel.cancelNetworkRequest = true
                                }) { Text("Cancel") }
                            }
                        )
                    }

                    // Transparent blocking overlay when UI is blocked
                    if (isUiBlocked) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(Transparent) // semi-transparent overlay
                                .pointerInput(Unit) {} // absorbs clicks & touches
                        )
                    }
                    // ---- Always pinned to TOP for all screens ----
                    TopBannerHost(
                        hostState = bannerHost,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                    NetNoticeHost(
                        hostState = netNoticeHost,
                        modifier = Modifier.align(Alignment.TopCenter),
                        onDismiss = { viewModel.onUserDismissNetNotice() }
                    )


                }
            }

            SessionExpiredDialog(
                showDialog = showSessionDialog,
                message = "You have logged in from another device. For security reasons, you have been logged out of this session.",
                onConfirm = {
                    SessionManager(applicationContext).clearSession()
                /*    startActivity(
                        Intent(this, LoginActivity::class.java).apply {
                            flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                    )*/


                },
                onDismiss = { SessionManagerEvent.hide() }
            )


        }

    }
}

@Composable
fun LoadingOverlay() {
    val context = LocalContext.current

    Box(
        Modifier
            .fillMaxSize()
            .background(Transparent)
            .clickable(false) {

            }, contentAlignment = Alignment.Center
    ) {
      /*  LottiePlayer(
            lottieRes = R.raw.app_loader,
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.Center),
            loop = true,
            speed = 1f
        )*/
    }
}


