package org.listenbrainz.android.util

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import org.listenbrainz.android.model.OnTimerListener
import org.listenbrainz.android.model.TimerState

class Timer {
    companion object {
        private const val MESSAGE_TOKEN = 69
        private const val TAG = "Timer"
    }
    
    private val mHandler: Handler by lazy { Handler(Looper.getMainLooper()) }
    
    private var mState: TimerState = TimerState.ENDED
    private var mListener: OnTimerListener? = null
    
    private var mInitialDuration = 0L
    private var mResumeTs: Long = 0
    private var mDurationLeft: Long = 0L
    
    fun setDuration(duration: Long) {
        mInitialDuration = duration
        this.mDurationLeft = duration
    }
    
    fun setOnTimerListener(listener: OnTimerListener) {
        this.mListener = listener
        android.util.Log.d(TAG, "setOnTimerListener: ")
    }
    
    fun startOrResume(delay: Long = 0L) {
        when (mState) {
            TimerState.RUNNING -> return
            TimerState.PAUSED -> {
                mResumeTs = SystemClock.uptimeMillis()
                
                mHandler.postAtTime(
                    { end() },
                    MESSAGE_TOKEN,
                    mResumeTs + mDurationLeft
                )
                Log.d("Timer resumed")
                
                mListener?.onTimerResumed()
                mState = TimerState.RUNNING
            }
            TimerState.ENDED -> {
                mResumeTs = SystemClock.uptimeMillis()
                
                mHandler.postAtTime(
                    { end() },
                    MESSAGE_TOKEN,
                    mResumeTs + mDurationLeft + delay
                )
                Log.d("Timer started")
    
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
        mHandler.removeCallbacksAndMessages(MESSAGE_TOKEN)
        reset()
    }
    
    fun extendDuration(extensionSeconds: (passedSeconds: Long) -> Long) {
        pause()
        mDurationLeft += extensionSeconds(/*passedSeconds =*/mInitialDuration - mDurationLeft)
        startOrResume()
    }
    
    fun pause() {
        Log.d("Timer paused")
        if (mState == TimerState.PAUSED || mState == TimerState.ENDED) {
            return
        }
        mState = TimerState.PAUSED
        mHandler.removeCallbacksAndMessages(MESSAGE_TOKEN)
        
        val durationLeft = mDurationLeft - (SystemClock.uptimeMillis() - mResumeTs)
        mListener?.onTimerPaused(durationLeft)
        this.mDurationLeft = durationLeft
    }
    
    private fun reset() {
        mResumeTs = 0L
        mDurationLeft = 0L
        mInitialDuration = 0L
    }
}