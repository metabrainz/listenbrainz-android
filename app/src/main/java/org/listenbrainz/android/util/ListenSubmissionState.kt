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

open class ListenSubmissionState {
    var playingTrack: PlayingTrack = PlayingTrack()
        private set
    private val timer: Timer
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
    
    fun init() {
        // Setting listener
        timer.setOnTimerListener(listener = object : OnTimerListener {
            override fun onTimerEnded() {
                submitListen()
            }
            
            override fun onTimerPaused(remainingMillis: Long) {
                Log.d("${remainingMillis / 1000} seconds left to submit listen.")
            }
        })
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
        submitPlayingNow()
    }

    fun onNewTrackDiscovered(
        newTrack: PlayingTrack,
        isMediaPlaying: Boolean
    ) {
        if (playingTrack.isOutdated(newTrack)) {
            beforeMetadataSet()
            playingTrack = newTrack
            afterMetadataSet()
        } else if (playingTrack.isDurationAbsent() && newTrack.isDurationPresent()) {
            // Update if duration is absent
            playingTrack.duration = newTrack.duration
            timer.extendDuration { secondsPassed ->
                playingTrack.duration / 2 - secondsPassed
            }
        }

        alertPlaybackStateChanged(isMediaPlaying)
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

    private fun submitPlayingNow() {
        if (!playingTrack.playingNowSubmitted) {
            workManager.enqueue(buildWorkRequest(playingTrack, ListenType.PLAYING_NOW))
            playingTrack.playingNowSubmitted = true
        }
    }

    private fun submitListen() {
        if (!playingTrack.submitted) {
            workManager.enqueue(buildWorkRequest(playingTrack, ListenType.SINGLE))
            playingTrack.submitted = true
        }
    }

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
    
        fun MediaMetadata.extractReleaseName(): String? =
            getString(MediaMetadata.METADATA_KEY_ALBUM)
                .takeIf { !it.isNullOrEmpty() }
                ?: getString(MediaMetadata.METADATA_KEY_COMPILATION)
    }
}
