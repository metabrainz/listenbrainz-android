package org.listenbrainz.shared.model.feed

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.listenbrainz.shared.model.feed.FeedEvent

@Immutable
@Serializable
data class FeedPayload(
    @SerialName("count") val count: Int = 0,
    @SerialName("events") val events: List<FeedEvent> = emptyList(),
    @SerialName("user_id") val userId: String = ""
)