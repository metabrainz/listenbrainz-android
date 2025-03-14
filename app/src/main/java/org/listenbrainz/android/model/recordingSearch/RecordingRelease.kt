package org.listenbrainz.android.model.recordingSearch


import com.google.gson.annotations.SerializedName

data class RecordingRelease(
    @SerializedName("artist-credit")
    val artistCredit: List<ArtistCreditX>?,
    @SerializedName("count")
    val count: Int?,
    @SerializedName("country")
    val country: String?,
    @SerializedName("date")
    val date: String?,
    @SerializedName("id")
    val id: String?,
    @SerializedName("media")
    val media: List<ReleaseMedia>?,
    @SerializedName("release-events")
    val releaseEvents: List<ReleaseEvent>?,
    @SerializedName("release-group")
    val releaseGroup: RecordingReleaseGroup?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("status-id")
    val statusId: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("track-count")
    val trackCount: Int?
)