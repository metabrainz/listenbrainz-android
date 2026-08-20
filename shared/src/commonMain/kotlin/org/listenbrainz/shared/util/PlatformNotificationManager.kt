package org.listenbrainz.shared.util

import org.listenbrainz.shared.model.PlayingTrack

interface PlatformNotificationManager {
    fun createChannel()
    fun postListeningNotification(track: PlayingTrack?)
    fun deleteChannel()
}