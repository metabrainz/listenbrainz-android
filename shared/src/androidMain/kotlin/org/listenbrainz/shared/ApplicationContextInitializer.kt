package org.listenbrainz.shared

import android.content.Context
import androidx.startup.Initializer

internal lateinit var applicationContext: Context
    private set

internal class ApplicationContextInitializer: Initializer<Context> {
    override fun create(context: Context): Context {
        applicationContext = context
        return context
    }

    override fun dependencies(): List<Class<out Initializer<*>?>?> = emptyList()
}