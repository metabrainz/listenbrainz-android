package org.listenbrainz.android.model

data class Artist(
    val id: Long = 0,
    val name: String = "",
    val songs: List<Song> = listOf(),
    val albums: List<Album> = listOf()
)