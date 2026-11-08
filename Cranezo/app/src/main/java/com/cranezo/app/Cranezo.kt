package com.cranezo.app


import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.cranezo.utils.AppStrings
import com.cranezo.utils.SessionManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.launch

@HiltAndroidApp
class Cranezo : Application(), Application.ActivityLifecycleCallbacks {

    lateinit var sm: SessionManager

    @javax.inject.Inject
    lateinit var repo: com.cranezo.repository.Repository

    /** How many screens are on the screen: 0 -> 1 means the app was opened or came back from the background */
    private var started = 0

    companion object {
        lateinit var instance: Cranezo
        var context: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        // Push notification channels (urgent job alerts, normal updates)
        com.cranezo.push.PushNotifications.createChannels(this)
        sm = SessionManager(this)
        com.cranezo.ui.theme.AppThemeState.init(this)
        registerActivityLifecycleCallbacks(this)

        val userId = sm.getData(AppStrings.SessionValues.userId, "")
        //  val channel = sm.getData(AppStrings.SessionValues.tripChannel, "")
    }


    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        if (started++ == 0) {
            // force update: asked on every launch and every return from the background
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch { com.cranezo.update.AppUpdate.check(repo) }
        }
    }
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {
        started = (started - 1).coerceAtLeast(0)
    }
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
