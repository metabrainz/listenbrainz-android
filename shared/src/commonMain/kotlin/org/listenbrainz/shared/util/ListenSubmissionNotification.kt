package org.listenbrainz.shared.util

import org.listenbrainz.shared.model.PlayingTrack

object ListenSubmissionNotification {
    const val NOTIFICATION_ID = 420
    const val CHANNEL_ID = "listen_channel"
    const val CHANNEL_NAME = "Listening"
    const val CHANNEL_DESCRIPTION = "Determines if the app is listening to notifications."

    fun playingTrackDescription(track: PlayingTrack): String = buildString {
        append("\uD83C\uDF99\uFE0F ")
        append(track.title ?: "Unknown Track")
        append("\n")
        append("\uD83D\uDC64 ")
        append(track.artist ?: "Unknown Artist")
        if (!track.releaseName.isNullOrEmpty()) {
            append("\n")
            append("\uD83D\uDCC0 ")
            append(track.releaseName)
        }
    }
}