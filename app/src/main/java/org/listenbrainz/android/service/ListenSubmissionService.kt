package org.listenbrainz.android.service

import android.Manifest
import android.app.Notification
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.session.MediaSessionManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import org.koin.android.ext.android.inject
import org.listenbrainz.android.application.App
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.shared.repository.listenservicemanager.ListenServiceManager
import org.listenbrainz.shared.util.AndroidNotificationManager
import org.listenbrainz.shared.util.ListenSessionListener
import org.listenbrainz.shared.util.ListenSubmissionNotification.NOTIFICATION_ID
import org.listenbrainz.shared.util.Log
import org.listenbrainz.shared.util.PlatformNotificationManager

class ListenSubmissionService(
    private val logger:Log = Log
) : NotificationListenerService() {

    private val appPreferences: AppPreferences by inject()
    
    private val serviceManager: ListenServiceManager by inject()
    private val notificationManager: AndroidNotificationManager by inject()
    
    private val scope = MainScope()

    private var _sessionListener: ListenSessionListener? = null
    private val sessionListener: ListenSessionListener
        get() = _sessionListener!!

    private var listenServiceComponent: ComponentName? = null
    private var isConnected = false

    private val sessionManager: MediaSessionManager? by lazy {
        val manager = ContextCompat.getSystemService(this, MediaSessionManager::class.java)
        if (manager == null)
            logger.e("MediaSessionManager is not available in this context.")
        manager
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onCreate() {
        // Koin may not be started if the system binds this NotificationListenerService
        // before App.onCreate() runs (e.g. after a reboot).
        App.ensureKoinStarted(this)
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
            logger.d("onListenerDisconnected: Listen Service paused.")
            isConnected = false
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY

    private fun initialize() {
        logger.d("Initializing Listener Service")
        _sessionListener = ListenSessionListener(appPreferences, serviceManager, scope)
        listenServiceComponent = ComponentName(this, this.javaClass)
        createNotificationChannel()

        try {
            sessionManager?.addOnActiveSessionsChangedListener(sessionListener, listenServiceComponent)
        } catch (e: SecurityException) {
            logger.e(message = "Could not add session listener due to security exception: ${e.message}")
        } catch (e: Exception) {
            logger.e(message = "Could not add session listener: ${e.message}")
        }
    }

    private fun destroy() {
        deleteNotificationChannel()
        sessionListener.clearSessions()
        sessionListener.let { sessionManager?.removeOnActiveSessionsChangedListener(it) }
    }

    override fun onDestroy() {
        scope.cancel()
        logger.d("onDestroy: Listen Service stopped.")
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


    private fun createNotificationChannel() {
        notificationManager.createChannel()
    }
    
    private fun deleteNotificationChannel() {
        notificationManager.deleteChannel()
    }

    var isStarted = false
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun startForeground() {
        val notification = notificationManager.createNotification(null)
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