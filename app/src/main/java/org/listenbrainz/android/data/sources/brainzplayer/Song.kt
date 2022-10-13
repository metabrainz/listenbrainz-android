package org.listenbrainz.android.data.sources.brainzplayer

data class Song (
    val mediaID : String="",
    val title : String="",
    val artist: String = "",
    val uri : String = "",
    val albumID: String = "",
    val album: String = "",
    val albumArt: String = ""
)