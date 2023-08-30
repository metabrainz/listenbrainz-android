package org.listenbrainz.android.model.feed

import com.google.gson.annotations.SerializedName

/** Users can only hide pins/recommendations of users they're following from their feed. */
data class FeedEventVisibilityData (
    
    @SerializedName("event_type" ) var eventType : String? = null,
    @SerializedName("event_id"   ) var eventId   : String? = null

)

