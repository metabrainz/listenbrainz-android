package org.listenbrainz.android.model

import com.google.gson.annotations.SerializedName

data class Listen(
    @SerializedName("inserted_at") val insertedAt: String,
    @SerializedName("listened_at") val listenedAt: Int,
    @SerializedName("recording_msid") val recordingMsid: String,
    @SerializedName("track_metadata") val trackMetadata: TrackMetadata,
    @SerializedName("user_name") val userName: String,
    @SerializedName("cover_art") var coverArt: CoverArt? = null
)