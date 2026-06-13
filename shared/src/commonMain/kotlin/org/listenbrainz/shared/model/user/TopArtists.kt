package org.listenbrainz.shared.model.user

import kotlinx.serialization.Serializable

@Serializable
data class TopArtists(
    val payload: TopArtistsPayload = TopArtistsPayload()
)