package org.listenbrainz.android.model

import com.google.gson.annotations.SerializedName

data class FeedPayload(
    val count: Int,
    val events: List<FeedEvent>,
    @SerializedName("user_id") val userId: String
)