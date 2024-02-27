package org.listenbrainz.android

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
        list shouldBe listOf(1,2,3,4)
    }
    
    @Test
    fun `test delayed post`() = test {
        val list = mutableListOf<Int>()
        val token1 = "token1"
        jobQueue.postDelayed(delayMillis = 5, token = token1) { list.add(0) }
        
        delay(4)
        list shouldBe emptyList()
        delay(1)
        list shouldBe listOf(0)
    }
    
    @Test
    fun `test delayed post does not post early`() = test {
        val list = mutableListOf<Int>()
        val token1 = "token1"
        jobQueue.postDelayed(delayMillis = 50, token = token1) { list.add(0) }
        
        delay(20)
        list shouldBe emptyList()
        
        delay(30)
        list shouldBe listOf(0)
    }
    
    @Test
    fun `test remove post`() = test {
        val list = mutableListOf<Int>()
        val token1 = "token1"
        jobQueue.postDelayed(delayMillis = 50, token = token1) { list.add(0) }
        
        delay(20)
        list shouldBe emptyList()
        
        delay(29)
        // Remove job from queue.
        jobQueue.removePosts(token1)
        
        list shouldBe emptyList()
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
                jobQueue.postDelayed(delayMillis = 50, token = it) { list.add(it) }
                jobQueue.removePosts(it)
            } else
                jobQueue.postDelayed(delayMillis = 0, token = it) { list.add(it) }
        }
        
        delay(20)
        
        list shouldBe expected
    }
    
    @Test
    fun `test remove posts long delay`() = test {
        val list = mutableListOf<Int>()
        jobQueue.postDelayed(60000, 69) {
            list.add(50)
        }
        delay(59000)
        jobQueue.removePosts(69)
        delay(1000)
        list shouldBe emptyList()
    }
}