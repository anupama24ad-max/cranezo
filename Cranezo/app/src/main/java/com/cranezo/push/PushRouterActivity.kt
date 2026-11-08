package com.cranezo.push

import android.app.Activity
import android.content.Intent
import android.os.Bundle

/**
 * Opens the right screen when a push is tapped while the app is in the background or closed.
 *
 * Android only shows the push; when it is tapped it starts whatever the push says. The server asks for
 * [ACTION], so this invisible screen gets the push's data, opens Home and then the push's own screen
 * (the same routing as tapping a notice in the Notifications list), and closes itself.
 * Without it a tap just brings the app to the front, on whatever screen it was.
 */
class PushRouterActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        route(intent)
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        route(intent)
        finish()
    }

    private fun route(intent: Intent?) {
        val screens = PushNotifications.targetIntents(this, intent?.extras)
        // Home is reused when it is already open (everything above it closes), so Back lands on it
        screens.firstOrNull()?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        runCatching { startActivities(screens.toTypedArray()) }
    }

    companion object {
        /** The click action the server puts on every push (android.notification.clickAction) */
        const val ACTION = "com.cranezo.OPEN_PUSH"
    }
}
