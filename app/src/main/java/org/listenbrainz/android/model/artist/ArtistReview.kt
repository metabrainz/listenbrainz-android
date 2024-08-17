package org.listenbrainz.android.model.artist

import com.google.gson.annotations.SerializedName

data class ArtistReview(
    @SerializedName("average_rating") val averageRating: AverageRating? = null,
    val count: Int? = null,
    val limit: Int? = null,
    val offset: Int? = null,
    val reviews: List<Review?>? = null
)