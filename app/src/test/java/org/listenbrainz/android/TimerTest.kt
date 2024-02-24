package org.listenbrainz.android

import android.os.Handler
import android.os.Looper
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.listenbrainz.android.model.OnTimerListener
import org.listenbrainz.android.model.TimerState
import org.listenbrainz.android.util.JobQueue
import org.listenbrainz.android.util.Timer
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class TimerTest: BaseUnitTest() {
    
    private lateinit var timer: Timer
    private lateinit var timerState: TimerState
    private var isEnded = false
    private var isPaused = false
    private var isStarted: Boolean = false
    private var remainingMs: Long? = null
    
    @Before
    fun setup() {
        val testHandler = Handler(Looper.getMainLooper())
        timer = Timer(JobQueue(testDispatcher()))
        
        timerState = TimerState.ENDED
        
        timer.setOnTimerListener(listener = object : OnTimerListener {
            override fun onTimerEnded() {
                timerState = TimerState.ENDED
            }
            
            override fun onTimerPaused(remainingMillis: Long) {
                timerState = TimerState.PAUSED
                remainingMs = remainingMillis
            }
            
            override fun onTimerStarted() {
                TimerState.RUNNING
            }
            
            override fun onTimerResumed() {
                TimerState.RUNNING
            }
        })
    }
    
    @Test
    fun `test flow`() = test {
        val duration = 4L
        timer.setDuration(duration)
        
        timer.startOrResume()
        assertEquals(TimerState.RUNNING, timerState)
        
        timer.pause()
        assertEquals(TimerState.PAUSED, timerState)
        
        timer.startOrResume()
        assertEquals(TimerState.RUNNING, timerState)
        
        runBlocking { delay(2) }
        assertEquals(TimerState.ENDED, timerState)
    }
    
    @Test
    fun startOrResume() {
    }
    
    @Test
    fun stop() {
    }
    
    @Test
    fun extendDuration() {
    }
    
    @Test
    fun pause() {
    }
}