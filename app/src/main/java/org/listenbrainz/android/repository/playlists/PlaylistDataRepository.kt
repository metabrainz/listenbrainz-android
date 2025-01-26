package org.listenbrainz.android.repository.playlists

import org.listenbrainz.android.model.playlist.CopyPlaylistResponse
import org.listenbrainz.android.model.playlist.PlaylistPayload
import org.listenbrainz.android.util.Resource

interface PlaylistDataRepository {

    // Fetches the playlist with tracks for the given MBID
    suspend fun fetchPlaylist(playlistMbid: String?): Resource<PlaylistPayload?>

    //Duplicates the playlist with the given MBID and returns the new playlist MBID
    suspend fun copyPlaylist(playlistMbid: String?): Resource<CopyPlaylistResponse?>
}