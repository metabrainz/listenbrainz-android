package org.listenbrainz.android.model

data class FeedPayload(
    val count: Int,
    val events: List<FeedEvent>,
    val user_id: String
)