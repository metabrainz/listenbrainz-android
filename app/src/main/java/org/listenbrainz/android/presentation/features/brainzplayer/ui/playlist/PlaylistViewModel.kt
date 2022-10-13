package org.listenbrainz.android.presentation.features.brainzplayer.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.listenbrainz.android.data.repository.PlaylistRepository
import org.listenbrainz.android.data.sources.brainzplayer.Playlist
import org.listenbrainz.android.data.sources.brainzplayer.Playlist.Companion.currentlyPlaying
import org.listenbrainz.android.data.sources.brainzplayer.Playlist.Companion.favourite
import org.listenbrainz.android.data.sources.brainzplayer.Song
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository
) : ViewModel() {
    val playlists = playlistRepository.getAllPlaylist()
    val playlistsCollectedFromChecklist = MutableStateFlow(mutableListOf<Playlist>())
    init {
        viewModelScope.launch {
            playlists.collectLatest {
                if (it.isEmpty()){
                    playlistRepository.insertPlaylists(listOf(currentlyPlaying, favourite))
                }
            }
        }
    }
    fun getPlaylistByID(playlistID: Long): Flow<Playlist> {
        return playlistRepository.getPlaylist(playlistID)
    }
    suspend fun createPlaylist(name: String){
        val id = Random.nextLong()
        val newPlaylist = Playlist(id, name, listOf())
        playlistRepository.insertPlaylist(newPlaylist)
    }
    suspend fun addSongToPlaylist(song: Song, playlist: Playlist){
        playlistRepository.insertSongToPlaylist(song, playlist)
    }
    suspend fun addSongToNewPlaylist(song: Song, playlistName: String){
        val id = Random.nextLong()
        val newPlaylist = Playlist(id, playlistName, listOf(song))
        playlistRepository.insertPlaylist(newPlaylist)
    }
    suspend fun renamePlaylist(playlist: Playlist, newName: String){
        playlistRepository.renamePlaylist(newName,playlist.id)
    }
    suspend fun deletePlaylist(playlist: Playlist){
        playlistRepository.deletePlaylist(playlist = playlist)
    }
    suspend fun deleteSongFromPlaylist(song:Song, playlist: Playlist){
        playlistRepository.deleteSongFromPlaylist(song,playlist)
    }

    suspend fun updatePlaylist(songs: List<Song>, playlistID: Long) =
        playlistRepository.updatePlaylist(songs, playlistID)
}