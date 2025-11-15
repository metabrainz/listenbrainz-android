package org.listenbrainz.android.model

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Immutable
@Serializable
data class Metadata(
    @SerializedName("blurb_content") @SerialName("blurb_content")
    val blurbContent: String? = null,
    @SerializedName("created") @SerialName("created")
    val created: Long? = null,
    @SerializedName("entity_id") @SerialName("entity_id")
    val entityId: String? = null,
    @SerializedName("entity_name") @SerialName("entity_name")
    val entityName: String? = null,
    @SerializedName("entity_type") @SerialName("entity_type")
    val entityType: String? = null,
    @SerializedName("inserted_at") @SerialName("inserted_at")
    val insertedAt: Long? = null,
    @SerializedName("listened_at") @SerialName("listened_at")
    val listenedAt: Long? = null,
    @SerializedName("listened_at_iso") @SerialName("listened_at_iso")
    val listenedAtIso: JsonElement? = null,
    @SerializedName("message") @SerialName("message")
    val message: String? = null,
    @SerializedName("playing_now") @SerialName("playing_now")
    val playingNow: JsonElement? = null,
    @SerializedName("rating") @SerialName("rating")
    val rating: Int? = null,
    @SerializedName("relationship_type") @SerialName("relationship_type")
    val relationshipType: String? = null,
    @SerializedName("review_mbid") @SerialName("review_mbid")
    val reviewMbid: String? = null,
    @SerializedName("text") @SerialName("text")
    val text: String? = null,
    @SerializedName("track_metadata") @SerialName("track_metadata")
    val trackMetadata: TrackMetadata? = null,
    @SerializedName("user_name") @SerialName("user_name")
    val username: String? = null,
    /** Used in personal recommendation to the user ahs recommended a song to. Only applicable
     * to self, i.e., parentUser or the logged in user.*/
    @SerializedName("users") @SerialName("users")
    val usersList: List<String>? = null,
    /** Used for follow following taglines. This is the one who followed. */
    @SerializedName("user_name_0") @SerialName("user_name_0")
    val user0: String? = null,
    /** Used for follow following taglines. This is the one who was followed. */
    @SerializedName("user_name_1") @SerialName("user_name_1")
    val user1: String? = null
) {
    val sharedTransitionId
        get() = trackMetadata?.sharedTransitionId +
                entityId.orEmpty() +
                entityName.orEmpty() +
                username.orEmpty() + (listenedAt ?: insertedAt ?: "")
}