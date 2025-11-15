package org.listenbrainz.android.model.feed

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class FeedPayload(
    @SerializedName("count") @SerialName("count") val count: Int,
    @SerializedName("events") @SerialName("events") val events: List<FeedEvent>,
    @SerializedName("user_id") @SerialName("user_id") val userId: String
)