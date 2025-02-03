package org.listenbrainz.android.model

data class Playable(
    val type: PlayableType,
    val id: Long,
    var songs: List<Song> = emptyList(),
    var currentSongIndex : Int,
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

enum class PlayableType{
    SONG,
    ARTIST,
    ALBUM,
    PLAYLIST,
    ALL_SONGS
}