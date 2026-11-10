package org.listenbrainz.shared

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import org.listenbrainz.shared.permission.CurrentActivityTracker

internal lateinit var applicationContext: Context
    private set

internal class ApplicationContextInitializer: Initializer<Context> {
    override fun create(context: Context): Context {
        applicationContext = context
        (context.applicationContext as Application).registerActivityLifecycleCallbacks(CurrentActivityTracker)
        return context
    }

    override fun dependencies(): List<Class<out Initializer<*>?>?> = emptyList()
}