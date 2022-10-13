package org.listenbrainz.android.presentation.features.brainzplayer.services.callback

import android.app.Notification
import android.content.Intent
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import org.listenbrainz.android.presentation.features.brainzplayer.services.BrainzPlayerService
import org.listenbrainz.android.util.BrainzPlayerUtils.NOTIFICATION_ID

class BrainzPlayerNotificationListener(private val brainzPlayerService: BrainzPlayerService) :
    PlayerNotificationManager.NotificationListener {
    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
        super.onNotificationCancelled(notificationId, dismissedByUser)
        brainzPlayerService.apply {
            stopForeground(true)
            isForegroundService = false
            stopSelf()
        }
    }

    override fun onNotificationPosted(
        notificationId: Int,
        notification: Notification,
        ongoing: Boolean
    ) {
        super.onNotificationPosted(notificationId, notification, ongoing)
        brainzPlayerService.apply {
            if (ongoing && !isForegroundService) {
                ContextCompat.startForegroundService(
                    this,
                    Intent(applicationContext, this::class.java)
                )
                startForeground(NOTIFICATION_ID, notification)
                isForegroundService = true
            }
        }
    }
}
