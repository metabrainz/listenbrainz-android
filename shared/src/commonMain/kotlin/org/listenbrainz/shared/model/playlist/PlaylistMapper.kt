package org.listenbrainz.shared.model.playlist

import org.listenbrainz.shared.model.search.playlistSearch.PlaylistUiModel

fun PlaylistData.toUiModel(): PlaylistUiModel {
    return PlaylistUiModel(
        mbid = this.getPlaylistMBID() ?: "",
        title = this.title ?: "Untitled Playlist",
        description = this.annotation ?: "No description provided",
        creator = this.creator ?: "Unknown Creator",
        date = this.date ?: "Unknown Date"
    )
}