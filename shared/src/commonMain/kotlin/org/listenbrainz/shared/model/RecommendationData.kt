package org.listenbrainz.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class RecommendationData(
    val metadata: RecommendationMetadata = RecommendationMetadata()
)