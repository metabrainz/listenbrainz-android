package org.listenbrainz.android.application

import android.app.Application
import android.content.Intent
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.service.ListenScrobbleService
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var appPreferences: AppPreferences

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        context = this
        
        MainScope().launch {
            if(
                appPreferences.isNotificationServiceAllowed &&
                !appPreferences.getLbAccessToken().isNullOrEmpty() &&
                appPreferences.submitListens
            ) {
                startListenService()
            }
        }
    }
    
    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    
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
