package org.listenbrainz.android.presentation.features.brainzplayer.ui.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.listenbrainz.android.data.repository.AlbumRepository
import org.listenbrainz.android.data.repository.AppPreferences
import org.listenbrainz.android.data.sources.brainzplayer.Album
import org.listenbrainz.android.data.sources.brainzplayer.Song
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val albumRepository: AlbumRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {
    val albums = albumRepository.getAlbums()
    
    // Refreshing variables.
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()
    
    init {
        fetchAlbumsFromDevice()
    }
    
    fun fetchAlbumsFromDevice(userRequestedRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            if (userRequestedRefresh){
                _isRefreshing.update { true }
                appPreferences.albumsOnDevice = albumRepository.addAlbums(userRequestedRefresh = true)
                _isRefreshing.update { false }
            } else {
                albums.collectLatest {
                    if (it.isEmpty()) {
                        _isRefreshing.update { true }
                        appPreferences.albumsOnDevice = albumRepository.addAlbums()
                        _isRefreshing.update { false }
                    }
                }
            }
        }
    }
    
    fun getAlbumFromID(albumID: Long): Flow<Album> {
        return albumRepository.getAlbum(albumID)
    }
    fun getAllSongsOfAlbum(albumID: Long): Flow<List<Song>>{
        return albumRepository.getAllSongsOfAlbum(albumID)
    }
}