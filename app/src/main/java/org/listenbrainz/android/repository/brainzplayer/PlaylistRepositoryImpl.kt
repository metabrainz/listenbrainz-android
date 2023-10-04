package org.listenbrainz.android.repository.brainzplayer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.listenbrainz.android.model.dao.PlaylistDao
import org.listenbrainz.android.util.Transformer.toPlaylist
import org.listenbrainz.android.util.Transformer.toPlaylistEntity
import org.listenbrainz.android.util.Transformer.toSongEntity
import org.listenbrainz.android.model.Playlist
import org.listenbrainz.android.model.Song
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepositoryImpl @Inject constructor(
   private val playlistDao: PlaylistDao
): PlaylistRepository {
    override fun getAllPlaylist(): Flow<List<Playlist>> = playlistDao.getAllPlaylist().map {
        it.map { playlistEntity ->
            playlistEntity.toPlaylist()
        }
    }

    override fun getPlaylist(playlistId: Long): Flow<Playlist> =
        playlistDao.getPlaylist(playlistId).map {
            it.toPlaylist()
        }

    override suspend fun insertPlaylist(playlist: Playlist) {
        playlistDao.insertPlaylist(playlist.toPlaylistEntity())
    }

    override suspend fun insertPlaylists(playlists: List<Playlist>) {
        playlistDao.insertPlaylists(playlists.map {
            it.toPlaylistEntity()
        })
    }

    override suspend fun updatePlaylist(songs: List<Song>, playlistID: Long) {
        playlistDao.updatePlaylistSongs(songs.map {
            it.toSongEntity()
        }, playlistID)
    }


    override suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.delete(playlist.toPlaylistEntity())
    }

    override suspend fun insertSongToPlaylist(song: Song, playlist: Playlist) {
        playlist.items.plus(song)
        val newList = mutableListOf(song)
        newList.addAll(playlist.items)
        updatePlaylist(newList, playlist.id)
    }

    override suspend fun insertSongsToPlaylist(songs: List<Song>, playlist: Playlist) {
        playlist.items.plus(songs)
        songs.plus(playlist.items)
        updatePlaylist(songs, playlist.id)
    }

    override suspend fun deleteSongFromPlaylist(song: Song, playlist: Playlist) {
        val newList = playlist.items.toMutableList().filter { it.mediaID != song.mediaID }
        updatePlaylist(newList, playlist.id)
    }

    override suspend fun renamePlaylist(newName: String, playlistID: Long) {
        playlistDao.renamePlaylistName(newName, playlistID)
    }
}