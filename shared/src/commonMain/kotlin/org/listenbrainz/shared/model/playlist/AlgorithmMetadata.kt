package org.listenbrainz.shared.model.playlist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlgorithmMetadata(
    @SerialName("source_patch")
    val sourcePatch: String? = null
)