package org.listenbrainz.android.model.feed

import com.google.gson.annotations.SerializedName

/** Users can only delete the following: recording_recommendation, notification or recording_pin. */
data class FeedEventDeletionData (
    
    @SerializedName("event_type" ) var eventType : String? = null,
    @SerializedName("id"         ) var eventId   : String? = null

)