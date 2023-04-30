package org.listenbrainz.android.application

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import org.listenbrainz.android.repository.AppPreferences
import org.listenbrainz.android.service.ListenService
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {
    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreate() {
        super.onCreate()
        context = this

        when {
            appPreferences.preferenceListeningEnabled && Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP -> {
                startListenService()
            }
        }
    }

    fun startListenService() {
        val intent = Intent(this.applicationContext, ListenService::class.java)
        if (ProcessLifecycleOwner.get().lifecycle.currentState == Lifecycle.State.CREATED) {
            startService(intent)
        }
    }

    fun stopListenService() {
        val intent = Intent(this.applicationContext, ListenService::class.java)
        stopService(intent)
    }

    companion object {
        var context: App? = null
            private set
    }
}