package org.listenbrainz.android.model.playlist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistArtist(
    @SerialName("artist_credit_name")
    val artistCreditName: String? = null,
    @SerialName("artist_mbid")
    val artistMbid: String? = null,
    @SerialName("join_phrase")
    val joinPhrase: String? = null
)