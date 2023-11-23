package org.listenbrainz.android.util

import android.content.Context
import android.media.AudioManager
import android.media.MediaMetadata
import android.service.notification.StatusBarNotification
import androidx.core.content.ContextCompat
import androidx.work.WorkManager
import org.listenbrainz.android.model.ListenType
import org.listenbrainz.android.model.OnTimerListener
import org.listenbrainz.android.model.PlayingTrack
import org.listenbrainz.android.service.ListenSubmissionWorker.Companion.buildWorkRequest
import org.listenbrainz.android.util.Log.d
import org.listenbrainz.android.util.Log.w

class ListenSubmissionState(
    private val workManager: WorkManager,
    private val context: Context
) {
    var playingTrack: PlayingTrack = PlayingTrack()
        private set
    private val audioManager by lazy {
        ContextCompat.getSystemService(
            context,
            AudioManager::class.java
        )!!
    }
    private val timer: Timer = Timer()
    
    /**
     * @param newTrack
     * @param onTrackIsOutdated lambda to run when the current track is outdated.
     * @param onTrackIsSimilarCallbackTrack lambda to run when the new track is similar to current
     * playing track AND current playing track's metadata has been derived from **Listen Callback**.
     * @param onTrackIsSimilarNotificationTrack lambda to run when the new track is similar to current
     * playing track AND current playing track's metadata has been derived from **onNotificationPosted**.*/
    private fun updatePlayingTrack(
        newTrack: PlayingTrack,
        onTrackIsOutdated: (newTrack: PlayingTrack) -> Unit,
        onTrackIsSimilarNotificationTrack: (newTrack: PlayingTrack) -> Unit,
        onTrackIsSimilarCallbackTrack: (newTrack: PlayingTrack) -> Unit,
    ) {
        if (playingTrack.isNothing() || !playingTrack.isSimilarTo(newTrack)) {
    
            // Before Metadata set
            timer.stop()
            playingTrack.reset()
        
            onTrackIsOutdated(newTrack)
            
            // After metadata set
            if (isMetadataFaulty()) {
                w("${if (playingTrack.artist == null) "Artist" else "Title"} is null, listen cancelled.")
                return
            }
    
            initTimer()
            
        } else if (playingTrack.isNotificationTrack()) {
            // This means only onPostedNotification's metadata has arrived and callback is late.
            // Timer is already started but we need to update its duration.
            
            onTrackIsSimilarNotificationTrack(newTrack)
        } else if (playingTrack.isCallbackTrack()) {
            // Track is callback track.
            
            onTrackIsSimilarCallbackTrack(newTrack)
        }
        
    }
    
    /** Initialize listen metadata and timer.
     * @param metadata Metadata to set the state's data.
     * @param pkg Package of music player the song is being played from.
     */
    fun onControllerCallback(
        metadata: MediaMetadata,
        pkg: String,
    ){
        updatePlayingTrack(
            newTrack = metadata.toPlayingTrack(pkg),
            onTrackIsOutdated = { newTrack ->
                // Updating currentTrack
                playingTrack = newTrack
            },
            onTrackIsSimilarCallbackTrack = { newTrack ->
                // Do nothing for now
            },
            onTrackIsSimilarNotificationTrack = { newTrack ->
                // Update but retain timestamp.
                playingTrack = newTrack.apply { timestamp = playingTrack.timestamp }    // Current track will always have more metadata here
                
                // Update timer.
                timer.extendDuration((newTrack.duration - 60_000) / 2)
            }
        )
        // No need to toggle timer here
    }
    
    fun alertMediaNotificationActive(newTrack: PlayingTrack) {
        val oldTrack = playingTrack.copy()
        updatePlayingTrack(
            newTrack = newTrack,
            onTrackIsOutdated = { track ->
                // Updating currentTrack
                playingTrack = track
            },
            onTrackIsSimilarCallbackTrack = { track ->
                // Always prefer notification timestamp.
                playingTrack.timestamp = track.timestamp
            },
            onTrackIsSimilarNotificationTrack = { track ->
                // Do nothing
            }
        )
        
        // We definitely know that whenever the notification bar changes a bit, we will get a state
        // update which means we have a valid reason to query is music is playing or not.
        if (oldTrack.isSimilarTo(newTrack)) {
            alertPlaybackStateChanged()
        }
        
    }
    
    fun alertMediaPlayerRemoved(notification: StatusBarNotification) {
        d("Removed " + notification.notification.extras)
    }
    
    /** Toggle timer based on state. */
    fun alertPlaybackStateChanged() {
        
         //d("onPlaybackStateChanged: Listen PlaybackState " + state.state)
        if (playingTrack.isNothing() || playingTrack.isSubmitted()) return
        
        if (audioManager.isMusicActive) {
            timer.start()
            //d("Timer started")
        } else {
            timer.pause()
            //d("Timer paused")
        }
    }
    
    /** Run [artist] and [title] value-check before invoking this function.*/
    private fun initTimer() {
        // d(duration.toString())
        timer.setDuration(
            roundDuration(duration = playingTrack.duration / 2L)     // Since maximum time required to validate a listen as submittable listen is 4 minutes.
                .coerceIn(60_000L..240_000L)      // If we have no information about the duration, we'll submit after 1 minute.
        )
        
        // Setting listener
        timer.setOnTimerListener(listener = object : OnTimerListener {
            override fun onTimerEnded() {
                submitListen(ListenType.SINGLE)
                playingTrack.submitted = true
            }
            
            override fun onTimerPaused(remainingMillis: Long) {
                d("${remainingMillis / 1000} seconds left to submit listen.")
            }
            
            override fun onTimerStarted() {
                if (!playingTrack.playingNowSubmitted) {
                    submitListen(ListenType.PLAYING_NOW)
                    playingTrack.playingNowSubmitted = true
                }
            }
            
        }, callbacksOnMainThread = true)
        d("Timer Set")
    }
    
    // Utility functions
    
    private fun roundDuration(duration: Long): Long {
        return (duration / 1000) * 1000
    }
    
    private fun submitListen(listenType: ListenType) =
        workManager.enqueue(buildWorkRequest(playingTrack, listenType))
    
    private fun MediaMetadata.toPlayingTrack(pkgName: String): PlayingTrack {
        return PlayingTrack(
            timestamp = System.currentTimeMillis(),
            artist = extractArtist(),
            title = extractTitle(),
            duration = extractDuration(),
            releaseName = extractReleaseName(),
            pkgName = pkgName,
            playingNowSubmitted = false
        )
    }
    
    private fun isMetadataFaulty(): Boolean = playingTrack.artist.isNullOrEmpty() || playingTrack.title.isNullOrEmpty()
    
    private fun isDurationUndefined(): Boolean = playingTrack.duration <= 0
    
    companion object {
        fun MediaMetadata.extractTitle(): String? = when {
            !getString(MediaMetadata.METADATA_KEY_TITLE)
                .isNullOrEmpty() -> getString(
                MediaMetadata.METADATA_KEY_TITLE
            )
    
            !getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
                .isNullOrEmpty() -> getString(
                MediaMetadata.METADATA_KEY_DISPLAY_TITLE
            )
    
            else -> null
        }
    
        fun MediaMetadata.extractArtist(): String? = when {
            !getString(MediaMetadata.METADATA_KEY_ARTIST)
                .isNullOrEmpty() -> getString(
                MediaMetadata.METADATA_KEY_ARTIST
            )
    
            !getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
                .isNullOrEmpty() -> getString(
                MediaMetadata.METADATA_KEY_ALBUM_ARTIST
            )
    
            !getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)
                .isNullOrEmpty() -> getString(
                MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE
            )
    
            !getString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION)
                .isNullOrEmpty() -> getString(
                MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION
            )
    
            else -> null
        }
        
        fun MediaMetadata.extractDuration(): Long = getLong(MediaMetadata.METADATA_KEY_DURATION)
    
        fun MediaMetadata.extractReleaseName(): String? = getString(MediaMetadata.METADATA_KEY_ALBUM)
    }
}
