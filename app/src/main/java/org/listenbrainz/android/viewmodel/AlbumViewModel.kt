package org.listenbrainz.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.listenbrainz.android.model.Album
import org.listenbrainz.android.model.Song
import org.listenbrainz.android.repository.AlbumRepository
import org.listenbrainz.android.repository.AppPreferences
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