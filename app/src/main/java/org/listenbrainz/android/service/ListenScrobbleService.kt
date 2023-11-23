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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.scrobblemanager.ScrobbleManager
import org.listenbrainz.android.util.Constants.Strings.CHANNEL_ID
import org.listenbrainz.android.util.ListenSessionListener
import org.listenbrainz.android.util.Log.d
import org.listenbrainz.android.util.Log.e
import javax.inject.Inject

@AndroidEntryPoint
class ListenScrobbleService : NotificationListenerService() {

    @Inject
    lateinit var appPreferences: AppPreferences
    
    @Inject
    lateinit var scrobbleManager: ScrobbleManager
    
    private var sessionManager: MediaSessionManager? = null
    private var sessionListener: ListenSessionListener? = null
    private var listenServiceComponent: ComponentName? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val nm by lazy {
        ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        )!!
    }

    override fun onCreate() {
        super.onCreate()
        initialize()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = Service.START_STICKY

    private fun initialize() {
        d("Initializing Listener Service")
        sessionManager = applicationContext.getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
        sessionListener = ListenSessionListener(appPreferences, scrobbleManager, scope)
        listenServiceComponent = ComponentName(this, this.javaClass)
        createNotificationChannel()

        try {
            sessionManager?.addOnActiveSessionsChangedListener(sessionListener!!, listenServiceComponent)
        } catch (e: SecurityException) {
            e(message = "Could not add session listener due to security exception: ${e.message}")
        } catch (e: Exception) {
            e(message = "Could not add session listener: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionListener?.clearSessions()
        sessionListener?.let { sessionManager?.removeOnActiveSessionsChangedListener(it) }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        scrobbleManager.onNotificationPosted(sbn)
    }

    override fun onNotificationRemoved(
        sbn: StatusBarNotification?,
        rankingMap: RankingMap?,
        reason: Int
    ) {
        if (reason == REASON_APP_CANCEL || reason == REASON_APP_CANCEL_ALL ||
            reason == REASON_TIMEOUT || reason == REASON_ERROR
        ) {
            scrobbleManager.onNotificationRemoved(sbn)
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
            nm.createNotificationChannel(channel)
        }
    }
}