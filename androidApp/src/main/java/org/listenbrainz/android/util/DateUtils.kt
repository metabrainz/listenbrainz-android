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