package org.listenbrainz.android.util

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadata
import android.os.Handler
import android.service.notification.StatusBarNotification
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import org.listenbrainz.android.R
import org.listenbrainz.android.model.ListenType
import org.listenbrainz.android.model.OnTimerListener
import org.listenbrainz.android.model.PlayingTrack
import org.listenbrainz.android.service.ListenSubmissionService.Companion.CHANNEL_ID
import org.listenbrainz.android.service.ListenSubmissionService.Companion.NOTIFICATION_ID
import org.listenbrainz.android.service.ListenSubmissionService.Companion.serviceNotification
import org.listenbrainz.android.service.ListenSubmissionWorker.Companion.buildWorkRequest
import org.listenbrainz.android.ui.screens.main.MainActivity

open class ListenSubmissionState {
    var playingTrack: PlayingTrack = PlayingTrack()
        private set
    private val timer: Timer
    private val workManager: WorkManager
    private val context: Context
    private val notificationManager: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(context)
    }

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
                Log.d("${remainingMillis / 1000} seconds left to submit: ${playingTrack.debugId}")
            }
        })
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

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(
                NOTIFICATION_ID,
                context.getListeningNotification(playingTrack)
            )
        }
    }

    fun onNewMetadata(
        newTrack: PlayingTrack,
        isMediaPlaying: Boolean
    ) {
        if (playingTrack.isOutdated(newTrack)) {
            timer.stop()
            
            if (playingTrack.isSimilarTo(newTrack) && newTrack.isDurationAbsent()) {
                playingTrack = newTrack.apply {
                    duration = playingTrack.duration
                }
            } else {
                playingTrack = newTrack
            }
            
            afterMetadataSet()
        } else if (playingTrack.isSimilarTo(newTrack)
            && playingTrack.isDurationAbsent()
            && newTrack.isDurationPresent()
        ) {
            // Update duration as it was absent before
            playingTrack.duration = newTrack.duration
            timer.extendDuration { secondsPassed ->
                newTrack.duration / 2 - secondsPassed
            }

            // Force submit a playing now because have updated metadata now.
            Log.d("Force submitting playing now: ${playingTrack.debugId}")
            playingTrack.playingNowSubmitted = false
            submitPlayingNow()
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
            Log.d("Play: ${playingTrack.debugId}")
        } else {
            timer.pause()
            Log.d("Pause: ${playingTrack.debugId}")
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
        Log.d("Timer Set: ${playingTrack.debugId}")
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

        fun Context.getListeningNotification(playingTrack: PlayingTrack): Notification {
            val context = this

            val clickPendingIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat
                .Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_listenbrainz_logo_no_text)
                .setContentTitle("Listening...")
                .setContentText(playingTrack.title + " by " + playingTrack.artist)

                //.setColorized(true)
                //.setColor(lb_purple.toArgb())
                .setContentIntent(clickPendingIntent)

                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .build()

            return notification
        }
    }
}
