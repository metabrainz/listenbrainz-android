package org.listenbrainz.android.model

interface OnTimerListener {
    fun onTimerStarted() {}
    fun onTimerResumed() {}
    fun onTimerPaused(remainingMillis: Long)
    fun onTimerEnded() {}
}