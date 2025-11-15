package org.listenbrainz.android.model.feed

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedEventVisibilityData (
    @SerialName("event_type")
    @SerializedName("event_type" ) var eventType : String? = null,
    @SerialName("event_id")
    @SerializedName("event_id"   ) var eventId   : String? = null

)
