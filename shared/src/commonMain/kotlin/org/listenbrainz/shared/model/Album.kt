package org.listenbrainz.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class Album(
    val albumId: Long = 0,
    val title: String = "",
    val artist: String = "",
    val albumArt: String = ""
)