package org.listenbrainz.android

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.listenbrainz.android.util.JobQueue
import org.listenbrainz.android.util.Log
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class JobQueueTest: BaseUnitTest() {
    
    private lateinit var jobQueue: JobQueue
    
    @Mock
    private lateinit var mockLog: Log
    
    @Before
    fun setUp() {
        jobQueue = JobQueue(testDispatcher(), mockLog)
    }
    
    @After
    fun tearDown() {
        jobQueue.cancel()
    }
    
    @Test
    fun post() = test {
        val list = mutableListOf<Int>()
        jobQueue.post {
            list.add(1)
        }
        launch {
            list.add(2)
        }
        list.add(3)
        list.add(4)
        assertEquals(list.toList(), listOf(1,2,3,4))
    }
    
    @Test
    fun `test delayed post`() = test {
        val list = mutableListOf<Int>()
        val token1 = "token1"
        jobQueue.postDelayed(delayMillis = 5, token = token1) { list.add(0) }
        
        delay(4)
        assertEquals(listOf<Int>(), list)
        delay(1)
        assertEquals(listOf(0), list)
    }
    
    @Test
    fun `test delayed post does not post early`() = test {
        val list = mutableListOf<Int>()
        val token1 = "token1"
        jobQueue.postDelayed(delayMillis = 50, token = token1) { list.add(0) }
        
        delay(20)
        assertEquals(emptyList<Int>(), list)
        
        delay(30)
        assertEquals(listOf(0), list)
    }
    
    @Test
    fun `test delayed post cancelled`() = test {
        val list = mutableListOf<Int>()
        val token1 = "token1"
        jobQueue.postDelayed(delayMillis = 50, token = token1) { list.add(0) }
        
        delay(20)
        assertEquals(emptyList<Int>(), list)
        
        delay(29)
        // Remove job from queue.
        jobQueue.removeDelayedPosts(token1)
        
        assertEquals(emptyList<Int>(), list)
    }
    
    @Test
    fun `test delayed post rapid fire`() = test {
        val tokens = mutableListOf<Int>()
        repeat(50) {
            tokens.add(it)
        }
        val expected = mutableListOf<Int>().apply {
            tokens.forEach {
                add(it)
            }
            remove(20)
        }
        
        val list = mutableListOf<Int>()
        
        tokens.forEach {
            if (it == 20) {
                jobQueue.postDelayed(delayMillis = 5, token = it) { list.add(it) }
                jobQueue.removeDelayedPosts(it)
            } else
                jobQueue.postDelayed(delayMillis = 0, token = it) { list.add(it) }
        }
        
        delay(20)
        
        assertEquals(expected, list)
    }
    
    @Test
    fun test() = test {
        val list = mutableListOf<Int>()
        val handler = CoroutineExceptionHandler { _, e ->
            list.add(0)
        }
        
        val job = TestScope().launch(handler) {
            throw IllegalStateException()
        }
        
        list.isEmpty() assert false
    }
}