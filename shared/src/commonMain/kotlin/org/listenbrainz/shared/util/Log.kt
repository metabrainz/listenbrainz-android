package org.listenbrainz.shared.util

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

interface Log {

    fun e(message: Any?, tag: String? = null, throwable: Throwable? = null)

    fun d(message: Any?, tag: String? = null)

    fun w(message: Any?, tag: String? = null)

    fun i(message: Any?, tag: String? = null)

    fun v(message: Any?, tag: String? = null)

    fun log(severity: Severity, message: String, tag: String, throwable: Throwable?)

    companion object : Log, KoinComponent {

        private val logger: Logger = get()

        override fun e(message: Any?, tag: String?, throwable: Throwable?) {
            val logTag = tag ?: "ListenBrainz"
            logger.withTag(logTag).e(throwable) { message.toString() }
        }

        override fun d(message: Any?, tag: String?) {
            val logTag = tag ?: "ListenBrainz"
            logger.withTag(logTag).d { message.toString() }
        }

        override fun w(message: Any?, tag: String?) {
            val logTag = tag ?: "ListenBrainz"
            logger.withTag(logTag).w { message?.toString() ?: "null" }
        }

        override fun i(message: Any?, tag: String?) {
            val logTag = tag ?: "ListenBrainz"
            logger.withTag(logTag).i { message?.toString() ?: "null" }
        }

        override fun v(message: Any?, tag: String?) {
            val logTag = tag ?: "ListenBrainz"
            logger.withTag(logTag).v { message?.toString() ?: "null" }
        }

        override fun log(
            severity: Severity,
            message: String,
            tag: String,
            throwable: Throwable?,
        ) {
            logger.withTag(tag).log(severity, tag, throwable, message)
        }
    }
}