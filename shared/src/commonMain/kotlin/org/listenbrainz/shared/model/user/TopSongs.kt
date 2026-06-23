package org.listenbrainz.shared.model.user

import kotlinx.serialization.Serializable

@Serializable
data class TopSongs(
    val payload: TopSongPayload? = TopSongPayload()
)