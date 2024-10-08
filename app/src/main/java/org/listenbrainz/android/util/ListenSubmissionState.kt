package org.listenbrainz.android.util

import android.content.Context
import android.media.MediaMetadata
import android.os.Handler
import android.service.notification.StatusBarNotification
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import org.listenbrainz.android.model.ListenType
import org.listenbrainz.android.model.OnTimerListener
import org.listenbrainz.android.model.PlayingTrack
import org.listenbrainz.android.service.ListenSubmissionWorker.Companion.buildWorkRequest

class ListenSubmissionState {
    var playingTrack: PlayingTrack = PlayingTrack()
        private set
    val timer: Timer
    private val workManager: WorkManager
    private val context: Context

    constructor(jobQueue: JobQueue = JobQueue(Dispatchers.Default), workManager: WorkManager, context: Context) {
        this.timer = TimerJQ(jobQueue)
        this.workManager = workManager
        this.context = context

        init()
    }

    constructor(handler: Handler, workManager: WorkManager, context: Context) {
        this.timer = TimerHandler(handler)
        this.workManager = workManager
        this.context = context

        init()
    }

    constructor(workManager: WorkManager, context: Context) {
        this.workManager = workManager
        this.context = context
        this.timer = TimerWorkManager(workManager)

        init()
    }
    
    fun init() {
        // Setting listener
        timer.setOnTimerListener(listener = object : OnTimerListener {
            override fun onTimerEnded() {
                submitListen(ListenType.SINGLE)
                playingTrack.submitted = true
            }
            
            override fun onTimerPaused(remainingMillis: Long) {
                Log.d("${remainingMillis / 1000} seconds left to submit listen.")
            }
            
            override fun onTimerStarted() {
                if (!playingTrack.playingNowSubmitted) {
                    submitListen(ListenType.PLAYING_NOW)
                    playingTrack.playingNowSubmitted = true
                }
            }
        })
    }
    
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
            Log.w("${if (playingTrack.artist.isNullOrEmpty()) "Artist" else "Title"} is null, listen cancelled.")
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
                Log.d("onControllerCallback: Updated current track")
            },
            onTrackIsSimilarCallbackTrack = { track ->
                // Usually this won't happen because metadata isn't being changed.
                beforeMetadataSet()
                playingTrack = track
                afterMetadataSet()
                Log.d("onControllerCallback: track is similar.")
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
                    track.duration / 2 - secondsPassed
                }
                Log.d("onControllerCallback: track is similar, updated metadata.")
            }
        )
        // No need to toggle timer here since we can rely on onNotificationPosted to do that.
    }
    
    fun alertMediaNotificationUpdate(newTrack: PlayingTrack, isMediaPlaying: Boolean) {
        newTrack.updatePlayingTrack(
            onTrackIsOutdated = { track ->
                beforeMetadataSet()
                
                playingTrack = if (playingTrack.isSimilarTo(track)) {
                    // Old track has useful metadata like duration, so smartly retrieve.
                    track.apply { duration = playingTrack.duration }
                } else {
                    track
                }
                
                afterMetadataSet()
                alertPlaybackStateChanged(isMediaPlaying)
                Log.d("notificationPosted: Updated current track")
            },
            onTrackIsSimilarCallbackTrack = { track ->
                // We definitely know that whenever the notification bar changes a bit, we will get a state
                // update which means we have a valid reason to query if music is playing or not.
                alertPlaybackStateChanged(isMediaPlaying)
                Log.d("notificationPosted: metadata is already updated, playback state changed.")
            },
            onTrackIsSimilarNotificationTrack = { track ->
                // Same as above.
                alertPlaybackStateChanged(isMediaPlaying)
                Log.d("notificationPosted: track is similar, metadata is about the same, playback state changed.")
            }
        )
    }
    
    fun alertMediaPlayerRemoved(notification: StatusBarNotification) {
        Log.d("Removed " + notification.notification.extras)
    }
    
    /** Toggle timer based on state. */
    fun alertPlaybackStateChanged(isMediaPlaying: Boolean) {
        if (playingTrack.isSubmitted()) return

        if (isMediaPlaying) {
            timer.startOrResume()
            Log.d("Play")
        } else {
            timer.pause()
            Log.d("Pause")
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
                roundDuration(duration = DEFAULT_DURATION)
            )
        }
        Log.d("Timer Set")
    }
    
    // Utility functions
    
    private fun roundDuration(duration: Long): Long =
        (duration / 1000) * 1000
    
    private fun submitListen(listenType: ListenType) =
        workManager.enqueue(buildWorkRequest(playingTrack, listenType))
    
    private fun isMetadataFaulty(): Boolean = playingTrack.artist.isNullOrEmpty() || playingTrack.title.isNullOrEmpty()
    
    /** Discard current listen.*/
    fun discardCurrentListen() {
        timer.stop()
        playingTrack = PlayingTrack()
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
