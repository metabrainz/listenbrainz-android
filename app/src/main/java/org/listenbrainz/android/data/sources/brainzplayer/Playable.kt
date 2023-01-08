package org.listenbrainz.android.data.sources.brainzplayer

data class Playable(
    val type: PlayableType,
    val id: Long,
    val songs: List<Song>,
    val currentSongIndex : Int
)

enum class PlayableType{
    SONG,
    ARTIST,
    ALBUM,
    PLAYLIST
}