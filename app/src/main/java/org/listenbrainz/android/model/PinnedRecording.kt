package org.listenbrainz.android.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrentPins(
    @SerialName("pinned_recording") val pinnedRecording: PinnedRecording? = null
)

@Serializable
data class PinnedRecording(
    @SerialName("created") val created: Long? = null,
    @SerialName("row_id") val rowId: Int? = null,
    @SerialName("track_metadata") val trackMetadata: TrackMetadata? = null,
    
    // Only below fields are used for posting pins.
    @SerialName("recording_msid") val recordingMsid : String? = null,
    @SerialName("recording_mbid") val recordingMbid : String? = null,
    @SerialName("blurb_content") val blurbContent  : String? = null,
    @SerialName("pinned_until") val pinnedUntil   : Int?    = null
) {
    fun toMetadata() = Metadata(
        trackMetadata = trackMetadata,
        blurbContent = blurbContent,
        created = created,
    )
}