package org.listenbrainz.android.util

import com.limurse.logger.Logger

object Log {

    fun e(message: String) {
        Logger.e(msg =  message)
    }

    fun d(message: String) {
        Logger.d(msg = message)
    }
    
    fun w(message: String) {
        Logger.w(msg = message)
    }
}