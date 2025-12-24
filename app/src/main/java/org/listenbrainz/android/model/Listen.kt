package org.listenbrainz.android.model

import com.google.gson.annotations.SerializedName

data class Listen(
    @SerializedName("inserted_at") val insertedAt: Long,
    @SerializedName("listened_at") val listenedAt: Long? = null,
    @SerializedName("recording_msid") val recordingMsid: String?,
    @SerializedName("track_metadata") val trackMetadata: TrackMetadata?,
    @SerializedName("user_name") val userName: String,
    @SerializedName("cover_art") val coverArt: CoverArt? = null
) {
    fun toMetadata(): Metadata {
        return Metadata(
            listenedAt = listenedAt,
            insertedAt = insertedAt,
            username = userName,
            trackMetadata = trackMetadata
        )
    }

    val sharedTransitionId
        get() = trackMetadata?.sharedTransitionId + (listenedAt ?: insertedAt).toString()
}