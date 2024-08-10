package org.listenbrainz.android.model.user

import com.google.gson.annotations.SerializedName

data class Artist(
    @SerializedName("artist_mbid")  val artistMbid: String,
    @SerializedName("artist_name")  val artistName: String,
    @SerializedName("listen_count") val listenCount: Int
)