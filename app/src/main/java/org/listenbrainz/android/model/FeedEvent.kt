package org.listenbrainz.android.model

import androidx.compose.runtime.Stable
import com.google.gson.annotations.SerializedName

@Stable
data class FeedEvent(
    @SerializedName("id"        ) val id: Int? = null,
    @SerializedName("created"   ) val created: Int,
    @SerializedName("event_type") val type: String,
    @SerializedName("hidden"    ) val hidden: Boolean? = null,
    @SerializedName("metadata"  ) val metadata: Metadata,
    @SerializedName("user_name" ) val username: String? = null,
    @SerializedName("user_id"   ) val userId: Int? = null
) {
    /** Quick getter for blurbContent.*/
    val blurbContent: String
        get() = this.metadata.blurbContent ?: "Oops! Error loading content."
}