package org.listenbrainz.android.model.feed

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedListenArtist(
    @SerialName("artist_credit_name") val artistCreditName: String,
    @SerialName("artist_mbid") val artistMbid: String?,
    @SerialName("join_phrase") val joinPhrase: String?
)