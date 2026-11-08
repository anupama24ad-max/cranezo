package com.cranezo.push

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import com.cranezo.R
import com.cranezo.ui.booking.BookingActivity
import com.cranezo.ui.booking.BookingRoutes
import com.cranezo.ui.mainhome.MainHomeActivity
import com.cranezo.ui.operator.OperatorActivity
import com.cranezo.ui.ownerjobs.OwnerJobActivity
import com.cranezo.ui.splash.SplashActivity
import com.cranezo.ui.support.SupportActivity
import com.cranezo.ui.support.SupportRoutes
import com.cranezo.utils.SessionManager

/**
 * Everything about showing a push and opening the right screen from it.
 *
 * The server sends `type`, `screen` and `booking_id` in the data of every push
 * (see backend notification.service.js and constants/notificationMeta.js).
 */
object PushNotifications {

    /** Loud: a new request, the crane arrived, the customer code, extra time, breakdown, emergency. */
    const val CHANNEL_URGENT = "cranezo_urgent"

    /** Normal: everything else (payments, receipts, announcements, support). */
    const val CHANNEL_UPDATES = "cranezo_updates"

    /** Owner buzzer: a new crane / booking request rings with buzzer_tone (res/raw) for up to 30 seconds */
    const val CHANNEL_BUZZER = "cranezo_buzzer"
    private const val BUZZER_RING_MS = 30_000L

    private fun buzzerUri(context: Context): android.net.Uri =
        android.net.Uri.parse("android.resource://${context.packageName}/${R.raw.buzzer_tone}")

    private val URGENT_TYPES = setOf(
        "crane_approved", "crane_rejected", "document_rejected", "booking_request", "job_assigned", "operator_arrived", "completion_requested",
        "extension_requested", "breakdown_reported", "emergency"
    )

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val urgent = NotificationChannel(
            CHANNEL_URGENT, context.getString(R.string.push_channel_urgent), NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.push_channel_urgent_desc)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 400, 200, 400)
        }
        val updates = NotificationChannel(
            CHANNEL_UPDATES, context.getString(R.string.push_channel_updates), NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = context.getString(R.string.push_channel_updates_desc) }
        // a channel's sound cannot change after it is created, so the buzzer has its own channel
        val buzzer = NotificationChannel(
            CHANNEL_BUZZER, context.getString(R.string.push_channel_buzzer), NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.push_channel_buzzer_desc)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 800, 400, 800, 400, 800)
            setSound(
                buzzerUri(context),
                android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }
        manager.createNotificationChannels(listOf(urgent, updates, buzzer))
    }

    fun canPost(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    /** Shows a push that arrived while the app was open (when closed, Android shows it by itself). */
    fun show(context: Context, title: String, body: String, data: Map<String, String>) {
        if (!canPost(context)) return
        val type = data["type"].orEmpty()
        val buzzer = data["buzzer"] == "true"
        val channel = when {
            buzzer -> CHANNEL_BUZZER
            type in URGENT_TYPES -> CHANNEL_URGENT
            else -> CHANNEL_UPDATES
        }
        val requestCode = (data["booking_id"] ?: type).hashCode()
        val extras = Bundle().apply { data.forEach { (k, v) -> putString(k, v) } }

        val pending = TaskStackBuilder.create(context)
            .apply { targetIntents(context, extras).forEach { addNextIntent(it) } }
            .getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setColor(ContextCompat.getColor(context, R.color.push_accent))
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(if (channel == CHANNEL_URGENT || buzzer) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_DEFAULT)
            .setSound(if (buzzer) buzzerUri(context) else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setAutoCancel(true)
            .setContentIntent(pending)
        if (buzzer) {
            // rings like a call until the owner opens it, for at most 30 seconds
            builder.setCategory(NotificationCompat.CATEGORY_CALL)
                .setTimeoutAfter(BUZZER_RING_MS)
                .setVibrate(longArrayOf(0, 800, 400, 800, 400, 800))
        }
        val notification = builder.build()
        if (buzzer) notification.flags = notification.flags or android.app.Notification.FLAG_INSISTENT
        runCatching { NotificationManagerCompat.from(context).notify(requestCode, notification) }
    }

    /**
     * The screens to open for a tapped push: Home first (so Back lands there), then the push's own screen.
     * Not signed in: the splash, which sends the user to sign in.
     */
    fun targetIntents(context: Context, extras: Bundle?): List<Intent> {
        val sm = SessionManager(context)
        if (!sm.isUserLoggedIn()) return listOf(Intent(context, SplashActivity::class.java))
        val home = Intent(context, MainHomeActivity::class.java)
        val target = targetIntent(context, extras, sm.getUserRole())
        return listOfNotNull(home, target)
    }

    /** Same routing as tapping the item in the Notifications list (SupportActivity). */
    fun targetIntent(context: Context, extras: Bundle?, role: String): Intent? {
        extras ?: return null
        val screen = extras.getString("screen").orEmpty()
        val bookingId = extras.getString("booking_id")
        return when (screen) {
            "booking", "track", "receipt", "rate" -> bookingId?.let {
                when (role) {
                    "CRANE_OWNER" -> OwnerJobActivity.intent(context, it)
                    "OPERATOR" -> OperatorActivity.intent(context, it)
                    else -> BookingActivity.bookingIntent(context, it)
                }
            }
            "owner_job" -> bookingId?.let { OwnerJobActivity.intent(context, it) }
            // the customer paid the advance: straight to the one screen that assigns the operator
            "owner_assign" -> bookingId?.let { com.cranezo.ui.finance.FinanceActivity.intent(context, com.cranezo.ui.finance.FinanceRoutes.ASSIGN, it) }
            "operator_job" -> bookingId?.let { OperatorActivity.intent(context, it) }
            "announcement" -> SupportActivity.intent(context, SupportRoutes.NOTIFICATIONS)
            // the support request itself (its chat), or the list when the id is missing
            "support_ticket" -> extras.getString("ticket_id")?.let { SupportActivity.ticketIntent(context, it) }
                ?: SupportActivity.intent(context, SupportRoutes.TICKETS)
            // complaints, operator job requests, commission and settlements
            "complaint" -> extras.getString("complaint_id")?.let {
                com.cranezo.ui.finance.FinanceActivity.intent(context, com.cranezo.ui.finance.FinanceRoutes.COMPLAINT, it)
            } ?: com.cranezo.ui.finance.FinanceActivity.intent(context, com.cranezo.ui.finance.FinanceRoutes.COMPLAINTS)
            "owner_job_requests" -> com.cranezo.ui.finance.FinanceActivity.intent(context, com.cranezo.ui.finance.FinanceRoutes.JOB_REQUESTS)
            "operator_job_requests" -> com.cranezo.ui.finance.FinanceActivity.intent(context, com.cranezo.ui.finance.FinanceRoutes.MY_REQUESTS)
            "operator_payments" -> com.cranezo.ui.finance.FinanceActivity.intent(context, com.cranezo.ui.finance.FinanceRoutes.MY_PAYMENTS)
            "owner_settlements" -> com.cranezo.ui.finance.FinanceActivity.intent(context, com.cranezo.ui.finance.FinanceRoutes.PLATFORM)
            // Rapido-style requests: the owner's list of requests near them; the customer's request screen
            "owner_crane_requests" -> com.cranezo.ui.finance.FinanceActivity.intent(context, com.cranezo.ui.finance.FinanceRoutes.CRANE_REQUESTS)
            "crane_request" -> BookingActivity.intent(context, BookingRoutes.REQUEST)
            "subscription" -> com.cranezo.ui.subscription.SubscriptionActivity.intent(context)
            // admin decisions: the crane that was approved or rejected, or the profile documents
            "owner_crane" -> extras.getString("crane_id")?.let { com.cranezo.ui.cranes.CranesActivity.detailsIntent(context, it) }
            "documents" -> com.cranezo.ui.myprofile.MyProfileActivity.intent(context, com.cranezo.ui.myprofile.ProfileRoutes.DOCUMENTS)
            // "owner_request": the request popup lives on Home, which is already opened
            else -> null
        }
    }
}
