package org.listenbrainz.android.model.artist

import kotlinx.serialization.Serializable

@Serializable
data class ArtistWikiExtract(
    val wikipediaExtract: WikipediaExtract? = null
)