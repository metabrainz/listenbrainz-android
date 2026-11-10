package org.listenbrainz.android.ui.screens.artist

import kotlin.math.round

fun formatNumber(input: Int): String {
    return when {
        input >= 1_00_000 -> "${round(input / 1_00_000f)}L"
        input >= 1_000 -> "${round(input / 1_000f)}K"
        else -> input.toString()
    }
}
