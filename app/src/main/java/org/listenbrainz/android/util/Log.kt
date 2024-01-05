package org.listenbrainz.android.util

import com.limurse.logger.Logger

object Log {

    fun e(message: Any?, tag: String? = null) {
        Logger.e(tag, msg = message.toString())
    }

    fun d(message: Any?) {
        Logger.d(msg = message.toString())
    }
    
    fun w(message: Any?) {
        Logger.w(msg = message.toString())
    }
}