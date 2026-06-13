package org.listenbrainz.shared.model.search.playlistSearch

data class PlaylistUiModel(
    val mbid: String,
    val title: String,
    val description: String,
    val creator: String,
    val date: String
)