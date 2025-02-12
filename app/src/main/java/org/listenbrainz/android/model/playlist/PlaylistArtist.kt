package org.listenbrainz.android.model.playlist


import com.google.gson.annotations.SerializedName

data class PlaylistArtist(
    @SerializedName("artist_credit_name")
    val artistCreditName: String? = null,
    @SerializedName("artist_mbid")
    val artistMbid: String? = null,
    @SerializedName("join_phrase")
    val joinPhrase: String? = null
)