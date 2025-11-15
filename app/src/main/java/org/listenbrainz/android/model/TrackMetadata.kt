package org.listenbrainz.android.model

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class TrackMetadata(
    @SerialName("additional_info")
    @SerializedName("additional_info")
    val additionalInfo: AdditionalInfo?,
    @SerialName("artist_name")
    @SerializedName("artist_name")
    val artistName: String,
    @SerialName("mbid_mapping")
    @SerializedName("mbid_mapping")
    val mbidMapping: MbidMapping?,
    @SerialName("release_name")
    @SerializedName("release_name")
    val releaseName: String?,
    @SerialName("track_name")
    @SerializedName("track_name")
    val trackName: String
) {
    val sharedTransitionId
        get() = (mbidMapping?.recordingMbid ?: mbidMapping?.recordingName ?: trackName) +
                (mbidMapping?.artistMbids?.joinToString() ?: artistName) +
                (mbidMapping?.releaseMbid ?: releaseName) +
                mbidMapping?.caaReleaseMbid.orEmpty()
}