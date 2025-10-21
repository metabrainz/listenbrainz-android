package org.listenbrainz.android.model.user

import com.google.gson.annotations.SerializedName
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.model.TrackMetadata

data class UserFeedbackEntry(
    @SerializedName("created") val created: Int? = 0,
    @SerializedName("recording_mbid") val recordingMBID: String? = null,
    @SerializedName("recording_msid") val recordingMSID: String? = null,
    @SerializedName("score") val score: Int? = null,
    @SerializedName("track_metadata") val trackMetadata: TrackMetadata? = null,
    @SerializedName("user_id")        val userId: String? = null
) {
    fun toMetadata() = Metadata(
        created = created,
        entityId = recordingMBID,
        trackMetadata = trackMetadata,
        username = userId
    )
}