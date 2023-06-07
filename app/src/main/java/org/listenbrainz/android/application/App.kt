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
import org.listenbrainz.android.repository.AppPreferences
import org.listenbrainz.android.service.ListenScrobbleService
import org.listenbrainz.android.service.ListensService
import org.listenbrainz.android.util.Constants
import org.listenbrainz.android.util.Log
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {
    @Inject
    lateinit var appPreferences: AppPreferences

    @Inject
    lateinit var listensService: ListensService

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        context = this

        if(appPreferences.isNotificationServiceAllowed) {
            startListenService()
        }

        if (!appPreferences.username.isNullOrEmpty() && !appPreferences.lbAccessToken.isNullOrEmpty()) {
            applicationScope.launch {
                if(listensService.getServicesLinkedToAccount("Bearer: ${appPreferences.lbAccessToken}",
                        appPreferences.username!!
                    ).services.contains("spotify")) {
                    Log.d("Spotify is already linked with web.")
                    if(!appPreferences.listeningBlacklist.contains(Constants.SPOTIFY_PACKAGE_NAME)) {
                        Log.d("Adding Spotify to blacklist.")
                        appPreferences.listeningBlacklist = appPreferences.listeningBlacklist.plus(Constants.SPOTIFY_PACKAGE_NAME)
                    }
                }
            }
        }
    }

    private fun startListenService() {
        val intent = Intent(this.applicationContext, ListenScrobbleService::class.java)
        if (ProcessLifecycleOwner.get().lifecycle.currentState == Lifecycle.State.CREATED) {
            startService(intent)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        applicationScope.cancel()
    }

    companion object {
        lateinit var context: App
            private set
    }
}
