package org.listenbrainz.shared.util

import org.listenbrainz.shared.model.PlayingTrack
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

class IosNotificationManager(
    private val notificationId: String,
    private val listeningTitle: String
): PlatformNotificationManager {

    private val center = UNUserNotificationCenter.currentNotificationCenter()

    override fun createChannel() {
        // no-op
    }

    override fun deleteChannel() {
        // no-op
        clear()
    }

    override fun postListeningNotification(track: PlayingTrack?) {
        if(track == null || track.isNothing()){
            clear()
            return
        }
        val titleText = track.title ?: "Unknown Track"
        val artistText = track.artist ?: "Unknown Artist"

        val body = buildString {
            append("\uD83C\uDF99\uFE0F ")
            append(titleText)
            append("\n")
            append("\uD83D\uDC64 ")
            append(artistText)
            if (!track.releaseName.isNullOrEmpty()) {
                append("\n")
                append("\uD83D\uDCC0 ")
                append(track.releaseName)
            }
        }
        val content = UNMutableNotificationContent().apply {
            setTitle(listeningTitle)
            setSubtitle("$titleText • $artistText")
            setBody(body)
            setSound(null)
        }
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = notificationId,
            content = content,
            trigger = null
        )
        center.addNotificationRequest(request,null)
    }

    private fun clear(){
        val ids = listOf<Any>(notificationId)
        center.removePendingNotificationRequestsWithIdentifiers(ids)
        center.removeDeliveredNotificationsWithIdentifiers(ids)
    }
}