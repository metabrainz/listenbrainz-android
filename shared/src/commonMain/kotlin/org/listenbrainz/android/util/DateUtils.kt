package org.listenbrainz.android.util

import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

fun YearMonth.Companion.now(timeZone: TimeZone = TimeZone.currentSystemDefault()): YearMonth {
    val now = Clock.System
        .now()
        .toLocalDateTime(timeZone)
    return YearMonth(now.year, now.month)
}

//Format duration in seconds to HH:MM:SS format or MM:SS format
fun formatDurationSeconds(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    fun Int.twoDigits() = toString().padStart(2, '0')
    return if (hours > 0) {
        "${hours.twoDigits()}:${minutes.twoDigits()}:${secs.twoDigits()}"
    } else {
        "${minutes.twoDigits()}:${secs.twoDigits()}"
    }
}
