package org.listenbrainz.android.model.artist

import com.google.gson.annotations.SerializedName

data class ArtistX(
    @SerializedName("artist_mbid") val artistMbid: String? = null,
    val count: Int? = null,
    @SerializedName("genre_mbid") val genreMbid: String? = null,
    val tag: String? = null
)