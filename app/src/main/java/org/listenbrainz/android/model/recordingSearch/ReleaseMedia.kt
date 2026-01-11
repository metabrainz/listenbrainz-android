package org.listenbrainz.android.model.recordingSearch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReleaseMedia(
    @SerialName("format")
    val format: String? = null,
    @SerialName("position")
    val position: Int? = null,
    @SerialName("track")
    val track: List<ReleaseTrack?> = emptyList(),
    @SerialName("track-count")
    val trackCount: Int? = null,
    @SerialName("track-offset")
    val trackOffset: Int? = null
)