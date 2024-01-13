package org.listenbrainz.android.model.yimdata

enum class YimShareable(val code: String) {
    TRACKS("tracks"),
    STATISTICS("stats"),
    ARTISTS("artists"),
    ALBUMS("albums"),
    DISCOVERIES("discovery-playlist"),
    UNDISCOVERED("missed-playlist"),
    OVERVIEW("overview")
}