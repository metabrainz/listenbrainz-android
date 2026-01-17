package org.listenbrainz.android.model.artist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SimilarArtist(
    @SerialName("artist_mbid") val artistMbid: String? = null,
    val comment: String? = null,
    val gender: String? = null,
    val name: String? = null,
    @SerialName("reference_mbid") val referenceMbid: String? = null,
    val score: Int? = null,
    val type: String? = null
)