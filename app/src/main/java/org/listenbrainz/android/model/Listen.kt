package org.listenbrainz.android.model

import com.google.gson.annotations.SerializedName

data class Listen(
    @SerializedName("inserted_at") val insertedAt: String,
    @SerializedName("listened_at") val listenedAt: Int? = null,
    @SerializedName("recording_msid") val recordingMsid: String,
    @SerializedName("track_metadata") val trackMetadata: TrackMetadata,
    @SerializedName("user_name") val userName: String,
    @SerializedName("cover_art") val coverArt: CoverArt? = null
)