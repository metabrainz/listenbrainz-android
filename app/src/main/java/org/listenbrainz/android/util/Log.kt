package org.listenbrainz.android.util

import android.util.Log
import org.listenbrainz.android.presentation.Configuration

object Log {

    private const val TAG = Configuration.TAG

    fun e(message: String?) {
        Log.e(TAG, message!!)
    }

    fun d(message: String?) {
        Log.d(TAG, message!!)
    }

    fun v(message: String?) {
        Log.v(TAG, message!!)
    }
    
    fun w(message: String?) {
        Log.w(TAG, message!!)
    }
}