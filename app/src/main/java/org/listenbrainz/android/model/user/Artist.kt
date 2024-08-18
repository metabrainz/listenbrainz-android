package org.listenbrainz.android.model.user

import com.google.gson.annotations.SerializedName

data class Artist(
    @SerializedName("artist_mbid")  val artistMbid: String? = null,
    @SerializedName("artist_name")  val artistName: String? = null,
    @SerializedName("listen_count") val listenCount: Int? = null,
)