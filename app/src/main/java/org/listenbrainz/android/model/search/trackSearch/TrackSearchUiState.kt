package org.listenbrainz.android.model.search.trackSearch

import org.listenbrainz.android.model.playlist.PlaylistTrack

data class TrackSearchUiState(
    val tracks : List<PlaylistTrack> = emptyList()
)
