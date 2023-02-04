package org.listenbrainz.android.data.repository

import kotlinx.coroutines.flow.Flow
import org.listenbrainz.android.data.sources.brainzplayer.Playlist
import org.listenbrainz.android.data.sources.brainzplayer.Song

interface PlaylistRepository {

    fun getAllPlaylist(): Flow<List<Playlist>>

    fun getPlaylist(playlistId: Long): Flow<Playlist>

    suspend fun insertPlaylist(playlist: Playlist)

    suspend fun insertPlaylists(playlists: List<Playlist>)

    suspend fun updatePlaylist(songs: List<Song>,playlistID: Long)

    suspend fun deletePlaylist(playlist: Playlist)

    suspend fun insertSongToPlaylist(song: Song, playlist: Playlist)

    suspend fun insertSongsToPlaylist(songs: List<Song>, playlist: Playlist)

    suspend fun deleteSongFromPlaylist(song: Song, playlist: Playlist)

    suspend fun renamePlaylist(newName: String, playlistID: Long)
}