package org.listenbrainz.android.model

import com.google.gson.annotations.SerializedName
import org.listenbrainz.android.util.defaultZoneOffset
import org.threeten.bp.LocalDateTime

data class Listen(
    @SerializedName("inserted_at") val insertedAt: Long,
    @SerializedName("listened_at") val listenedAt: Long? = null,
    @SerializedName("recording_msid") val recordingMsid: String,
    @SerializedName("track_metadata") val trackMetadata: TrackMetadata,
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

    val listenedAtDateTime get() = listenedAt?.let {
        LocalDateTime.ofEpochSecond(it, 0, defaultZoneOffset())
    }

    val insertedAtDateTime get() =
        LocalDateTime.ofEpochSecond(insertedAt, 0, defaultZoneOffset())

    val sharedTransitionId
        get() = trackMetadata.sharedTransitionId + (listenedAt ?: insertedAt).toString()
}