package org.listenbrainz.android.model.artist

import kotlinx.serialization.Serializable

@Serializable
data class AverageRating(
    val count: Int? = null,
    val rating: Double? = null
)