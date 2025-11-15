package org.listenbrainz.android.model.feed

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.listenbrainz.android.model.Metadata

@Immutable
@Serializable
data class FeedEvent(
    @SerializedName("id") @SerialName("id") val id: Int? = null,
    @SerializedName("created") @SerialName("created") val created: Long,
    @SerializedName("event_type") @SerialName("event_type") val type: String,
    @SerializedName("hidden") @SerialName("hidden") val hidden: Boolean? = null,
    @SerializedName("metadata") @SerialName("metadata") val metadata: Metadata,
    @SerializedName("user_name") @SerialName("user_name") val username: String? = null,
    @SerializedName("similarity") @SerialName("similarity") val similarity: Float? = null,
    @SerializedName("user_id") @SerialName("user_id") val userId: Int? = null
) {
    /** Quick getter for blurbContent.*/
    val blurbContent: String?
        get() = metadata.message ?: metadata.blurbContent ?: metadata.text
}