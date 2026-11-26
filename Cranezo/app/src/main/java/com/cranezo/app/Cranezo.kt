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
class Cranezo : Application(), Application.ActivityLifecycleCallbacks, coil.ImageLoaderFactory {

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


    /**
     * Uploaded photos and documents are private on the server: a request for /public/uploads is answered only when it carries
     * the login token. Every image the app shows goes through this loader, so the token is added here.
     */
    override fun newImageLoader(): coil.ImageLoader =
        coil.ImageLoader.Builder(this)
            .okHttpClient {
                okhttp3.OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request()
                        if (request.url.encodedPath.startsWith("/public/uploads")) {
                            val token = sm.getData<String>(AppStrings.SessionValues.accessToken, "").toString()
                            if (token.isNotBlank()) {
                                val bearer = if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
                                return@addInterceptor chain.proceed(request.newBuilder().header("Authorization", bearer).build())
                            }
                        }
                        chain.proceed(request)
                    }
                    .build()
            }
            .build()

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
