package org.listenbrainz.android.util

import com.limurse.logger.Logger

interface Log {
    fun e(message: Any?, tag: String? = null)
    
    fun d(message: Any?)
    
    fun w(message: Any?)
    
    companion object: Log {
        override fun e(message: Any?, tag: String?) {
            Logger.e(tag, msg = message.toString())
        }
        
        override fun d(message: Any?) {
            Logger.d(msg = message.toString())
        }
        
        override fun w(message: Any?) {
            Logger.w(msg = message.toString())
        }
    }
}