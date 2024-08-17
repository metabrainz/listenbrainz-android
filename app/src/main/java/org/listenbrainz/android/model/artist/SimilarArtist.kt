package org.listenbrainz.android.model.artist

import com.google.gson.annotations.SerializedName

data class SimilarArtist(
    @SerializedName("artist_mbid") val artistMbid: String? = null,
    val comment: String? = null,
    val gender: String? = null,
    val name: String? = null,
    @SerializedName("reference_mbid") val referenceMbid: String? = null,
    val score: Int? = null,
    val type: String? = null
)