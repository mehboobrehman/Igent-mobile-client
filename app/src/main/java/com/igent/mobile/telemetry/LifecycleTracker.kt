package com.igent.mobile.telemetry

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.igent.mobile.util.AgentLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LifecycleTracker @Inject constructor(
    private val logger: AgentLogger
) : Application.ActivityLifecycleCallbacks {

    private var currentActivityName: String = "None"

    fun getCurrentActivity(): String = currentActivityName

    override fun onActivityResumed(activity: Activity) {
        currentActivityName = activity.javaClass.simpleName
        logger.i("LifecycleTracker", "Activity Resumed: $currentActivityName")
    }

    override fun onActivityPaused(activity: Activity) {
        logger.i("LifecycleTracker", "Activity Paused: ${activity.javaClass.simpleName}")
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
