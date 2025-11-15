package org.listenbrainz.android.model.feed

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedEventDeletionData (
    @SerialName("event_type")
    @SerializedName("event_type" ) var eventType : String? = null,
    @SerialName("id")
    @SerializedName("id"         ) var eventId   : String? = null
)