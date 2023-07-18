package org.listenbrainz.android.model

data class FeedEvent(
    val created: Int,
    val event_type: String,
    val hidden: Boolean,
    val id: Int,
    val metadata: Metadata,
    val user_name: String
)