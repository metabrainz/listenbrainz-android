package org.listenbrainz.android.util

import android.app.Application
import android.util.Log
import io.sentry.Sentry
import io.sentry.android.core.SentryAndroid
import javax.inject.Inject
import javax.inject.Singleton

interface SentryController {
    fun setEnabled(enabled: Boolean)

    fun logMessage(message: String)

    fun logException(throwable: Throwable)
}

@Singleton
class SentryControllerImpl @Inject constructor(
    private val application: Application
) : SentryController {

    override fun setEnabled(enabled: Boolean) {
        if (enabled) {
            // Re-initialize Sentry using the application context.
            // This will use the DSN and other config from the AndroidManifest.
            SentryAndroid.init(application)
        } else {
            Sentry.close()
        }
        // Fixed: Added tag to Log.d
        Log.d("SentryController", "Sentry reporting set to: $enabled")
    }

    override fun logMessage(message: String) {
        if (Sentry.isEnabled()) {
            Sentry.captureMessage(message)
        }
    }

    override fun logException(throwable: Throwable) {
        if (Sentry.isEnabled()) {
            Sentry.captureException(throwable)
        }
    }
}