package org.listenbrainz.android.repository.listenservicemanager

import android.media.MediaMetadata
import android.media.session.PlaybackState
import android.service.notification.StatusBarNotification

interface ListenServiceManager {
    
    fun onMetadataChanged(metadata: MediaMetadata?, player: String)
    
    fun onPlaybackStateChanged(state: PlaybackState?)
    
    fun onNotificationPosted(sbn: StatusBarNotification?)
    
    fun onNotificationRemoved(sbn: StatusBarNotification?)
}