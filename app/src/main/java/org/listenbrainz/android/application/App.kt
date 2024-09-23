package org.listenbrainz.android.application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.StrictMode
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.limurse.logger.Logger
import com.limurse.logger.config.Config
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.listenbrainz.android.BuildConfig
import org.listenbrainz.android.R
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.service.ListenSubmissionService
import org.listenbrainz.android.util.Constants
import org.listenbrainz.android.util.Log
import org.listenbrainz.android.util.Utils.isServiceRunning
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var appPreferences: AppPreferences

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        val logDirectory = applicationContext.getExternalFilesDir(null)?.path.orEmpty()
        val config = Config.Builder(logDirectory)
            .setDefaultTag(Constants.TAG)
            .setLogcatEnable(true)
            .setDataFormatterPattern("dd-MM-yyyy-HH:mm:ss")
            .setStartupData(collectStartupData())
            .build()

        Logger.init(config)

        if (BuildConfig.DEBUG) {
            enableStrictMode()
        }

        context = this
        
        MainScope().launch {
            if(
                appPreferences.isNotificationServiceAllowed &&
                appPreferences.lbAccessToken.get().isNotEmpty() &&
                appPreferences.isListeningAllowed.get()
            ) {
                startListenService()
            }
        }

        createChannels()
    }

    private fun collectStartupData(): Map<String, String> = mapOf(
        "App Version" to System.currentTimeMillis().toString(),
        "Device Application Id" to BuildConfig.APPLICATION_ID,
        "Device Version Code" to BuildConfig.VERSION_CODE.toString(),
        "Device Version Name" to BuildConfig.VERSION_NAME,
        "Device Build Type" to BuildConfig.BUILD_TYPE,
        "Device" to Build.DEVICE,
        "Device SDK" to Build.VERSION.SDK_INT.toString(),
        "Device Manufacturer" to Build.MANUFACTURER
    )

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
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    
    companion object {
        lateinit var context: App
            private set

        fun startListenService() {
            val intent = Intent(context, ListenSubmissionService::class.java)
            if (!context.isServiceRunning(ListenSubmissionService::class.java)) {
                val component = context.startService(intent)
                if (component == null) {
                    Log.d("No running instances found, starting service.")
                } else {
                    Log.d("Service already running with name: $component")
                }
            } else {
                Log.d("Service already running")
            }
        }
    }
}
