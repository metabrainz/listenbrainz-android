package org.listenbrainz.android.model.artist

import kotlinx.serialization.Serializable

@Serializable
data class WikipediaExtract(
    val canonical: String? = null,
    val content: String? = null,
    val language: String? = null,
    val title: String? = null,
    val url: String? = null
)