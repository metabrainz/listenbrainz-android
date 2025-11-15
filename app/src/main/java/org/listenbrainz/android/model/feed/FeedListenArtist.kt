package org.listenbrainz.android.model.feed

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedListenArtist(
    @SerializedName("artist_credit_name") @SerialName("artist_credit_name") val artistCreditName: String,
    @SerializedName("artist_mbid") @SerialName("artist_mbid") val artistMbid: String?,
    @SerializedName("join_phrase") @SerialName("join_phrase") val joinPhrase: String?
)