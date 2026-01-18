package org.listenbrainz.android.model.search

import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.Song
import org.listenbrainz.android.model.search.userSearch.UserListUiState
import org.listenbrainz.android.model.search.albumSearch.AlbumSearchUiState
import org.listenbrainz.android.model.search.artistSearch.ArtistSearchUiState
import org.listenbrainz.android.model.search.playlistSearch.PlayListSearchUiState
import org.listenbrainz.android.model.search.trackSearch.TrackSearchUiState

data class SearchUiState(
    val selectedSearchType: SearchType = SearchType.USER,
    val query: String,
    val result: SearchData?,
    val error: ResponseError?
)

sealed interface SearchData {
    data class Users(val data: UserListUiState) : SearchData
    data class Playlists(val data: PlayListSearchUiState) : SearchData
    data class Artists(val data: ArtistSearchUiState) : SearchData
    data class Albums(val data : AlbumSearchUiState):SearchData
    data class Tracks(val data : TrackSearchUiState): SearchData
    data class Songs(val data: List<Song>) : SearchData
}
