package org.listenbrainz.android.model.artist

import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    val artist: List<ArtistWithTags>? = listOf()
)