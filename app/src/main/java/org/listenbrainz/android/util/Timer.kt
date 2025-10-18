package org.listenbrainz.android.util

import android.os.Handler
import android.os.SystemClock
import android.util.Log
import org.listenbrainz.android.model.OnTimerListener
import org.listenbrainz.android.model.TimerState
import kotlin.random.Random

interface Timer {
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
    private var mState: TimerState = TimerState.ENDED
    private var mListener: OnTimerListener? = null
    
    private var mInitialDuration = 0L
    private var mResumeTs: Long = 0
    private var mDurationLeft: Long = 0L
    
    override fun setDuration(duration: Long) {
        mInitialDuration = duration
        mDurationLeft = duration
    }
    
    override fun setOnTimerListener(listener: OnTimerListener) {
        mListener = listener
    }
    
    protected fun startOrResume(
        delay: Long = 0L,
        postDelayed: (durationLeft: Long, block: () -> Unit) -> Unit
    ) {
        when (mState) {
            TimerState.RUNNING -> return
            TimerState.PAUSED -> {
                mResumeTs = SystemClock.uptimeMillis()

                postDelayed(
                    mDurationLeft,
                    ::end
                )
                
                mListener?.onTimerResumed()
                mState = TimerState.RUNNING
            }
            TimerState.ENDED -> {
                mResumeTs = SystemClock.uptimeMillis()
                mDurationLeft += delay
                
                postDelayed(
                    mDurationLeft,
                    ::end
                )
    
                mListener?.onTimerStarted()
                mState = TimerState.RUNNING
            }
        }
    }
    
    override fun end() {
        if (mState == TimerState.ENDED) {
            return
        }
        mState = TimerState.ENDED
        mListener?.onTimerEnded()
        reset()
    }
    
    /** Discard current listen post and stop timer.*/
    protected fun stop(removePosts: () -> Unit) {
        if (mState == TimerState.ENDED) {
            return
        }
        mState = TimerState.ENDED
        removePosts()
        reset()
    }
    
    protected fun extendDuration(
        extensionSeconds: (passedSeconds: Long) -> Long,
        removePosts: () -> Unit,
        postDelayed: (duration: Long, block: () -> Unit) -> Unit,
    ) {
        pause(removePosts)
        mDurationLeft = extensionSeconds(/*passedSeconds =*/mInitialDuration - mDurationLeft)
        startOrResume(postDelayed = postDelayed)
    }
    
    protected fun pause(removePosts: () -> Unit) {
        if (mState == TimerState.PAUSED || mState == TimerState.ENDED) {
            return
        }
        mState = TimerState.PAUSED
        removePosts()
        
        val durationLeft = mDurationLeft - (SystemClock.uptimeMillis() - mResumeTs)
        mListener?.onTimerPaused(durationLeft)
        mDurationLeft = durationLeft
    }
    
    private fun reset() {
        mResumeTs = 0L
        mDurationLeft = 0L
        mInitialDuration = 0L
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
