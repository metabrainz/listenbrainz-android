package org.listenbrainz.android.model

import kotlinx.serialization.Serializable

@Serializable
data class CoverArt(
    val images: List<Image> = emptyList(),
    val release: String? = null
)