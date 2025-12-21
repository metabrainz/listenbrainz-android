package org.listenbrainz.android.model

import kotlinx.serialization.Serializable

@Serializable
data class Blog(
    val posts: List<BlogPost> = emptyList()
)