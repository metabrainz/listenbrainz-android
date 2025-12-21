package org.listenbrainz.android.model

import kotlinx.serialization.Serializable

@Serializable
data class Review(
    val metadata: ReviewMetadata? = null
)