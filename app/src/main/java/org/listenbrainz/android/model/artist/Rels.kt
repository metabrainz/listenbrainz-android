package org.listenbrainz.android.model.artist

import com.google.gson.annotations.SerializedName

data class Rels(
    @SerializedName("free streaming") val freeStreaming: String? = null,
    @SerializedName("purchase for download") val purchaseForDownload: String? = null,
    @SerializedName("social network") val socialNetwork: String? = null,
    val wikidata: String? = null,
    val youtube: String? = null
)