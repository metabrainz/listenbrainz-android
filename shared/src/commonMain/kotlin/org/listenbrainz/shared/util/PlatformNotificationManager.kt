package org.listenbrainz.shared.util

import org.listenbrainz.shared.model.PlayingTrack

interface PlatformNotificationManager {
    fun createChannel()
    fun deleteChannel()
}
expect fun PlatformNotificationManager.postListeningNotification(track: PlayingTrack?)