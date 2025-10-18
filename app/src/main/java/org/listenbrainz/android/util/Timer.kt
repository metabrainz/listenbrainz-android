package org.listenbrainz.android.util

import android.os.Handler
import android.os.SystemClock
import org.listenbrainz.android.model.OnTimerListener
import org.listenbrainz.android.model.TimerState

interface Timer {
    val state: TimerState

    val durationLeft: Long

    val initialDuration: Long

    fun startOrResume(delay: Long = 0L)

    fun setDuration(duration: Long)

    fun setOnTimerListener(listener: OnTimerListener)

    fun extendDuration(extensionSeconds: (passedSeconds: Long) -> Long)

    fun pause()

    fun stop()

    fun end()
}

/** **NOT** thread safe.*/
abstract class TimerBase: Timer {
    final override var state: TimerState = TimerState.ENDED
        private set
    final override var durationLeft: Long = 0L
        private set
    final override var initialDuration = 0L
        private set

    private var mListener: OnTimerListener? = null
    private var mResumeTs: Long = 0
    
    override fun setDuration(duration: Long) {
        initialDuration = duration
        durationLeft = duration
    }
    
    override fun setOnTimerListener(listener: OnTimerListener) {
        mListener = listener
    }
    
    protected fun startOrResume(
        delay: Long = 0L,
        postDelayed: (durationLeft: Long, block: () -> Unit) -> Unit
    ) {
        when (state) {
            TimerState.RUNNING -> return
            TimerState.PAUSED -> {
                mResumeTs = SystemClock.uptimeMillis()

                postDelayed(
                    durationLeft,
                    ::end
                )
                
                mListener?.onTimerResumed()
                state = TimerState.RUNNING
            }
            TimerState.ENDED -> {
                mResumeTs = SystemClock.uptimeMillis()
                durationLeft += delay
                
                postDelayed(
                    durationLeft,
                    ::end
                )
    
                mListener?.onTimerStarted()
                state = TimerState.RUNNING
            }
        }
    }
    
    override fun end() {
        if (state == TimerState.ENDED) {
            return
        }
        state = TimerState.ENDED
        mListener?.onTimerEnded()
        reset()
    }
    
    /** Discard current listen post and stop timer.*/
    protected fun stop(removePosts: () -> Unit) {
        if (state == TimerState.ENDED) {
            return
        }
        state = TimerState.ENDED
        removePosts()
        reset()
    }
    
    protected fun extendDuration(
        extensionSeconds: (passedSeconds: Long) -> Long,
        removePosts: () -> Unit,
        postDelayed: (duration: Long, block: () -> Unit) -> Unit,
    ) {
        pause(removePosts)
        durationLeft = extensionSeconds(/*passedSeconds =*/initialDuration - durationLeft)
        startOrResume(postDelayed = postDelayed)
    }
    
    protected fun pause(removePosts: () -> Unit) {
        if (state == TimerState.PAUSED || state == TimerState.ENDED) {
            return
        }
        state = TimerState.PAUSED
        removePosts()
        
        val durationLeft = durationLeft - (SystemClock.uptimeMillis() - mResumeTs)
        mListener?.onTimerPaused(durationLeft)
        this@TimerBase.durationLeft = durationLeft
    }
    
    private fun reset() {
        mResumeTs = 0L
        durationLeft = 0L
        initialDuration = 0L
    }
}


class TimerJQ(
    private val jobQueue: JobQueue,
    private val uniqueToken: Any
): TimerBase() {
    override fun startOrResume(delay: Long) = startOrResume(delay) { durationLeft, block ->
        jobQueue.postDelayed(durationLeft, uniqueToken) { block() }
    }

    override fun stop() = stop { jobQueue.removePosts(uniqueToken) }

    override fun extendDuration(extensionSeconds: (passedSeconds: Long) -> Long) = extendDuration(
        extensionSeconds = extensionSeconds,
        removePosts = { jobQueue.removePosts(uniqueToken) },
        postDelayed = { duration, block ->
            jobQueue.postDelayed(duration, uniqueToken) { block() }
        }
    )

    override fun pause() = pause { jobQueue.removePosts(uniqueToken) }
}


class TimerHandler(
    private val handler: Handler,
    private val uniqueToken: Any
): TimerBase() {
    override fun startOrResume(delay: Long) = startOrResume(delay) { durationLeft, block ->
        handler.postAtTime(
            { block() },
            uniqueToken,
            SystemClock.uptimeMillis() + durationLeft,
        )
    }

    override fun stop() = stop { handler.removeCallbacksAndMessages(uniqueToken) }

    override fun extendDuration(extensionSeconds: (passedSeconds: Long) -> Long) = extendDuration(
        extensionSeconds = extensionSeconds,
        removePosts = { handler.removeCallbacksAndMessages(uniqueToken) },
        postDelayed = { duration,  block ->
            handler.postAtTime(
                { block() },
                uniqueToken,
                SystemClock.uptimeMillis() + duration,
            )
        }
    )

    override fun pause() = pause { handler.removeCallbacksAndMessages(uniqueToken) }
}
