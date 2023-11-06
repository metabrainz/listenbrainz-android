package org.listenbrainz.android.application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.StrictMode
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.listenbrainz.android.BuildConfig
import org.listenbrainz.android.R
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.service.ListenScrobbleService
import org.listenbrainz.android.util.Constants
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var appPreferences: AppPreferences

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            enableStrictMode()
        }

        context = this
        
        MainScope().launch {
            if(
                appPreferences.isNotificationServiceAllowed &&
                appPreferences.getLbAccessToken().isNotEmpty() &&
                appPreferences.submitListens
            ) {
                startListenService()
            }
        }

        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return

        val nm = ContextCompat.getSystemService(this, NotificationManager::class.java)!!

        val channels = nm.notificationChannels

        // delete old channels, if they exist
        if (channels?.any { it.id == "foreground" } == true) {
            channels.forEach { nm.deleteNotificationChannel(it.id) }
        }

        nm.createNotificationChannel(
            NotificationChannel(
                Constants.Strings.CHANNEL_NOTI_SCROBBLING,
                getString(R.string.state_scrobbling), NotificationManager.IMPORTANCE_LOW
            )
        )
        nm.createNotificationChannel(
            NotificationChannel(
                Constants.Strings.CHANNEL_NOTI_SCR_ERR,
                getString(R.string.channel_err), NotificationManager.IMPORTANCE_MIN
            )
        )
        nm.createNotificationChannel(
            NotificationChannel(
                Constants.Strings.CHANNEL_NOTI_NEW_APP,
                getString(R.string.new_player, getString(R.string.new_app)),
                NotificationManager.IMPORTANCE_LOW
            )
        )
        nm.createNotificationChannel(
            NotificationChannel(
                Constants.Strings.CHANNEL_NOTI_PENDING,
                getString(R.string.pending_scrobbles), NotificationManager.IMPORTANCE_MIN
            )
        )
    }

    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyFlashScreen()
                .build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectActivityLeaks()
                .detectFileUriExposure()
                .detectLeakedClosableObjects()
                .detectLeakedRegistrationObjects()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .build()
        )
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
