package org.listenbrainz.android.model

import kotlinx.serialization.Serializable

@Serializable
data class WikiDataResponse(
    val sitelinks: Map<String, WikiDataEntry>? = null,
    val type: String? = null,
    val id: String? = null
)