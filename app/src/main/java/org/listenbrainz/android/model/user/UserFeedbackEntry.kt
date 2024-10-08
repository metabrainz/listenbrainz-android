package org.listenbrainz.android.model.user

import com.google.gson.annotations.SerializedName
import org.listenbrainz.android.model.TrackMetadata

data class UserFeedbackEntry(
    val created: Int? = 0,
    @SerializedName ("recording_mbid") val recordingMBID: String? = null,
    @SerializedName ("recording_msid") val recordingMSID: String? = null,
    val score: Int? = null,
    @SerializedName("track_metadata") val trackMetadata: TrackMetadata? = null,
    @SerializedName("user_id")        val userId: String? = null
)