package org.listenbrainz.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class SearchResult(
    val users: List<User>? = null
)