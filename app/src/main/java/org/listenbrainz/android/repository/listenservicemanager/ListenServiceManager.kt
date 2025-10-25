package org.listenbrainz.android.repository.listenservicemanager

import android.media.MediaMetadata
import android.media.session.PlaybackState
import android.service.notification.StatusBarNotification
import org.listenbrainz.android.util.ListenSubmissionState
import java.lang.AutoCloseable
import javax.inject.Singleton

@Singleton
interface ListenServiceManager {

    val listenSubmissionState: ListenSubmissionState
    
    fun onMetadataChanged(metadata: MediaMetadata?, packageName: String, isMediaPlaying: Boolean)
    
    fun onPlaybackStateChanged(state: PlaybackState?)
    
    fun onNotificationPosted(sbn: StatusBarNotification?, mediaPlaying: Boolean)
    
    fun onNotificationRemoved(sbn: StatusBarNotification?)
}