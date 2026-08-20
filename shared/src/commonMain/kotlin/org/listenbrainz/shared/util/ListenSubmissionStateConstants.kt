package org.listenbrainz.shared.util

object ListenSubmissionStateConstants {
    const val DEFAULT_DURATION: Long = 60_000L

    /** Max time required to validate a listen as submittable listen is 4 minutes. */
    const val MAX_SUBMISSION_DURATION: Long = 240_000L
    const val SUBMISSION_TIMER_TOKEN = 69
    const val TRACK_COMPLETION_TIMER_TOKEN = 420
}