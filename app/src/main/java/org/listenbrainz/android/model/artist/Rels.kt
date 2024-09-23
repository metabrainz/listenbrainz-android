package org.listenbrainz.android.model.artist

import com.google.gson.annotations.SerializedName

data class Rels(
    @SerializedName("free streaming") val freeStreaming: String? = null,
    @SerializedName("purchase for download") val purchaseForDownload: String? = null,
    @SerializedName("purchase for mail order") val purchaseForMailOrder: String? = null,
    @SerializedName("official homepage") val officialHomePage: String? = null,
    @SerializedName("social network") val socialNetwork: String? = null,
    val wikidata: String? = null,
    val youtube: String? = null,
    val lyrics: String? = null,
    val streaming: String? = null,
)