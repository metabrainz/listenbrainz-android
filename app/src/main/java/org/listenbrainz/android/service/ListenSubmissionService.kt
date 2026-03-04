package org.listenbrainz.android.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.session.MediaSessionManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.MainScope
import org.koin.android.ext.android.inject
import kotlinx.coroutines.cancel
import org.listenbrainz.android.repository.listenservicemanager.ListenServiceManager
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.android.util.ListenSessionListener
import org.listenbrainz.android.util.ListenSubmissionState.Companion.getListeningNotification
import org.listenbrainz.android.util.Log

class ListenSubmissionService : NotificationListenerService() {

    private val appPreferences: AppPreferences by inject()
    
    private val serviceManager: ListenServiceManager by inject()
    
    private val scope = MainScope()

    private var _sessionListener: ListenSessionListener? = null
    private val sessionListener: ListenSessionListener
        get() = _sessionListener!!

    private var listenServiceComponent: ComponentName? = null
    private var isConnected = false
    
    private val nm: NotificationManager? by lazy {
        val manager = ContextCompat.getSystemService(this, NotificationManager::class.java)
        if (manager == null)
            Log.e("NotificationManager is not available in this context.")
        manager
    }
    private val sessionManager: MediaSessionManager? by lazy {
        val manager = ContextCompat.getSystemService(this, MediaSessionManager::class.java)
        if (manager == null)
            Log.e("MediaSessionManager is not available in this context.")
        manager
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground()
    }

    override fun onListenerConnected() {
        // Called more times than onListenerDisconnected for some reason.
        if (!isConnected) {
            initialize()
            isConnected = true
        }
    }

    override fun onListenerDisconnected() {
        if (isConnected) {
            destroy()
            Log.d("onListenerDisconnected: Listen Service paused.")
            isConnected = false
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY

    private fun initialize() {
        Log.d("Initializing Listener Service")
        _sessionListener = ListenSessionListener(appPreferences, serviceManager, scope)
        listenServiceComponent = ComponentName(this, this.javaClass)
        createNotificationChannel()

        try {
            sessionManager?.addOnActiveSessionsChangedListener(sessionListener, listenServiceComponent)
        } catch (e: SecurityException) {
            Log.e(message = "Could not add session listener due to security exception: ${e.message}")
        } catch (e: Exception) {
            Log.e(message = "Could not add session listener: ${e.message}")
        }
    }

    private fun destroy() {
        deleteNotificationChannel()
        sessionListener.clearSessions()
        sessionListener.let { sessionManager?.removeOnActiveSessionsChangedListener(it) }
    }

    override fun onDestroy() {
        scope.cancel()
        Log.d("onDestroy: Listen Service stopped.")
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        serviceManager.onNotificationPosted(sbn, sessionListener.isMediaPlaying)
    }

    override fun onNotificationRemoved(
        sbn: StatusBarNotification?,
        rankingMap: RankingMap?,
        reason: Int
    ) {
        if (reason == REASON_APP_CANCEL || reason == REASON_APP_CANCEL_ALL ||
            reason == REASON_CANCEL || reason == REASON_TIMEOUT || reason == REASON_ERROR
        ) {
            serviceManager.onNotificationRemoved(sbn)
        }
    }

    companion object {
        const val NOTIFICATION_ID = 420
        const val CHANNEL_ID = "listen_channel"
        private const val CHANNEL_NAME = "Listening"
        private const val CHANNEL_DESCRIPTION = "Determines if the app is listening to notifications."
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            nm?.createNotificationChannel(channel)
        }
    }
    
    private fun deleteNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm?.deleteNotificationChannel(CHANNEL_ID)
        }
    }

    var isStarted = false
    fun startForeground() {
        val notification = getListeningNotification(null)
        if (!isStarted) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU)
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                else
                    0
            )
            isStarted = true
        } else {
            NotificationManagerCompat.from(this)
                .notify(NOTIFICATION_ID, notification)
        }
    }
}