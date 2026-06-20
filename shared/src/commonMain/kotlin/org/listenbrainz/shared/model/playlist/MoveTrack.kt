package org.listenbrainz.shared.model.playlist

import kotlinx.serialization.Serializable

@Serializable
data class MoveTrack(
    val mbid: String = "",
    val from: Int = 0,
    val to: Int = 0,
    val count: Int = 0
)
