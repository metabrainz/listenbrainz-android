package org.listenbrainz.android.data.sources.brainzplayer

data class Playable(
    val type: String,
    val id: Long,
    val songs: List<Song>,
    val currentlyPlayingSongIndex: Int
)