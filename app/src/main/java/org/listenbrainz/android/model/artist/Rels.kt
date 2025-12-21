package org.listenbrainz.android.model.artist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Rels(
    @SerialName("free streaming") val freeStreaming: String? = null,
    @SerialName("purchase for download") val purchaseForDownload: String? = null,
    @SerialName("purchase for mail order") val purchaseForMailOrder: String? = null,
    @SerialName("official homepage") val officialHomePage: String? = null,
    @SerialName("social network") val socialNetwork: String? = null,
    val wikidata: String? = null,
    val youtube: String? = null,
    val lyrics: String? = null,
    val streaming: String? = null,
)