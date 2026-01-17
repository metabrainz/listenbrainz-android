package org.listenbrainz.android.model.playlist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdditionalMetadata(
    @SerialName("algorithm_metadata")
    val algorithmMetadata: AlgorithmMetadata = AlgorithmMetadata(),
    @SerialName("expires_at")
    val expiresAt: String? = null
)