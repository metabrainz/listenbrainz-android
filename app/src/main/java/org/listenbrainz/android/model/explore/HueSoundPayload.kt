package org.listenbrainz.android.model.explore

import com.google.gson.annotations.SerializedName
import org.listenbrainz.android.model.TrackMetadata

data class HueSoundPayload(
    val payload: HueSoundPayloadData
)

data class HueSoundPayloadData(
    val releases: List<Release>
)

data class Release(
    @SerializedName("artist_name") val artistName: String = "",
    @SerializedName("release_name") val releaseName: String = "",
    @SerializedName("release_mbid") val releaseId: String = "",
    @SerializedName("caa_id") val caaId: Long = 0,
    val recordings: List<Recording> = listOf()
)

data class Recording(
    @SerializedName("track_metadata") val trackMetadata: TrackMetadata
)