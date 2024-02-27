package org.listenbrainz.android.util

import android.os.SystemClock
import android.util.Log
import org.listenbrainz.android.model.OnTimerListener
import org.listenbrainz.android.model.TimerState

/** **NOT** thread safe.*/
class Timer(private val jobQueue: JobQueue) {
    companion object {
        private const val MESSAGE_TOKEN = 69
        private const val TAG = "Timer"
    }
    
    private var mState: TimerState = TimerState.ENDED
    private var mListener: OnTimerListener? = null
    
    private var mInitialDuration = 0L
    private var mResumeTs: Long = 0
    private var mDurationLeft: Long = 0L
    
    fun setDuration(duration: Long) {
        mInitialDuration = duration
        mDurationLeft = duration
    }
    
    fun setOnTimerListener(listener: OnTimerListener) {
        mListener = listener
        Log.d(TAG, "setOnTimerListener: ")
    }
    
    fun startOrResume(delay: Long = 0L) {
        when (mState) {
            TimerState.RUNNING -> return
            TimerState.PAUSED -> {
                mResumeTs = SystemClock.uptimeMillis()
                
                jobQueue.postDelayed(
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
                
                jobQueue.postDelayed(
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
    fun stop() {
        if (mState == TimerState.ENDED) {
            return
        }
        mState = TimerState.ENDED
        jobQueue.removePosts(MESSAGE_TOKEN)
        reset()
    }
    
    fun extendDuration(extensionSeconds: (passedSeconds: Long) -> Long) {
        pause()
        mDurationLeft = extensionSeconds(/*passedSeconds =*/mInitialDuration - mDurationLeft)
        startOrResume()
    }
    
    fun pause() {
        Log.d(TAG,"Timer paused")
        if (mState == TimerState.PAUSED || mState == TimerState.ENDED) {
            return
        }
        mState = TimerState.PAUSED
        jobQueue.removePosts(MESSAGE_TOKEN)
        
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