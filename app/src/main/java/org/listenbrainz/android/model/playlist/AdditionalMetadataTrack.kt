package org.listenbrainz.android.model.playlist


import com.google.gson.annotations.SerializedName

data class AdditionalMetadataTrack(
    @SerializedName("artists")
    val artists: List<PlaylistArtist> = listOf(),
    @SerializedName("caa_id")
    val caaId: Long? = null,
    @SerializedName("caa_release_mbid")
    val caaReleaseMbid: String? = null
)