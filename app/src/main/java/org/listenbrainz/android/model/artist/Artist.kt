package org.listenbrainz.android.model.artist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Artist(
    val area: String? = "",
    @SerialName("artist_mbid") val artistMbid: String? = "",
    @SerialName("begin_year") val beginYear: Int? = 0,
    @SerialName("end_year") val endYear: Int? = null,
    @SerialName("join_phrase") val joinPhrase: String? = null,
    val gender: String? = "",
    val mbid: String? = "",
    val name: String? = "",
    val rels: Rels? = Rels(),
    val tag: Tag? = Tag(),
    val type: String? = "",
)