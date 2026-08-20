package org.listenbrainz.shared.util

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import org.listenbrainz.shared.applicationContext
import org.listenbrainz.shared.model.PlayingTrack
import org.listenbrainz.shared.repository.PlatformContext
import org.listenbrainz.shared.util.ListenSubmissionNotification.CHANNEL_DESCRIPTION
import org.listenbrainz.shared.util.ListenSubmissionNotification.CHANNEL_ID
import org.listenbrainz.shared.util.ListenSubmissionNotification.CHANNEL_NAME
import org.listenbrainz.shared.util.ListenSubmissionNotification.NOTIFICATION_ID

class AndroidNotificationManager(
    private val context: PlatformContext = applicationContext,
    private val targetActivityClass: Class<*>,
    private val drawableProvider: DrawableProvider,
    private val arrayProvider: ArrayProvider,
    private val stringProvider: StringProvider
): PlatformNotificationManager {
    private val manager = NotificationManagerCompat.from(context)

    override fun createChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val systemManager = ContextCompat.getSystemService(context, NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            systemManager?.createNotificationChannel(channel)
        }
    }

    override fun deleteChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val systemManager = ContextCompat.getSystemService(context, NotificationManager::class.java)
            systemManager?.deleteNotificationChannel(CHANNEL_ID)
        }
    }

    @SuppressLint("MissingPermission")
    override fun postListeningNotification(track: PlayingTrack?)  {
        val notification = createNotification(track)
        if(manager.areNotificationsEnabled()){
            manager.notify(NOTIFICATION_ID,notification)
        }
    }

    fun createNotification(playingTrack: PlayingTrack?): Notification{
        val clickPendingIntent = PendingIntent.getActivity(
            context,0, Intent(context,targetActivityClass),
            PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(context,CHANNEL_ID)
            .setSmallIcon(drawableProvider.getDrawable(DrawableResource.IC_LISTENBRAINZ_LOGO_NO_TEXT))
            .setContentIntent(clickPendingIntent)
            .setSound(null)
            .setOngoing(true)
            .setAutoCancel(false)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            //.setColorized(true)
            //.setColor(ContextCompat.getColor(context, R.color.lb_purple))

        if(playingTrack != null && !playingTrack.isNothing()){
            val titleText = playingTrack.title ?: "Unknown Track"
            val artistText = playingTrack.artist ?: "Unknown Artist"

            val listeningTitle = context.getString(stringProvider.getString(StringResource.NOTIFICATION_LISTENING_TITLE))
            builder.setContentTitle(listeningTitle)
                .setContentText("$titleText • $artistText")

            val bigTextStyle = NotificationCompat.BigTextStyle()
                .setBigContentTitle(listeningTitle)
                .bigText(buildString {
                    append("\uD83C\uDF99\uFE0F ")
                    append(titleText)
                    append("\n")
                    append("\uD83D\uDC64 ")
                    append(artistText)
                    if (!playingTrack.releaseName.isNullOrEmpty()) {
                        append("\n")
                        append("\uD83D\uDCC0 ")
                        append(playingTrack.releaseName)
                    }
                })
            builder.setStyle(bigTextStyle)
        }else{
            // No track playing - show idle state
            val idleMessages = context.resources.getStringArray(arrayProvider.getArray(ArrayResource.NOTIFICATION_IDLE_MESSAGES))
            val randomMessage = idleMessages.random()
            builder.setContentText(randomMessage)
        }
        return builder.build()
    }
}