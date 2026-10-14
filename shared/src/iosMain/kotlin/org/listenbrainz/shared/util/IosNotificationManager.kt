package org.listenbrainz.shared.util

import com.mmk.kmpnotifier.KMPNotifier
import com.mmk.kmpnotifier.local.LocalNotifications
import com.mmk.kmpnotifier.local.localNotifier
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration

class IosNotificationManager(
    val notificationId: Int,
    val listeningTitle: String
): PlatformNotificationManager {

    init {
        KMPNotifier.initialize(
            configuration = NotificationPlatformConfiguration.Ios(),
            LocalNotifications
        )
    }

    val notifier get() = KMPNotifier.localNotifier


    override fun createChannel() {
        // no-op
    }

    override fun deleteChannel() {
        // no-op
        clear()
    }
    fun clear(){
        notifier.remove(notificationId)
    }
}