package org.listenbrainz.android.model.feed

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.listenbrainz.android.model.Metadata

@Immutable
@Serializable
data class FeedEvent(
    @SerialName("id") val id: Int? = null,
    @SerialName("created") val created: Long = 0L,
    @SerialName("event_type") val type: String = "",
    @SerialName("hidden") val hidden: Boolean? = null,
    @SerialName("metadata") val metadata: Metadata = Metadata(),
    @SerialName("user_name") val username: String? = null,
    @SerialName("similarity") val similarity: Float? = null,
    @SerialName("user_id") val userId: Int? = null
) {
    /** Quick getter for blurbContent.*/
    val blurbContent: String?
        get() = metadata.message ?: metadata.blurbContent ?: metadata.text
}