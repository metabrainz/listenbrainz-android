package org.listenbrainz.android.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Immutable
@Serializable
data class Metadata(
    @SerialName("blurb_content")
    val blurbContent: String? = null,
    @SerialName("created")
    val created: Long? = null,
    @SerialName("entity_id")
    val entityId: String? = null,
    @SerialName("entity_name")
    val entityName: String? = null,
    @SerialName("entity_type")
    val entityType: String? = null,
    @SerialName("inserted_at")
    val insertedAt: Long? = null,
    @SerialName("listened_at")
    val listenedAt: Long? = null,
    @SerialName("listened_at_iso")
    val listenedAtIso: JsonElement? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("playing_now")
    val playingNow: JsonElement? = null,
    @SerialName("rating")
    val rating: Int? = null,
    @SerialName("relationship_type")
    val relationshipType: String? = null,
    @SerialName("review_mbid")
    val reviewMbid: String? = null,
    @SerialName("text")
    val text: String? = null,
    @SerialName("track_metadata")
    val trackMetadata: TrackMetadata? = null,
    @SerialName("user_name")
    val username: String? = null,
    /** Used in personal recommendation to the user ahs recommended a song to. Only applicable
     * to self, i.e., parentUser or the logged in user.*/
    @SerialName("users")
    val usersList: List<String>? = null,
    /** Used for follow following taglines. This is the one who followed. */
    @SerialName("user_name_0")
    val user0: String? = null,
    /** Used for follow following taglines. This is the one who was followed. */
    @SerialName("user_name_1")
    val user1: String? = null
) {
    val sharedTransitionId
        get() = trackMetadata?.sharedTransitionId +
                entityId.orEmpty() +
                entityName.orEmpty() +
                username.orEmpty() + (listenedAt ?: insertedAt ?: "")
}