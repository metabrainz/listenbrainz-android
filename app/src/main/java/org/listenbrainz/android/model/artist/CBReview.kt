package org.listenbrainz.android.model.artist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CBReview(
    @SerialName("average_rating") val averageRating: AverageRating? = null,
    val count: Int? = null,
    val limit: Int? = null,
    val offset: Int? = null,
    val reviews: List<Review?>? = null
)