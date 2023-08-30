package org.listenbrainz.android.model.feed

import com.google.gson.annotations.SerializedName

data class FeedListenArtist(
    @SerializedName("artist_credit_name") val artistCreditName: String,
    @SerializedName("artist_mbid") val artistMbid: String,
    @SerializedName("join_phrase")val joinPhrase: String
)