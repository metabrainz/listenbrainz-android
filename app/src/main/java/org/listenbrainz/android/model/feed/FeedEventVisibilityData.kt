package org.listenbrainz.android.model.feed

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedEventVisibilityData (
    @SerialName("event_type") val eventType : String? = null,
    @SerialName("event_id") val eventId   : String? = null
)
