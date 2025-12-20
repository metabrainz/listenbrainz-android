package org.listenbrainz.android.util

import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset

fun defaultZoneOffset(): ZoneOffset =
    ZoneOffset.of(
        ZoneId.systemDefault()
            .rules
            .getOffset(Instant.now())
            .id,
    )