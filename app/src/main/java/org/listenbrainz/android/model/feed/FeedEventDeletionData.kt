package org.listenbrainz.android.model.feed

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedEventDeletionData (
    @SerialName("event_type") val eventType : String? = null,
    @SerialName("id") val eventId   : String? = null
)