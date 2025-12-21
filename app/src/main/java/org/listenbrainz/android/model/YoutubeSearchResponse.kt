package org.listenbrainz.android.model

import kotlinx.serialization.Serializable

@Serializable
data class YouTubeSearchResponse(
    val items: List<YouTubeVideoItem> = emptyList()
)

@Serializable
data class YouTubeVideoItem(
    val id: YouTubeVideoId = YouTubeVideoId(),
)

@Serializable
data class YouTubeVideoId(
    val videoId: String = ""
)