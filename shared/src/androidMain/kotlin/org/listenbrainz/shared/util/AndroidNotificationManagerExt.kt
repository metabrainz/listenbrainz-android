package org.listenbrainz.shared.util

import android.annotation.SuppressLint
import org.listenbrainz.shared.model.PlayingTrack
import org.listenbrainz.shared.util.ListenSubmissionNotification.NOTIFICATION_ID

@SuppressLint("MissingPermission")
actual fun PlatformNotificationManager.postListeningNotification(track: PlayingTrack?) {
    val androidManager = this as? AndroidNotificationManager ?: return
    if (!androidManager.manager.areNotificationsEnabled()) {
        return
    }
    androidManager.manager.notify(NOTIFICATION_ID, androidManager.createNotification(track))
}
