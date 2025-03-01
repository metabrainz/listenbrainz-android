package org.listenbrainz.android.repository.playlists

import org.listenbrainz.android.model.playlist.CopyPlaylistResponse
import org.listenbrainz.android.model.playlist.PlaylistPayload
import org.listenbrainz.android.util.Resource

interface PlaylistDataRepository {

    // Fetches the playlist with tracks for the given MBID
    suspend fun fetchPlaylist(playlistMbid: String?): Resource<PlaylistPayload?>

    //Duplicates the playlist with the given MBID and returns the new playlist MBID
    suspend fun copyPlaylist(playlistMbid: String?): Resource<CopyPlaylistResponse?>

    suspend fun deletePlaylist(playlistMbid: String?): Resource<Unit>

    //Gets cover art of a particular playlist
    suspend fun getPlaylistCoverArt(
        playlistMBID: String,
        dimension: Int = DEFAULT_PLAYLIST_GRID_SIZE,
        layout: Int = DEFAULT_LAYOUT
    ): Resource<String?>

    companion object {
        const val DEFAULT_PLAYLIST_GRID_SIZE = 3
        const val DEFAULT_PLAYLIST_LIST_VIEW_GRID_SIZE = 2
        const val DEFAULT_LAYOUT = 0
    }
}