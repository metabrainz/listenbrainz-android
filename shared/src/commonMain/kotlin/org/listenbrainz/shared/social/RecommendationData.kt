package org.listenbrainz.shared.social

import kotlinx.serialization.Serializable

@Serializable
data class RecommendationData(
    val metadata: RecommendationMetadata = RecommendationMetadata()
)
