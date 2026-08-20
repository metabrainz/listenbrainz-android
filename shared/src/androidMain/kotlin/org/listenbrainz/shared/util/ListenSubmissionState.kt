package org.listenbrainz.shared.util

import android.annotation.SuppressLint
import android.os.Handler
import dev.brewkits.kmpworkmanager.background.domain.BackgroundTaskScheduler
import dev.brewkits.kmpworkmanager.background.domain.Constraints
import dev.brewkits.kmpworkmanager.background.domain.TaskTrigger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.listenbrainz.shared.applicationContext
import org.listenbrainz.shared.model.ListenType
import org.listenbrainz.shared.model.ListenWorkerInput
import org.listenbrainz.shared.model.OnTimerListener
import org.listenbrainz.shared.model.PlayingTrack
import org.listenbrainz.shared.model.TimerState
import org.listenbrainz.shared.repository.PlatformContext
import org.listenbrainz.shared.util.ListenSubmissionStateConstants.DEFAULT_DURATION
import org.listenbrainz.shared.util.ListenSubmissionStateConstants.MAX_SUBMISSION_DURATION
import org.listenbrainz.shared.util.ListenSubmissionStateConstants.SUBMISSION_TIMER_TOKEN
import org.listenbrainz.shared.util.ListenSubmissionStateConstants.TRACK_COMPLETION_TIMER_TOKEN
import org.listenbrainz.shared.util.PlatformUtils.canShowNotifications

open class ListenSubmissionState : KoinComponent {
    var playingTrack: PlayingTrack = PlayingTrack()
        private set
    private val submissionTimer: Timer
    private val trackCompletionTimer: Timer
    private val scheduler: BackgroundTaskScheduler
    private var context: PlatformContext = applicationContext
    private val logger : Log
    private val notificationManager: PlatformNotificationManager by inject()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    constructor(jobQueue: JobQueue = JobQueue(Dispatchers.Default), scheduler: BackgroundTaskScheduler, context: PlatformContext, logger:Log = Log) {
        this.submissionTimer = TimerJQ(jobQueue, SUBMISSION_TIMER_TOKEN)
        this.trackCompletionTimer = TimerJQ(jobQueue, TRACK_COMPLETION_TIMER_TOKEN)
        this.scheduler = scheduler
        this.context = context
        this.logger = logger
        init()
    }

    constructor(handler: Handler, scheduler: BackgroundTaskScheduler, context: PlatformContext, logger:Log = Log) {
        this.submissionTimer = TimerHandler(handler, SUBMISSION_TIMER_TOKEN)
        this.trackCompletionTimer = TimerHandler(handler, TRACK_COMPLETION_TIMER_TOKEN)
        this.scheduler = scheduler
        this.context = context
        this.logger = logger
        init()
    }

    fun init() {
        // Setting listener
        submissionTimer.setOnTimerListener(listener = object : OnTimerListener {
            override fun onTimerEnded() {
                submitListen()
            }

            override fun onTimerPaused(remainingMillis: Long) {
                logger.d("${remainingMillis / 1000} seconds left to submit: ${playingTrack.id}")
            }
        })

        @SuppressLint("MissingPermission")
        trackCompletionTimer.setOnTimerListener(listener = object : OnTimerListener {
            override fun onTimerStarted() {
                scope.launch {
                    if (canShowNotifications()) {
                        notificationManager.postListeningNotification(playingTrack)
                    }
                }
            }

            override fun onTimerResumed() = onTimerStarted()

            override fun onTimerEnded() {
                // Make notification null
                logger.d("Track completion timer ended: ${playingTrack.id}")
                scope.launch {
                    if (canShowNotifications()) {
                        notificationManager.postListeningNotification(null)
                    }
                }
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun afterMetadataSet() {
        // After metadata set
        if (isMetadataFaulty()) {
            logger.w("Metadata is faulty, listen cancelled: $playingTrack")
            playingTrack = PlayingTrack.Nothing
            return
        }

        initTimer()
        submitPlayingNow()
    }

    fun onNewMetadata(
        newTrack: PlayingTrack,
        isMediaPlaying: Boolean
    ) {
        if (newTrack.isValid) {
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
                logger.d("Force submitting playing now: ${playingTrack.id}")
                playingTrack.playingNowSubmitted = false
                submitPlayingNow()
            }
        }

        alertPlaybackStateChanged(isMediaPlaying)
    }

    @SuppressLint("MissingPermission")
    fun alertMediaPlayerRemoved(packageName: String) {
        scope.launch {
            if (canShowNotifications() && packageName == playingTrack.pkgName) {
                logger.d("Media player for $packageName removed, cleaning up notification.")
                notificationManager.postListeningNotification(null)
            }
        }
    }

    /** Toggle timer based on state. */
    fun alertPlaybackStateChanged(isMediaPlaying: Boolean) {
        if (playingTrack.isSubmitted()) return

        if (isMediaPlaying) {
            submissionTimer.startOrResume()

            if (trackCompletionTimer.state == TimerState.ENDED) {
                trackCompletionTimer.setDuration(
                    // submission timer's initial duration will always be half of trackCompletionTimer
                    // So, the following calculation gives us the time when the song will end if played continuously.
                    submissionTimer.initialDuration + submissionTimer.durationLeft
                )
            }
            trackCompletionTimer.startOrResume()
            logger.d("Play: ${playingTrack.id}")
        } else {
            submissionTimer.pause()
            logger.d("Pause: ${playingTrack.id}")
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
                roundDuration(duration = DEFAULT_DURATION * 2)
            )
        }
        logger.d("Timer Set: ${playingTrack.id}")
    }
    
    // Utility functions
    
    private fun roundDuration(duration: Long): Long =
        (duration / 1000) * 1000

    private fun submitPlayingNow() {
        if (!playingTrack.playingNowSubmitted) {
            val track = playingTrack
            scope.launch {
                scheduler.enqueue(
                    id = "playing-now-${track.pkgName}-${System.currentTimeMillis()}",
                    trigger = TaskTrigger.OneTime(initialDelayMs = 0),
                    workerClassName = "ListenSubmissionWorker",
                    inputJson = Json.encodeToString(
                        ListenWorkerInput(track, ListenType.PLAYING_NOW)
                    ),
                    constraints = Constraints(requiresNetwork = true)
                )
            }
            track.playingNowSubmitted = true
        }
    }

    private fun submitListen() {
        if (!playingTrack.submitted) {
            val track = playingTrack
           scope.launch {
               scheduler.enqueue(
                   id = "listen-${track.id}-${System.currentTimeMillis()}",
                   trigger = TaskTrigger.OneTime(initialDelayMs = 0),
                   workerClassName = "ListenSubmissionWorker",
                   inputJson = Json.encodeToString(
                       ListenWorkerInput(track, ListenType.SINGLE)
                   ),
                   constraints = Constraints(requiresNetwork = true)
               )
           }
            track.submitted = true
        }
    }

    private fun isMetadataFaulty(): Boolean = playingTrack.artist.isNullOrEmpty() || playingTrack.title.isNullOrEmpty()
    
    /** Discard current listen.*/
    fun discardCurrentListen() {
        submissionTimer.stop()
        trackCompletionTimer.stop()
        playingTrack = PlayingTrack.Nothing
    }
}