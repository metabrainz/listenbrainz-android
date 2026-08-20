package org.listenbrainz.android.application

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.StrictMode
import dev.brewkits.kmpworkmanager.KmpWorkManager
import dev.brewkits.kmpworkmanager.generated.AndroidWorkerFactoryGenerated
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.listenbrainz.android.BuildConfig
import org.listenbrainz.android.di.appModules
import org.listenbrainz.android.ui.screens.main.MainActivity
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.android.service.ListenSubmissionService
import org.listenbrainz.shared.util.Constants
import org.listenbrainz.android.util.Utils.isServiceRunning
import org.listenbrainz.shared.util.Log
import org.listenbrainz.shared.util.NotificationConfig

class App : Application() {

    private val appPreferences: AppPreferences by inject()

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        context = this
        super.onCreate()

        NotificationConfig.initialize(MainActivity::class)

        // Initialize Koin
        ensureKoinStarted(this)

        if (BuildConfig.DEBUG) {
            enableStrictMode()
        }

        GlobalScope.launch {
            cleanupBrainzPlayerResources()
            startListenService(appPreferences)
        }
    }

    private fun cleanupBrainzPlayerResources(){
        val dbName = "brainzplayer_database"
        val dbFile = context.getDatabasePath(dbName)
        if(dbFile.exists()){
            context.deleteDatabase(dbName)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.deleteNotificationChannel("Music")
        }
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

    companion object {
        lateinit var context: App
            private set

        @Synchronized
        fun ensureKoinStarted(context: Context) {
            if (GlobalContext.getOrNull() == null) {
                startKoin {
                    androidLogger()
                    androidContext(context.applicationContext)
                    modules(appModules)
                }
                KmpWorkManager.initialize(
                    context = context.applicationContext,
                    workerFactory = AndroidWorkerFactoryGenerated()
                )
            }
        }

        suspend fun startListenService(appPreferences: AppPreferences) = withContext(Dispatchers.Main) {
            if (
                appPreferences.isNotificationServiceAllowed &&
                appPreferences.lbAccessToken.get().isNotEmpty() &&
                appPreferences.isListeningAllowed.get()
            ) {
                val intent = Intent(context, ListenSubmissionService::class.java)
                if (!context.isServiceRunning(ListenSubmissionService::class.java)) {
                    val component = runCatching {
                         context.startService(intent)
                    }.getOrElse { error ->
                        Log.d(error)
                        null
                    }

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
}
