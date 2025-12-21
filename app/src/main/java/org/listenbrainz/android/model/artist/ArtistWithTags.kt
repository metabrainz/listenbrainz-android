package org.listenbrainz.android.model.artist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArtistWithTags(
    @SerialName("artist_mbid") val artistMbid: String? = null,
    val count: Int? = null,
    @SerialName("genre_mbid") val genreMbid: String? = null,
    val tag: String? = null
)