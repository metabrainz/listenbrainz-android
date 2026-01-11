package org.listenbrainz.android.model.artist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LastRevision(
    val id: Int? = null,
    val rating: Int? = null,
    @SerialName("review_id") val reviewId: String? = null,
    val text: String? = null,
    val timestamp: String? = null
)