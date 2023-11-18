package org.listenbrainz.android.util

import android.nfc.Tag
import com.limurse.logger.Logger

object Log {

    fun e(tag: String? = null, message: String) {
        Logger.e(tag, msg =  message)
    }

    fun d(message: String) {
        Logger.d(msg = message)
    }
    
    fun w(message: String) {
        Logger.w(msg = message)
    }
}