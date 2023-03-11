package org.listenbrainz.android.util

import android.util.Log

object Log {

    private const val TAG = Constants.TAG

    fun e(message: String) {
        Log.e(TAG, message)
    }

    fun d(message: String) {
        Log.d(TAG, message)
    }

    fun v(message: String) {
        Log.v(TAG, message)
    }
    
    fun w(message: String) {
        Log.w(TAG, message)
    }
}