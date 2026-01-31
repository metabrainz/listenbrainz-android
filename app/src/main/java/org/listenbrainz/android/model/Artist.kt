package org.listenbrainz.android.model

import kotlinx.serialization.Serializable
import org.listenbrainz.shared.model.Song

@Serializable
data class Artist(
    val id: Long = 0,
    val name: String = "",
    val songs: List<Song> = listOf(),
    val albums: List<Album> = listOf()
)