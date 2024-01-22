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
    
    /** Update current [playingTrack] with [this] given some conditions.
     * @param newTrack
     * @param onTrackIsOutdated lambda to run when the current track is outdated.
     * @param onTrackIsSimilarCallbackTrack lambda to run when the new track is similar to current
     * playing track AND current playing track's metadata has been derived from **Listen Callback**.
     * @param onTrackIsSimilarNotificationTrack lambda to run when the new track is similar to current
     * playing track AND current playing track's metadata has been derived from **onNotificationPosted**.*/
    private fun PlayingTrack.updatePlayingTrack(
        onTrackIsOutdated: (newTrack: PlayingTrack) -> Unit,
        onTrackIsSimilarNotificationTrack: (newTrack: PlayingTrack) -> Unit,
        onTrackIsSimilarCallbackTrack: (newTrack: PlayingTrack) -> Unit,
    ) {
        if (playingTrack.isOutdated(this)) {
            
            onTrackIsOutdated(this)
            
        } else if (playingTrack.isNotificationTrack()) {
            // This means only onPostedNotification's metadata has arrived and callback is late.
            // Timer is already started but we need to update its duration.
            
            onTrackIsSimilarNotificationTrack(this)
        } else if (playingTrack.isCallbackTrack()) {
            // Track is callback track.
            
            onTrackIsSimilarCallbackTrack(this)
        }
    }
    
    private fun beforeMetadataSet() {
        // Before Metadata set
        timer.stop()
        playingTrack.reset()
    }
    
    private fun afterMetadataSet() {
        // After metadata set
        if (isMetadataFaulty()) {
            w("${if (playingTrack.artist == null) "Artist" else "Title"} is null, listen cancelled.")
            playingTrack.reset()
            return
        }
    
        initTimer()
    }
    
    /** Initialize listen metadata and timer.
     * @param metadata Metadata to set the state's data.
     * @param pkg Package of music player the song is being played from.
     */
    fun onControllerCallback(
        newTrack: PlayingTrack
    ){
        newTrack.updatePlayingTrack(
            onTrackIsOutdated = { track ->
                // Updating currentTrack
                beforeMetadataSet()
                playingTrack = track
                afterMetadataSet()
            },
            onTrackIsSimilarCallbackTrack = { track ->
                // Usually this won't happen because metadata isn't being changed.
                beforeMetadataSet()
                playingTrack = track
                afterMetadataSet()
            },
            onTrackIsSimilarNotificationTrack = { track ->
                // Update but retain timestamp and playingNowSubmitted.
                // We usually do not expect this callback to arrive later for submitted to change.
                playingTrack = track.apply {
                    timestamp = playingTrack.timestamp
                    playingNowSubmitted = playingTrack.playingNowSubmitted
                }    // Current track will always have more metadata here
                
                // Update timer because now we have duration.
                timer.extendDuration { secondsPassed ->
                    track.duration/2 - secondsPassed
                }
            }
        )
        // No need to toggle timer here since we can rely on onNotificationPosted to do that.
    }
    
    fun alertMediaNotificationUpdate(newTrack: PlayingTrack) {
        newTrack.updatePlayingTrack(
            onTrackIsOutdated = { track ->
                beforeMetadataSet()
                
                playingTrack = if (playingTrack.isSimilarTo(track)){
                    // Old track has useful metadata like duration, so smartly retrieve.
                    track.apply { duration = playingTrack.duration }
                } else {
                    track
                }
                
                afterMetadataSet()
            },
            onTrackIsSimilarCallbackTrack = { track ->
                // We definitely know that whenever the notification bar changes a bit, we will get a state
                // update which means we have a valid reason to query if music is playing or not.
                alertPlaybackStateChanged()
            },
            onTrackIsSimilarNotificationTrack = { track ->
                // Same as above.
                alertPlaybackStateChanged()
            }
        )
        alertPlaybackStateChanged()
    }
    
    fun alertMediaPlayerRemoved(notification: StatusBarNotification) {
        d("Removed " + notification.notification.extras)
    }
    
    /** Toggle timer based on state. */
    @Synchronized
    fun alertPlaybackStateChanged() {
        if (playingTrack.isSubmitted()) return
        
        if (audioManager.isMusicActive) {
            timer.startOrResume()
        } else {
            timer.pause()
        }
    }
    
    /** Run [artist] and [title] value-check before invoking this function.*/
    private fun initTimer() {
        // d(duration.toString())
        if (playingTrack.duration != 0L) {
            timer.setDuration(
                roundDuration(duration = playingTrack.duration / 2L)     // Since maximum time required to validate a listen as submittable listen is 4 minutes.
                    .coerceAtMost(240_000L)
            )
        } else {
            timer.setDuration(
                roundDuration(duration = DEFAULT_DURATION)     // Since maximum time required to validate a listen as submittable listen is 4 minutes.
            )
        }
        
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
        })
        d("Timer Set")
        alertPlaybackStateChanged()
    }
    
    // Utility functions
    
    private fun roundDuration(duration: Long): Long {
        return (duration / 1000) * 1000
    }
    
    private fun submitListen(listenType: ListenType) =
        workManager.enqueue(buildWorkRequest(playingTrack, listenType))
    
    private fun isMetadataFaulty(): Boolean = playingTrack.artist.isNullOrEmpty() || playingTrack.title.isNullOrEmpty()
    
    /** Discard current listen.*/
    fun discardCurrentListen() {
        playingTrack = PlayingTrack()
        timer.stop()
    }
    
    companion object {
        const val DEFAULT_DURATION: Long = 60_000L
        
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
