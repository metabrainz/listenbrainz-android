package org.listenbrainz.android.model.user

import kotlinx.serialization.Serializable

@Serializable
data class TopAlbums(
    val payload: TopAlbumsPayload? = TopAlbumsPayload()
)