package org.listenbrainz.android.model

import kotlinx.serialization.Serializable

@Serializable
data class BlogPost(
    val ID: String = "",
    val title: String = "",
    val URL: String = "",
    val content: String = ""
)