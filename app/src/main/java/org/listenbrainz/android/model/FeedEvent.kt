package org.listenbrainz.android.model

import com.google.gson.annotations.SerializedName

data class FeedEvent(
    @SerializedName("id"        ) val id: Int,
    @SerializedName("created"   ) val created: Int,
    @SerializedName("event_type") val eventType: String,
    @SerializedName("hidden"    ) val hidden: Boolean? = null,
    @SerializedName("metadata"  ) val metadata: Metadata,
    @SerializedName("user_name" ) val userName: String? = null,
    @SerializedName("user_id"   ) val userId: Int? = null
)