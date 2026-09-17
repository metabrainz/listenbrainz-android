package org.listenbrainz.shared.util

import org.listenbrainz.shared.model.PlayingTrack

actual fun PlatformNotificationManager.postListeningNotification(track: PlayingTrack?) {
    val iosManager = this as? IosNotificationManager ?: return
    if (track == null || track.isNothing()) {
        iosManager.clear()
        return
    }
    iosManager.notifier.notify {
        id = iosManager.notificationId
        title = iosManager.listeningTitle
        body = ListenSubmissionNotification.playingTrackDescription(track)
    }
}
