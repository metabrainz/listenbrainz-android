package org.listenbrainz.android.model.playlist


import com.google.gson.annotations.SerializedName

data class AdditionalMetadataTrack(
    @SerializedName("artists")
    val artists: List<PlaylistArtist>,
    @SerializedName("caa_id")
    val caaId: Long,
    @SerializedName("caa_release_mbid")
    val caaReleaseMbid: String
)