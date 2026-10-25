package com.cranezo.app


import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.cranezo.utils.AppStrings
import com.cranezo.utils.SessionManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Cranezo : Application(), Application.ActivityLifecycleCallbacks {

    lateinit var sm: SessionManager

    companion object {
        lateinit var instance: Cranezo
        var context: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
//        FirebaseApp.initializeApp(this)
        instance = this
        sm = SessionManager(this)

        val userId = sm.getData(AppStrings.SessionValues.userId, "")
        //  val channel = sm.getData(AppStrings.SessionValues.tripChannel, "")
    }


    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
