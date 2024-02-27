package org.listenbrainz.android.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.LinkedList
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

class JobQueue(
    private val dispatcher: CoroutineDispatcher,
    private val log: Log = Log.Companion
) {
    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        log.e("JobQueue Exception: ${e.message}")
    }
    private val scope = CoroutineScope(SupervisorJob() + dispatcher + exceptionHandler)
    
    /** `Token: List<QueueJob>` map. To support jobs with same token, we are creating list for
    * a specified token. */
    private val jobsMap = HashMap<Any?, LinkedList<QueueJob>>()
    private val queue = Channel<QueueJob>(Channel.UNLIMITED)
    
    /** Lock for [jobsMap].*/
    private val mapLock = Mutex()
    
    init {
        // Execution queue
        scope.launch(exceptionHandler) {
            queue.receiveAsFlow().collect { queueJob ->
                ensureActive()
                
                if (!queueJob.cancelled.get()) {
                    queueJob.job.join()
                    log.d("Job with token: ${queueJob.token} completed.")
                } else {
                    queueJob.job.cancelAndJoin()
                    log.d("Job with token: ${queueJob.token} was cancelled.")
                }
                
                // Remove the job from jobMap
                mapLock.withLock {
                    jobsMap[queueJob.token]?.let { jobList ->
                        // Remove reference to the QueueJob
                        jobList.remove(queueJob)
                        // Remove token from map if empty.
                        if (jobList.isEmpty()) {
                            jobsMap.remove(queueJob.token)
                        }
                    }
                }
            }
        }
    }
    
    fun post(
        token: Any? = null,
        context: CoroutineContext = dispatcher,
        block: suspend CoroutineScope.() -> Unit
    ) {
        scope.launch {
            val queueJob = createQueueJob(token, context, block)
            addJobToMap(queueJob)
            queue.trySend(queueJob)
        }
    }
    
    fun postDelayed(
        delayMillis: Long,
        token: Any? = null,
        context: CoroutineContext = dispatcher,
        block: suspend CoroutineScope.() -> Unit
    ) {
        scope.launch {
            val queueJob = createQueueJob(token, context, block)
            addJobToMap(queueJob)
            delay(delayMillis)
            queue.trySend(queueJob)
        }
    }
    
    /** Removes all the delayed posts with [token] identifier.
     * @param token if null, all jobs will be cancelled.*/
    fun removePosts(token: Any?) {
        scope.launch(dispatcher) {
            if (token == null) {
                /** Mark all jobs as cancelled. */
                mapLock.withLock {
                    jobsMap.forEach {
                        val jobList = it.value
                        jobList.forEach { queueJob ->
                            queueJob.cancelled.set(true)
                        }
                    }
                }
            } else {
                /** Mark job with unique token as cancelled. */
                mapLock.withLock {
                    val jobList = jobsMap[token]
                    jobList?.forEach { queueJob ->
                        queueJob.cancelled.set(true)
                    }
                }
            }
        }
    }
    
    private fun createQueueJob(
        token: Any?,
        context: CoroutineContext,
        block: suspend CoroutineScope.() -> Unit
    ): QueueJob {
        val job = scope.launch(context, CoroutineStart.LAZY) {
            if (isActive) {
                block()
            }
        }
        
        return QueueJob(token, job, AtomicBoolean(false))
    }
    
    private suspend fun addJobToMap(queueJob: QueueJob) {
        mapLock.withLock {
            if (jobsMap.containsKey(queueJob.token)) {
                // We should not need to do null checks since we are using locks but still
                // are for safety.
                jobsMap[queueJob.token]?.add(queueJob)
            } else {
                jobsMap[queueJob.token] = LinkedList<QueueJob>().apply { add(queueJob) }
            }
        }
    }
    
    fun cancel() {
        queue.cancel()
        scope.cancel()
    }
    
    companion object {
        /** A class containing reference to a scheduled job and its token.*/
        private data class QueueJob(
            val token: Any?,
            val job: Job,
            val cancelled: AtomicBoolean
        )
    }
}