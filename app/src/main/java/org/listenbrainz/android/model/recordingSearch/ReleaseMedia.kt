package org.listenbrainz.android.model.recordingSearch


import com.google.gson.annotations.SerializedName

data class ReleaseMedia(
    @SerializedName("format")
    val format: String? = null,
    @SerializedName("position")
    val position: Int? = null,
    @SerializedName("track")
    val track: List<ReleaseTrack?> = emptyList(),
    @SerializedName("track-count")
    val trackCount: Int? = null,
    @SerializedName("track-offset")
    val trackOffset: Int? = null
)