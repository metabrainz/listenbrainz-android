package org.listenbrainz.android.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.listenbrainz.shared.model.Metadata
import org.listenbrainz.shared.model.TrackMetadata

@Serializable
data class Listen(
    @SerialName("inserted_at") val insertedAt: Long? = null,
    @SerialName("listened_at") val listenedAt: Long? = null,
    @SerialName("recording_msid") val recordingMsid: String? = null,
    @SerialName("track_metadata") val trackMetadata: TrackMetadata? = null,
    @SerialName("user_name") val userName: String? = null,
    @SerialName("cover_art") val coverArt: CoverArt? = null
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
        get() = trackMetadata?.sharedTransitionId.orEmpty() + (listenedAt ?: insertedAt).toString()
}