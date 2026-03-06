package org.listenbrainz.shared.preferences

import android.content.Context

internal object AndroidDataStoreContext {
    @Volatile
    private var appContext: Context? = null

    fun set(context: Context) {
        appContext = context.applicationContext
    }

    fun require(): Context {
        return requireNotNull(appContext) {
            "DataStore context not initialized. Call platformInitDataStoreContext first."
        }
    }
}
