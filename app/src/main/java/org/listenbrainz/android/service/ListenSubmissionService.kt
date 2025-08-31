package org.listenbrainz.android.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.media.session.MediaSessionManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import org.listenbrainz.android.repository.listenservicemanager.ListenServiceManager
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.util.Constants.Strings.CHANNEL_ID
import org.listenbrainz.android.util.ListenSessionListener
import org.listenbrainz.android.util.Log
import javax.inject.Inject

@AndroidEntryPoint
class ListenSubmissionService : NotificationListenerService() {

    @Inject
    lateinit var appPreferences: AppPreferences
    
    @Inject
    lateinit var serviceManager: ListenServiceManager
    
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
        serviceManager.close()
        scope.cancel()
        Log.d("onDestroy: Listen Service stopped.")
        super.onDestroy()
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
            reason == REASON_TIMEOUT || reason == REASON_ERROR
        ) {
            serviceManager.onNotificationRemoved(sbn)
        }
    }

    companion object {
        private const val CHANNEL_NAME = "Scrobbling"
        private const val CHANNEL_DESCRIPTION = "Shows notifications when a song is played"
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
}