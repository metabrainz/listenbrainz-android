package org.listenbrainz.android.model.artist

import com.google.gson.annotations.SerializedName

data class Artist(
    val area: String? = "",
    @SerializedName("artist_mbid") val artistMbid: String? = "",
    @SerializedName("begin_year") val beginYear: Int? = 0,
    val gender: String? = "",
    val mbid: String? = "",
    val name: String? = "",
    val rels: Rels? = Rels(),
    val tag: Tag? = Tag(),
    val type: String? = "",
)