package org.listenbrainz.android.model.feed

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class FeedPayload(
    val count: Int,
    val events: List<FeedEvent>,
    @SerialName("user_id") val userId: String
)