package org.listenbrainz.android.model

import kotlinx.serialization.Serializable

@Serializable
data class SearchResult(
    val users: List<User>? = null
)