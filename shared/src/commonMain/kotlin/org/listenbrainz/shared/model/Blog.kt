package org.listenbrainz.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class Blog(
    val posts: List<BlogPost> = emptyList()
)