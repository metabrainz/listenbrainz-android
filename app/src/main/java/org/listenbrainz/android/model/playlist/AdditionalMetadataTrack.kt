package org.listenbrainz.android.model.playlist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdditionalMetadataTrack(
    @SerialName("artists")
    val artists: List<PlaylistArtist> = listOf(),
    @SerialName("caa_id")
    val caaId: Long? = null,
    @SerialName("caa_release_mbid")
    val caaReleaseMbid: String? = null
)