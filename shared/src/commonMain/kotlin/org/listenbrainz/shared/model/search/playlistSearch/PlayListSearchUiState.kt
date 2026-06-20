package org.listenbrainz.shared.model.search.playlistSearch

data class PlayListSearchUiState(
    val playlists: List<PlaylistUiModel> = emptyList(),
)
