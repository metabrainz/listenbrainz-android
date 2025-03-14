package org.listenbrainz.android.model.recordingSearch


import com.google.gson.annotations.SerializedName

data class ReleaseMedia(
    @SerializedName("format")
    val format: String?,
    @SerializedName("position")
    val position: Int?,
    @SerializedName("track")
    val track: List<ReleaseTrack?>?,
    @SerializedName("track-count")
    val trackCount: Int?,
    @SerializedName("track-offset")
    val trackOffset: Int?
)