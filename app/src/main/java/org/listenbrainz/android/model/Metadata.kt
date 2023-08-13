package org.listenbrainz.android.model

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName


@Immutable
data class Metadata(
    @SerializedName("blurb_content"    ) val blurbContent: String? = null,
    @SerializedName("created"          ) val created: Int? = null,
    @SerializedName("entity_id"        ) val entityId: String? = null,
    @SerializedName("entity_name"      ) val entityName: String? = null,
    @SerializedName("entity_type"      ) val entityType: String? = null,
    @SerializedName("inserted_at"      ) val insertedAt: Int? = null,
    @SerializedName("listened_at"      ) val listenedAt: Int? = null,
    @SerializedName("listened_at_iso"  ) val listenedAtIso: Any? = null,
    @SerializedName("message"          ) val message: String? = null,
    @SerializedName("playing_now"      ) val playingNow: Any? = null,
    @SerializedName("rating"           ) val rating: Int? = null,
    @SerializedName("relationship_type") val relationshipType: String? = null,
    @SerializedName("review_mbid"      ) val reviewMbid: String? = null,
    @SerializedName("text"             ) val text: String? = null,
    @SerializedName("track_metadata"   ) val trackMetadata: TrackMetadata? = null,
    @SerializedName("user_name"        ) val username: String? = null,
    /** Used in personal recommendation to the user ahs recommended a song to. Only applicable
     * to self, i.e., parentUser or the logged in user.*/
    @SerializedName("users"            ) val usersList: List<String>? = null,
    /** Used for follow following taglines. This is the one who followed. */
    @SerializedName("user_name_0"      ) val user0: String? = null,
    /** Used for follow following taglines. This is the one who was followed. */
    @SerializedName("user_name_1"      ) val user1: String? = null
)