package org.listenbrainz.android.model.recordingSearch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecordingRelease(
    @SerialName("artist-credit")
    val artistCredit: List<ArtistCreditX> = emptyList(),
    @SerialName("count")
    val count: Int? = null,
    @SerialName("country")
    val country: String? = null,
    @SerialName("date")
    val date: String? = null,
    @SerialName("id")
    val id: String? = null,
    @SerialName("media")
    val media: List<ReleaseMedia> = emptyList(),
    @SerialName("release-events")
    val releaseEvents: List<ReleaseEvent> = emptyList(),
    @SerialName("release-group")
    val releaseGroup: RecordingReleaseGroup? = null,
    @SerialName("status")
    val status: String? = null,
    @SerialName("status-id")
    val statusId: String? = null,
    @SerialName("title")
    val title: String? = null,
    @SerialName("track-count")
    val trackCount: Int? = null
)