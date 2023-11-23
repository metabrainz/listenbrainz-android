package org.listenbrainz.android.util

import android.os.Handler
import android.os.Looper
import org.listenbrainz.android.model.OnTimerListener
import org.listenbrainz.android.model.Status
import java.util.Timer
import kotlin.concurrent.fixedRateTimer

class Timer {
    
    private val handler: Handler by lazy { Handler(Looper.getMainLooper()) }
    
    private var timer: Timer? = null
    private var status: Status? = null
    private var listener: OnTimerListener? = null
    
    private var initialTimerDuration = 0L
    private var currentDuration = 0L
    private var startDelay = 0L
    private var isDaemon = false
    private var callbacksOnMainThread = false
    
    fun setDuration(duration: Long) {
        initialTimerDuration = duration
        currentDuration = duration
    }
    
    fun setIsDaemon(isDaemon: Boolean) {
        this.isDaemon = isDaemon
    }
    
    fun setStartDelay(delay: Long) {
        this.startDelay = delay
    }
    
    fun setOnTimerListener(listener: OnTimerListener, callbacksOnMainThread: Boolean) {
        this.listener = listener
        this.callbacksOnMainThread = callbacksOnMainThread
    }
    
    fun start() {
        
        if (status == Status.RUN) {
            return
        }
        
        // When the status is end or stop I must reinitialize the duration to initial duration.
        if (status == Status.END || status == Status.STOP) {
            currentDuration = initialTimerDuration
            status = null
        }
        
        val delay = when (status) {
            null -> {
                startDelay
            }
            else -> {
                0
            }
        }
        
        status = Status.START
        
        timer = fixedRateTimer("timer", isDaemon, delay, 1000) {
            
            currentDuration -= 1_000
            
            // When I arrive to -1 it means that all the milliseconds at 0 seconds are passed.
            if (currentDuration <= -1_000L) {
                end()
                return@fixedRateTimer
            }
            
            if (status == Status.START) {
                execute {
                    listener?.onTimerStarted()
                }
            }
            
            status = Status.RUN
            
            execute {
                listener?.onTimerRun(currentDuration)
            }
        }
    }
    
    private fun execute(f: () -> Unit) {
        if (callbacksOnMainThread) {
            handler.post {
                f.invoke()
            }
        } else {
            f.invoke()
        }
    }
    
    private fun end() {
        if (status == Status.END) {
            return
        }
        status = Status.END
        recycle()
        execute {
            listener?.onTimerEnded()
        }
    }
    
    fun stop() {
        if (status == Status.STOP) {
            return
        }
        status = Status.STOP
        recycle()
        execute {
            listener?.onTimerStopped()
        }
    }
    
    fun extendDuration(seconds: Long) {
        currentDuration += seconds
    }
    
    fun pause() {
        if (status == Status.STOP || status == Status.PAUSE || status == Status.END) {
            return
        }
        status = Status.PAUSE
        timer?.apply {
            cancel()
            purge()
        }
        execute {
            listener?.onTimerPaused(currentDuration)
        }
    }
    
    private fun recycle() {
        timer?.apply {
            cancel()
            purge()
        }
        timer = null
        currentDuration = 0
    }
}