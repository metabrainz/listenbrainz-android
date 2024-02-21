package org.listenbrainz.android.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

class JobQueue(
    private val dispatcher: CoroutineDispatcher,
    private val log: Log = Log.Companion
) {
    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        log.e("JobQueue Exception: ${e.message}")
    }
    
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val delayedQueue: ConcurrentLinkedQueue<QueueJob> = ConcurrentLinkedQueue()
    private val queue = Channel<QueueJob>(Channel.UNLIMITED)
    private val syncLock = Mutex()
    private val delayedQueueLock = Mutex()
    private var tokenCounter = Int.MIN_VALUE
    
    init {
        with(scope) {
            // Execution queue
            launch(exceptionHandler) {
                queue.receiveAsFlow().collect { queueJob ->
                    runCancellationCatching {
                        // If a job is being cancelled, in the delayedQueue, we must wait for
                        // the current selected job's cancellation status.
                        delayedQueueLock.withLock {
                            queueJob.job.join()
                        }
                        log.d("Job with token ${queueJob.token} completed.")
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
            syncLock.withLock {
                val job = scope.launch(context, CoroutineStart.LAZY, block)
                queue.send(QueueJob(token, job))
            }
        }
    }
    
    fun postDelayed(
        delayMillis: Long,
        token: Any? = null,
        context: CoroutineContext = dispatcher,
        block: suspend CoroutineScope.() -> Unit
    ) {
        val delayToken: String = token.toString() + tokenCounter++

        val job = scope.launch {
            runCancellationCatching {
                delay(delayMillis)
                // We do not want job to be executed while the
                // user just briefly ordered to remove the job.
                delayedQueueLock.lock()
            }.onFailure { return@launch }
            
            post(token, context) {
                ensureActive()
                block()
            }
            
            // Remove after completion.
            withContext(dispatcher) {
                // Find and remove job from delayed queue.
                delayedQueue.removeJobIf { it.token == delayToken }
            }
            delayedQueueLock.unlock()
        }
        delayedQueue.add(QueueJob(delayToken, job))
    }
    
    /** Removes all the delayed posts with [token] identifier.
     * @param token if null, all jobs will be cancelled.*/
    fun removeDelayedPosts(token: Any?) {
        with(scope) {
            if (token == null) {
                // We won't be removing jobs from the channel since we
                // don't expect any job to be in channel when this is called.
                /** Remove all delayed posts*/
                launch(dispatcher) {
                    delayedQueueLock.withLock {
                        delayedQueue.removeJobIf { true }
                    }
                }
            } else {
                /** Remove from delayed queue */
                scope.launch(dispatcher) {
                    delayedQueueLock.withLock {
                        delayedQueue.removeJobIf {
                            // Since we are appending a counter as well,
                            val tokenLength = token.toString().length
                            val delayToken = it.token.toString()
                            val userAssignedToken = delayToken.substring(0..<tokenLength)
                            return@removeJobIf userAssignedToken == token
                        }
                    }
                }
            }
        }
    }
    
    /** Removes and cancels all the jobs that match the filter predicate.*/
    private fun ConcurrentLinkedQueue<QueueJob>.removeJobIf(filter: (QueueJob) -> Boolean) {
        val iterator = this.iterator()
        while (iterator.hasNext()) {
            val queueJob = iterator.next()
            if (filter(queueJob)) {
                queueJob.job.cancel("Post delayed cancelled for token ${queueJob.token}.")
                iterator.remove()
            }
        }
    }
    
    fun cancel() {
        queue.cancel()
        scope.cancel()
    }
    
    private suspend inline fun <T> T.runCancellationCatching(
        crossinline block: suspend T.() -> Unit
    ): Result<Unit> {
        try {
            block()
        } catch (e: CancellationException) {
            // If the job is cancelled, we should log but not throw exception.
            log.d(e.message.toString())
            return Result.failure(e)
        }
        return Result.success(Unit)
    }
    
    companion object {
        /** A class containing reference to a scheduled job and its token.*/
        private data class QueueJob(
            val token: Any?,
            val job: Job
        )
    }
}