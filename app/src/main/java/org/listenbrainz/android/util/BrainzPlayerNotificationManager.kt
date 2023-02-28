package org.listenbrainz.android.util

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import kotlinx.coroutines.*
import org.listenbrainz.android.R
import org.listenbrainz.android.util.BrainzPlayerExtensions.bitmap
import org.listenbrainz.android.util.BrainzPlayerUtils.NOTIFICATION_CHANNEL_ID
import org.listenbrainz.android.util.BrainzPlayerUtils.NOTIFICATION_ID

class BrainzPlayerNotificationManager(
    private val context : Context,
    sessionToken: MediaSessionCompat.Token,
    notificationListener: PlayerNotificationManager.NotificationListener,
    private val newSongCallback: ()-> Unit
) {
    private val notificationManager: PlayerNotificationManager
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)


    init {
        val mediaController = MediaControllerCompat(context, sessionToken)
        notificationManager = PlayerNotificationManager.Builder(
            context,
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL_ID
        )
            .setChannelNameResourceId(R.string.brainzplayer_notification_channel_name)
            .setChannelDescriptionResourceId(R.string.brainzplayer_notification_channel_description)
            .setMediaDescriptionAdapter(DescriptionAdapter(mediaController))
            .setNotificationListener(notificationListener)
            .setNextActionIconResourceId(R.drawable.btn_next)
            .setPreviousActionIconResourceId(R.drawable.btn_prev)
            .setPlayActionIconResourceId(R.drawable.ic_play)
            .setPauseActionIconResourceId(R.drawable.ic_pause)
            .build()
            .apply {
                setSmallIcon(R.drawable.ic_listenbrainz_logo_no_text)
                setMediaSessionToken(sessionToken)
                setUseNextActionInCompactView(true)
                setUsePreviousActionInCompactView(true)
                setUseFastForwardAction(false)
                setUseRewindAction(false)

            }
    }

    fun showNotification(player: Player) {
        notificationManager.setPlayer(player)
    }
    
    fun hideNotification() {
        notificationManager.setPlayer(null)
    }

    inner class DescriptionAdapter(private val mediaController: MediaControllerCompat) :
        PlayerNotificationManager.MediaDescriptionAdapter {

        var currentIconUri: Uri? = null
        var currentBitmap: Bitmap? = null

        override fun getCurrentContentTitle(player: Player): CharSequence {
            newSongCallback()
            return mediaController.metadata.description.title.toString()
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return mediaController.sessionActivity
        }

        override fun getCurrentContentText(player: Player): CharSequence {
            return mediaController.metadata.description.subtitle.toString()
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            val iconUri = mediaController.metadata.description.iconUri
            return if (currentIconUri != iconUri || currentBitmap == null) {
                currentIconUri = iconUri
                serviceScope.launch {
                    currentBitmap = iconUri?.let { retrieveBitmap(it) }
                    currentBitmap?.let { callback.onBitmap(it) }
                }
                null
            } else {
                currentBitmap
            }
        }

        private suspend fun retrieveBitmap(uri: Uri): Bitmap = uri.toString().bitmap(context)
    }
}
