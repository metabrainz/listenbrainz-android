package org.listenbrainz.android.application

import android.app.Application
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.service.ListenScrobbleService
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {
    
    @Inject
    lateinit var appPreferences: AppPreferences

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        context = this
        
        applicationScope.launch {
            if(appPreferences.isNotificationServiceAllowed && appPreferences.getLbAccessToken().isNotEmpty()) {
                startListenService()
            }
        }
        
    }

    override fun onTerminate() {
        super.onTerminate()
        applicationScope.cancel()
    }

    companion object {
        lateinit var context: App
            private set

        fun startListenService() {
            val intent = Intent(context, ListenScrobbleService::class.java)
            if (ProcessLifecycleOwner.get().lifecycle.currentState == Lifecycle.State.CREATED) {
                context.startService(intent)
            }
        }
    }
}
