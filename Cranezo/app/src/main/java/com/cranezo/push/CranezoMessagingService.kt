package com.cranezo.push

import com.cranezo.repository.Repository
import com.cranezo.utils.AppMethods
import com.cranezo.utils.SessionManager
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

/** Receives pushes from Firebase Cloud Messaging and keeps this phone's token on the server. */
@AndroidEntryPoint
class CranezoMessagingService : FirebaseMessagingService() {

    @Inject lateinit var repo: Repository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Firebase gave this phone a new address: tell the server (only when someone is signed in). */
    override fun onNewToken(token: String) {
        val sm = SessionManager(applicationContext)
        if (!sm.isUserLoggedIn()) return
        scope.launch { sendToken(repo, sm, token) }
    }

    /** App open: Android does not show the push by itself, so it is shown here. */
    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: return
        val body = message.notification?.body ?: message.data["body"].orEmpty()
        // the screen on view reloads if the push is about what it shows
        PushEvents.emit(message.data)
        // app open: job updates slide up as a sheet on the current screen instead of a system notification
        if (!InAppAlerts.tryShow(title, body, message.data)) PushNotifications.show(applicationContext, title, body, message.data)
        // the bell on the home screen shows the new unread count straight away
        scope.launch { UnreadBadge.refresh(applicationContext) }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        /** POST notifications/register-device { push_token, platform } */
        suspend fun sendToken(repo: Repository, sm: SessionManager, token: String) {
            val body = JSONObject().put("push_token", token).put("platform", "android")
            repo.registerDeviceApi(AppMethods.getToken(sm), body)
        }

        /** Called after sign-in and on every app start, so the server always has the current token. */
        fun syncToken(scope: CoroutineScope, repo: Repository, sm: SessionManager) {
            runCatching {
                FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                    if (!token.isNullOrBlank()) scope.launch(Dispatchers.IO) { sendToken(repo, sm, token) }
                }
            }
        }
    }
}
