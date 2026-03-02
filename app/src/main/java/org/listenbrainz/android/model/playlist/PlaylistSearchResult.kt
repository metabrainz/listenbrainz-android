package org.listenbrainz.android.model.playlist

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistSearchResult(
    val playlists : List<PlaylistPayload> = emptyList()
)
