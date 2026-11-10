package org.listenbrainz.android.model

import kotlinx.serialization.Serializable

@Serializable
data class WikiDataEntry(
    val site: String? = null,
    val title: String? = null,
    val url: String? = null
)