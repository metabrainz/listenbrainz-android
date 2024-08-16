package org.listenbrainz.android.model.artist

import com.google.gson.annotations.SerializedName

data class ArtistPersonalInfo(
    val area: String? = null,
    @SerializedName("artist_mbid") val artistMbid: String? = null,
    @SerializedName("begin_year") val beginYear: Int? = null,
    val gender: String? = null,
    @SerializedName("join_phrase") val joinPhrase: String? = null,
    val name: String? = null,
    val rels: Rels? = null,
    val type: String? = null
)