package org.listenbrainz.android.repository.playlists

import org.listenbrainz.android.model.playlist.AddCopyPlaylistResponse
import org.listenbrainz.android.model.playlist.DeleteTracks
import org.listenbrainz.android.model.playlist.EditPlaylistResponse
import org.listenbrainz.android.model.playlist.MoveTrack
import org.listenbrainz.android.model.playlist.PlaylistPayload
import org.listenbrainz.android.model.playlist.PlaylistTrack
import org.listenbrainz.android.model.recordingSearch.RecordingSearchPayload
import org.listenbrainz.android.util.Resource

interface PlaylistDataRepository {

    // Fetches the playlist with tracks for the given MBID
    suspend fun fetchPlaylist(playlistMbid: String?): Resource<PlaylistPayload?>

    //Duplicates the playlist with the given MBID and returns the new playlist MBID
    suspend fun copyPlaylist(playlistMbid: String?): Resource<AddCopyPlaylistResponse?>

    suspend fun deletePlaylist(playlistMbid: String?): Resource<Unit>

    //Gets cover art of a particular playlist
    suspend fun getPlaylistCoverArt(
        playlistMBID: String,
        dimension: Int = DEFAULT_PLAYLIST_GRID_SIZE,
        layout: Int = DEFAULT_LAYOUT
    ): Resource<String?>

    //Function to add a playlist
    suspend fun addPlaylist(playlistPayload: PlaylistPayload):
            Resource<AddCopyPlaylistResponse?>

    //Edit a playlist
    suspend fun editPlaylist(playlistPayload: PlaylistPayload, playlistMbid: String?):
            Resource<EditPlaylistResponse?>

    //Search for a recording
    suspend fun searchRecording(searchQuery: String?, mbid: String? = null): Resource<RecordingSearchPayload?>

    //Move a track in a playlist
    suspend fun moveTrack(playlistMbid: String?, moveTrack: MoveTrack): Resource<EditPlaylistResponse?>

    //Add tracks to a playlist
    suspend fun addTracks(playlistMbid: String?, playlistTracks: List<PlaylistTrack>): Resource<EditPlaylistResponse?>

    //Delete tracks from a playlist
    suspend fun deleteTracks(playlistMbid: String?, deleteTracks: DeleteTracks): Resource<EditPlaylistResponse?>

    companion object {
        const val DEFAULT_PLAYLIST_GRID_SIZE = 3
        const val DEFAULT_PLAYLIST_LIST_VIEW_GRID_SIZE = 2
        const val DEFAULT_LAYOUT = 0
    }
}