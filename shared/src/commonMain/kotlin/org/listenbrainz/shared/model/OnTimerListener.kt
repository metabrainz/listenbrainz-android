package org.listenbrainz.shared.model

interface OnTimerListener {
    fun onTimerStarted() {}
    fun onTimerResumed() {}
    fun onTimerPaused(remainingMillis: Long) {}
    fun onTimerEnded() {}
}