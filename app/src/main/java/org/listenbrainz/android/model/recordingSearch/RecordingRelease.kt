package org.listenbrainz.android.model.recordingSearch


import com.google.gson.annotations.SerializedName

data class RecordingRelease(
    @SerializedName("artist-credit")
    val artistCredit: List<ArtistCreditX> = emptyList(),
    @SerializedName("count")
    val count: Int? = null,
    @SerializedName("country")
    val country: String? = null,
    @SerializedName("date")
    val date: String? = null,
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("media")
    val media: List<ReleaseMedia> = emptyList(),
    @SerializedName("release-events")
    val releaseEvents: List<ReleaseEvent> = emptyList(),
    @SerializedName("release-group")
    val releaseGroup: RecordingReleaseGroup? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("status-id")
    val statusId: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("track-count")
    val trackCount: Int? = null
)