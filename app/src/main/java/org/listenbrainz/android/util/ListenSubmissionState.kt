package org.listenbrainz.android.util

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.os.Handler
import android.service.notification.StatusBarNotification
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
import org.listenbrainz.android.service.ListenSubmissionWorker.Companion.buildWorkRequest
import org.listenbrainz.android.ui.screens.main.MainActivity
import org.listenbrainz.android.util.Utils.canShowNotifications

open class ListenSubmissionState {
    var playingTrack: PlayingTrack = PlayingTrack()
        private set
    private val submissionTimer: Timer
    private val trackCompletionTimer: Timer
    private val workManager: WorkManager
    private val context: Context
    private val notificationManager: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(context)
    }

    constructor(jobQueue: JobQueue = JobQueue(Dispatchers.Default), workManager: WorkManager, context: Context) {
        this.submissionTimer = TimerJQ(jobQueue, SUBMISSION_TIMER_TOKEN)
        this.trackCompletionTimer = TimerJQ(jobQueue, TRACK_COMPLETION_TIMER_TOKEN)
        this.workManager = workManager
        this.context = context

        init()
    }

    constructor(handler: Handler, workManager: WorkManager, context: Context) {
        this.submissionTimer = TimerHandler(handler, SUBMISSION_TIMER_TOKEN)
        this.trackCompletionTimer = TimerHandler(handler, TRACK_COMPLETION_TIMER_TOKEN)
        this.workManager = workManager
        this.context = context

        init()
    }
    
    fun init() {
        // Setting listener
        submissionTimer.setOnTimerListener(listener = object : OnTimerListener {
            override fun onTimerEnded() {
                submitListen()
            }
            
            override fun onTimerPaused(remainingMillis: Long) {
                Log.d("${remainingMillis / 1000} seconds left to submit: ${playingTrack.id}")
            }
        })

        @SuppressLint("MissingPermission")
        trackCompletionTimer.setOnTimerListener(listener = object : OnTimerListener {
            override fun onTimerStarted() {
                if (context.canShowNotifications) {
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        context.getListeningNotification(playingTrack)
                    )
                }
            }

            override fun onTimerResumed() = onTimerStarted()

            override fun onTimerEnded() {
                // Make notification null
                Log.d("Track completion timer ended: ${playingTrack.id}")
                if (context.canShowNotifications) {
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        context.getListeningNotification(null)
                    )
                }
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun afterMetadataSet() {
        // After metadata set
        if (isMetadataFaulty()) {
            Log.w("Metadata is faulty, listen cancelled: $playingTrack")
            playingTrack.reset()
            return
        }

        initTimer()
        submitPlayingNow()
    }

    fun onNewMetadata(
        newTrack: PlayingTrack,
        isMediaPlaying: Boolean
    ) {
        if (playingTrack.isOutdated(newTrack)) {
            submissionTimer.stop()
            trackCompletionTimer.stop()
            
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
            submissionTimer.extendDuration { secondsPassed ->
                newTrack.duration / 2 - secondsPassed
            }
            trackCompletionTimer.extendDuration { secondsPassed ->
                newTrack.duration - secondsPassed
            }

            // Force submit a playing now because have updated metadata now.
            Log.d("Force submitting playing now: ${playingTrack.id}")
            playingTrack.playingNowSubmitted = false
            submitPlayingNow()
        }

        alertPlaybackStateChanged(isMediaPlaying)
    }
    
    @SuppressLint("MissingPermission")
    fun alertMediaPlayerRemoved(packageName: String) {
        if (context.canShowNotifications && packageName == playingTrack.pkgName) {
            Log.d("Media player for $packageName removed, cleaning up notification.")
            notificationManager.notify(
                NOTIFICATION_ID,
                context.getListeningNotification(null)
            )
        }
    }
    
    /** Toggle timer based on state. */
    fun alertPlaybackStateChanged(isMediaPlaying: Boolean) {
        if (playingTrack.isSubmitted()) return

        if (isMediaPlaying) {
            submissionTimer.startOrResume()
            trackCompletionTimer.startOrResume()
            Log.d("Play: ${playingTrack.id}")
        } else {
            submissionTimer.pause()
            trackCompletionTimer.pause()
            Log.d("Pause: ${playingTrack.id}")
        }
    }
    
    /** Run [artist] and [title] value-check before invoking this function.*/
    private fun initTimer() {
        if (playingTrack.duration != 0L) {
            submissionTimer.setDuration(
                roundDuration(duration = playingTrack.duration / 2L)
                    .coerceAtMost(MAX_SUBMISSION_DURATION)
            )
            trackCompletionTimer.setDuration(
                roundDuration(duration = playingTrack.duration)
            )
        } else {
            submissionTimer.setDuration(
                roundDuration(duration = DEFAULT_DURATION)
            )
            trackCompletionTimer.setDuration(
                roundDuration(duration = DEFAULT_DURATION)
            )
        }
        Log.d("Timer Set: ${playingTrack.id}")
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
        submissionTimer.stop()
        trackCompletionTimer.stop()
        playingTrack = PlayingTrack()
    }
    
    companion object {
        const val DEFAULT_DURATION: Long = 60_000L

        /** Max time required to validate a listen as submittable listen is 4 minutes. */
        const val MAX_SUBMISSION_DURATION: Long = 240_000L
        const val SUBMISSION_TIMER_TOKEN = 69
        const val TRACK_COMPLETION_TIMER_TOKEN = 420
        
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

        fun Context.getListeningNotification(playingTrack: PlayingTrack?): Notification {
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
                .let {
                    if (playingTrack != null && !playingTrack.isNothing()) {
                        it.setContentText(playingTrack.title + " by " + playingTrack.artist)
                    } else {
                        it
                    }
                }

                //.setColorized(true)
                //.setColor(lb_purple.toArgb())
                .setContentIntent(clickPendingIntent)

                .setSound(null)
                .setOngoing(true)
                .setAutoCancel(false)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .build()

            return notification
        }
    }
}
