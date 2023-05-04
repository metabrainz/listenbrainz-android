package org.listenbrainz.android.model

data class Playable(
    val type: PlayableType,
    val id: Long,
    val songs: List<Song> = emptyList(),
    var currentSongIndex : Int,
    var seekTo: Long = 0L
)

enum class PlayableType{
    SONG,
    ARTIST,
    ALBUM,
    PLAYLIST,
    ALL_SONGS
}