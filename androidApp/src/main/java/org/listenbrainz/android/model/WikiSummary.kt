package org.listenbrainz.android.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WikiSummary(
    @SerialName("displaytitle")
    val displayTitle: String? = null,
    val pageId: Long = 0,
    @SerialName("originalimage")
    val originalImage: OriginalImage? = null,
    val extract: String? = null
) {
    @Serializable
    data class OriginalImage(
        val source: String? = null,
        val width: Int = 0,
        val height: Int = 0
    )
}