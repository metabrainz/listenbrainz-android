package org.listenbrainz.shared.model.search.trackSearch

import org.listenbrainz.shared.model.playlist.PlaylistTrack

data class TrackSearchUiState(
    val tracks : List<PlaylistTrack> = emptyList()
)