package org.listenbrainz.android.service

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.media.session.MediaSessionManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import dagger.hilt.android.AndroidEntryPoint
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.listens.ListensRepository
import org.listenbrainz.android.util.ListenHandler
import org.listenbrainz.android.util.ListenSessionListener
import org.listenbrainz.android.util.Log.d
import javax.inject.Inject

@AndroidEntryPoint
class ListenScrobbleService : NotificationListenerService() {

    @Inject
    lateinit var appPreferences: AppPreferences
    
    @Inject
    lateinit var listensRepository: ListensRepository

    private var sessionManager: MediaSessionManager? = null
    private var handler: ListenHandler? = null
    private var sessionListener: ListenSessionListener? = null
    private var listenServiceComponent: ComponentName? = null

    override fun onCreate() {
        super.onCreate()
        initialize()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int) = Service.START_STICKY

    private fun initialize() {
        d("Initializing Listener Service")
        handler = ListenHandler(appPreferences, listensRepository)
        sessionManager = applicationContext.getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
        sessionListener = ListenSessionListener(handler!!, appPreferences)
        listenServiceComponent = ComponentName(this, this.javaClass)
        sessionManager?.addOnActiveSessionsChangedListener(sessionListener!!, listenServiceComponent)
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionListener?.let { sessionManager?.removeOnActiveSessionsChangedListener(it) }
        sessionListener?.clearSessions()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
    }
}