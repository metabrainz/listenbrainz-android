package org.listenbrainz.shared.model.user

import kotlinx.serialization.Serializable

@Serializable
data class TopAlbums(
    val payload: TopAlbumsPayload? = TopAlbumsPayload()
)