package org.listenbrainz.android.model

import kotlinx.serialization.Serializable

@Serializable
data class Playable(
    val type: PlayableType = PlayableType.SONG,
    val id: Long = 0L,
    var songs: List<Song> = emptyList(),
    var currentSongIndex: Int = 0,
    var seekTo: Long = 0L
) {
    companion object {
        val EMPTY_PLAYABLE = Playable(
            PlayableType.SONG,
            -1L,
            emptyList(),
            0
        )
    }
}

@Serializable
enum class PlayableType{
    SONG,
    ARTIST,
    ALBUM,
    PLAYLIST,
    ALL_SONGS
}