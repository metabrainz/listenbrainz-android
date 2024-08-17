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
    val coverArt: String? = "",
    val listeningStats: ListeningStats? = null,
    val popularRecordings: List<PopularRecording?> = listOf(),
    val releaseGroups: List<ReleaseGroup?> = listOf(),
    val similarArtists: List<SimilarArtist?> = listOf()
)