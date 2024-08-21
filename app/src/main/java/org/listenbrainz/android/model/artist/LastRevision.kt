package org.listenbrainz.android.model.artist

import com.google.gson.annotations.SerializedName

data class LastRevision(
    val id: Int? = null,
    val rating: Int? = null,
    @SerializedName("review_id") val reviewId: String? = null,
    val text: String? = null,
    val timestamp: String? = null
)