package org.listenbrainz.android.service

import android.content.ComponentName
import android.content.Intent
import android.media.session.MediaSessionManager
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.preference.PreferenceManager
import org.listenbrainz.android.util.ListenHandler
import org.listenbrainz.android.util.ListenSessionListener
import org.listenbrainz.android.util.Log.d
import org.listenbrainz.android.util.UserPreferences

class ListenService : NotificationListenerService() {
    private var sessionManager: MediaSessionManager? = null
    private var handler: ListenHandler? = null
    private var sessionListener: ListenSessionListener? = null
    private var listenServiceComponent: ComponentName? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        d("Listen Service Started")
        return START_STICKY
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        when {
            Looper.myLooper() == null -> {
                Handler(Looper.getMainLooper()).post { initialize() }
            }
            else -> initialize()
        }
    }

    private fun initialize() {
        d("Initializing Listener Service")
        val token = PreferenceManager.getDefaultSharedPreferences(this).getString(UserPreferences.PREFERENCE_LISTENBRAINZ_TOKEN, null)
        d("Your token is: $token")
        if (token == null || token.isEmpty()){
            d("ListenBrainz User token has not been set!")
        }
        handler = ListenHandler()
        sessionManager = applicationContext.getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
        sessionListener = ListenSessionListener(handler!!)
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