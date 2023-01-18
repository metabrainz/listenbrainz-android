package org.listenbrainz.android.presentation.features.yim.navigation

enum class YimShareable(val code: String) {
    TRACKS("tracks"),
    STATISTICS("stats"),
    ARTISTS("artists"),
    ALBUMS("albums"),
    DISCOVERIES("discovery-playlist"),
    UNDISCOVERED("missed-playlist")
}