package org.listenbrainz.android.service

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.media.session.MediaSessionManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.listenbrainz.android.repository.listens.ListensRepository
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.util.ListenSessionListener
import org.listenbrainz.android.util.Log.d
import javax.inject.Inject

@AndroidEntryPoint
class ListenScrobbleService : NotificationListenerService() {

    @Inject
    lateinit var appPreferences: AppPreferences
    
    @Inject
    lateinit var listensRepository: ListensRepository

    @Inject
    lateinit var workManager: WorkManager
    
    private var sessionManager: MediaSessionManager? = null
    private var sessionListener: ListenSessionListener? = null
    private var listenServiceComponent: ComponentName? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        initialize()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = Service.START_STICKY

    private fun initialize() {
        d("Initializing Listener Service")
        sessionManager = applicationContext.getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
        sessionListener = ListenSessionListener(appPreferences, workManager, scope)
        listenServiceComponent = ComponentName(this, this.javaClass)
        
        try {
            sessionManager?.addOnActiveSessionsChangedListener(
                sessionListener!!,
                listenServiceComponent
            )
        } catch (e: Exception) {
            e.printStackTrace()
            // Remove orphan entries.
            try {
                sessionManager?.removeOnActiveSessionsChangedListener(sessionListener!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionListener?.clearSessions()
        sessionListener?.let { sessionManager?.removeOnActiveSessionsChangedListener(it) }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        print(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
    }
}