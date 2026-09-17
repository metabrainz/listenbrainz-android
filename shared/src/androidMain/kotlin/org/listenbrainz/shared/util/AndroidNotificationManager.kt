package org.listenbrainz.shared.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mmk.kmpnotifier.KMPNotifier
import com.mmk.kmpnotifier.local.LocalNotifications
import com.mmk.kmpnotifier.local.localNotifier
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration
import org.listenbrainz.shared.applicationContext
import org.listenbrainz.shared.model.PlayingTrack
import org.listenbrainz.shared.repository.PlatformContext
import org.listenbrainz.shared.util.ListenSubmissionNotification.CHANNEL_DESCRIPTION
import org.listenbrainz.shared.util.ListenSubmissionNotification.CHANNEL_ID
import org.listenbrainz.shared.util.ListenSubmissionNotification.CHANNEL_NAME
import org.listenbrainz.shared.util.ListenSubmissionNotification.NOTIFICATION_ID

class AndroidNotificationManager(
    val context: PlatformContext = applicationContext,
    val drawableProvider: DrawableProvider,
    val arrayProvider: ArrayProvider,
    val stringProvider: StringProvider
) : PlatformNotificationManager {

    val manager = NotificationManagerCompat.from(context)

    init {
        KMPNotifier.initialize(
            configuration = NotificationPlatformConfiguration.Android(
                notificationIconResId = drawableProvider.getDrawable(DrawableResource.IC_LISTENBRAINZ_LOGO_NO_TEXT),
                notificationChannelData = NotificationPlatformConfiguration.Android.NotificationChannelData(
                    id = CHANNEL_ID,
                    name = CHANNEL_NAME,
                    description = CHANNEL_DESCRIPTION
                )
            ),
            LocalNotifications
        )
    }

    private val notifier get() = KMPNotifier.localNotifier

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
        notifier.remove(NOTIFICATION_ID)
        val systemManager = ContextCompat.getSystemService(context, NotificationManager::class.java)
        systemManager?.deleteNotificationChannel(CHANNEL_ID)
    }

    fun createNotification(playingTrack: PlayingTrack?): Notification{
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(drawableProvider.getDrawable(DrawableResource.IC_LISTENBRAINZ_LOGO_NO_TEXT))
            .setContentIntent(launcherPendingIntent())
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
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .setBigContentTitle(listeningTitle)
                        .bigText(ListenSubmissionNotification.playingTrackDescription(playingTrack))
                )
        }else{
            // No track playing - show idle state
            val idleMessages = context.resources.getStringArray(arrayProvider.getArray(ArrayResource.NOTIFICATION_IDLE_MESSAGES))
            val randomMessage = idleMessages.random()
            builder.setContentText(randomMessage)
        }
        return builder.build()
    }

    private fun launcherPendingIntent(): PendingIntent? {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return null
        return PendingIntent.getActivity(context, NOTIFICATION_ID, intent, PendingIntent.FLAG_IMMUTABLE)
    }
}