package org.listenbrainz.android.model.feed

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName

@Immutable
data class FeedPayload(
    val count: Int,
    val events: List<FeedEvent>,
    @SerializedName("user_id") val userId: String
)