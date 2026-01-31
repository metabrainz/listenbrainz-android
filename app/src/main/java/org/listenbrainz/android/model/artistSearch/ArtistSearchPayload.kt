package org.listenbrainz.android.model.artistSearch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArtistSearchPayload(
    @SerialName("created")
    val created: String? = null,
    @SerialName("count")
    val count: Int? = null,
    @SerialName("offset")
    val offset: Int? = null,
    @SerialName("artists")
    val artists: List<ArtistData> = emptyList()
)
