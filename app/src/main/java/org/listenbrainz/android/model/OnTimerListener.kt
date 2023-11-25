package org.listenbrainz.android.model

interface OnTimerListener {
    fun onTimerRun(milliseconds: Long) {}
    fun onTimerStarted()
    fun onTimerPaused(remainingMillis: Long)
    fun onTimerStopped() {}
    fun onTimerEnded()
}