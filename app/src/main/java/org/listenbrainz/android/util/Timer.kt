package org.listenbrainz.android.util

import android.os.Handler
import android.os.SystemClock
import android.util.Log
import org.listenbrainz.android.model.OnTimerListener
import org.listenbrainz.android.model.TimerState

interface Timer {
    fun startOrResume(delay: Long = 0L)

    fun setDuration(duration: Long)

    fun setOnTimerListener(listener: OnTimerListener)

    fun extendDuration(extensionSeconds: (passedSeconds: Long) -> Long)

    fun pause()

    fun stop()
}

/** **NOT** thread safe.*/
abstract class TimerBase: Timer {
    companion object {
        private const val MESSAGE_TOKEN = 69
        private const val TAG = "Timer"
    }
    
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
        Log.d(TAG, "setOnTimerListener: ")
    }
    
    protected fun startOrResume(
        delay: Long = 0L,
        postDelayed: (duration: Long, token: Int, block: () -> Unit) -> Unit
    ) {
        when (mState) {
            TimerState.RUNNING -> return
            TimerState.PAUSED -> {
                mResumeTs = SystemClock.uptimeMillis()
                
                postDelayed(
                    mDurationLeft,
                    MESSAGE_TOKEN,
                ) { end() }
                Log.d(TAG,"Timer resumed")
                
                mListener?.onTimerResumed()
                mState = TimerState.RUNNING
            }
            TimerState.ENDED -> {
                mResumeTs = SystemClock.uptimeMillis()
                mDurationLeft += delay
                
                postDelayed(
                    mDurationLeft,
                    MESSAGE_TOKEN,
                ) { end() }
                Log.d(TAG,"Timer started")
    
                mListener?.onTimerStarted()
                mState = TimerState.RUNNING
            }
        }
    }
    
    private fun end() {
        if (mState == TimerState.ENDED) {
            return
        }
        mState = TimerState.ENDED
        mListener?.onTimerEnded()
        reset()
    }
    
    /** Discard current listen post and stop timer.*/
    protected fun stop(removePosts: (Int) -> Unit) {
        if (mState == TimerState.ENDED) {
            return
        }
        mState = TimerState.ENDED
        removePosts(MESSAGE_TOKEN)
        reset()
    }
    
    protected fun extendDuration(
        extensionSeconds: (passedSeconds: Long) -> Long,
        removePosts: (Int) -> Unit,
        postDelayed: (duration: Long, token: Int, block: () -> Unit) -> Unit,
    ) {
        pause(removePosts)
        mDurationLeft = extensionSeconds(/*passedSeconds =*/mInitialDuration - mDurationLeft)
        startOrResume(postDelayed = postDelayed)
    }
    
    protected fun pause(removePosts: (Int) -> Unit) {
        Log.d(TAG,"Timer paused")
        if (mState == TimerState.PAUSED || mState == TimerState.ENDED) {
            return
        }
        mState = TimerState.PAUSED
        removePosts(MESSAGE_TOKEN)
        
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
    private val jobQueue: JobQueue
): TimerBase() {
    override fun startOrResume(delay: Long) = startOrResume(delay) { duration, token, block ->
        jobQueue.postDelayed(duration, token) { block() }
    }

    override fun stop() = stop { jobQueue.removePosts(it) }

    override fun extendDuration(extensionSeconds: (passedSeconds: Long) -> Long) = extendDuration(
        extensionSeconds = extensionSeconds,
        removePosts = { jobQueue.removePosts(it) },
        postDelayed = { duration, token, block ->
            jobQueue.postDelayed(duration, token) { block() }
        }
    )

    override fun pause() = pause { jobQueue.removePosts(it) }
}


class TimerHandler(
    private val handler: Handler
): TimerBase() {
    override fun startOrResume(delay: Long) = startOrResume(delay) { duration, token, block ->
        handler.postAtTime(
            { block() },
            token,
            duration,
        )
    }

    override fun stop() = stop { handler.removeCallbacksAndMessages(it) }

    override fun extendDuration(extensionSeconds: (passedSeconds: Long) -> Long) = extendDuration(
        extensionSeconds = extensionSeconds,
        removePosts = { handler.removeCallbacksAndMessages(it) },
        postDelayed = { duration, token, block ->
            handler.postAtTime(
                { block() },
                token,
                duration,
            )
        }
    )

    override fun pause() = pause { handler.removeCallbacksAndMessages(it) }
}