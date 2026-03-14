package org.listenbrainz.shared.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrackMetadata(
    @SerialName("additional_info")
    val additionalInfo: AdditionalInfo? = null,
    @SerialName("artist_name")
    val artistName: String? = null,
    @SerialName("mbid_mapping")
    val mbidMapping: MbidMapping? = null,
    @SerialName("release_name")
    val releaseName: String? = null,
    @SerialName("track_name")
    val trackName: String? = null
) {
    val sharedTransitionId
        get() = (mbidMapping?.recordingMbid ?: mbidMapping?.recordingName ?: trackName).orEmpty() +
                (mbidMapping?.artistMbids?.joinToString() ?: artistName).orEmpty() +
                (mbidMapping?.releaseMbid ?: releaseName).orEmpty() +
                mbidMapping?.caaReleaseMbid.orEmpty()
}
