package org.listenbrainz.shared.permission

import android.app.Activity
import android.app.Application
import android.os.Bundle
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.lang.ref.WeakReference

internal object CurrentActivityTracker : Application.ActivityLifecycleCallbacks {

    private var current: WeakReference<Activity>? = null

    val resumed: SharedFlow<Unit>
        field = MutableSharedFlow(extraBufferCapacity = 1)

    val activity: Activity? get() = current?.get()

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        current = WeakReference(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        current = WeakReference(activity)
        resumed.tryEmit(Unit)
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (current?.get() === activity) current = null
    }

    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
}
