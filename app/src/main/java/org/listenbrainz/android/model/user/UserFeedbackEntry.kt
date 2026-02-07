package org.listenbrainz.android.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.model.TrackMetadata

@Serializable
data class UserFeedbackEntry(
    @SerialName("created") val created: Long? = 0,
    @SerialName("recording_mbid") val recordingMBID: String? = null,
    @SerialName("recording_msid") val recordingMSID: String? = null,
    @SerialName("score") val score: Int? = null,
    @SerialName("track_metadata") val trackMetadata: TrackMetadata? = null,
    @SerialName("user_id") val userId: String? = null
) {
    fun toMetadata() = Metadata(
        created = created,
        entityId = recordingMBID,
        trackMetadata = trackMetadata,
        username = userId
    )
}