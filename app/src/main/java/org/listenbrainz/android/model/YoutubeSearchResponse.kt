package org.listenbrainz.android.model

data class YouTubeSearchResponse(
    val items: List<YouTubeVideoItem>
)

data class YouTubeVideoItem(
    val id: YouTubeVideoId,
)

data class YouTubeVideoId(
    val videoId: String
)