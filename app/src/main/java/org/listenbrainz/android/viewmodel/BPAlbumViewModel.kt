package org.listenbrainz.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.listenbrainz.android.model.Album
import org.listenbrainz.shared.model.Song
import org.listenbrainz.android.repository.brainzplayer.BPAlbumRepository

class BPAlbumViewModel(
    private val bpAlbumRepository: BPAlbumRepository,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    val albums = bpAlbumRepository.getAlbums()
    
    // Refreshing variables.
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()
    
    init {
        fetchAlbumsFromDevice()
    }
    
    fun fetchAlbumsFromDevice(userRequestedRefresh: Boolean = false) {
        viewModelScope.launch(ioDispatcher) {
            if (userRequestedRefresh){
                _isRefreshing.update { true }
                bpAlbumRepository.addAlbums(userRequestedRefresh = true)
                _isRefreshing.update { false }
            } else {
                albums.collectLatest {
                    if (it.isEmpty()) {
                        _isRefreshing.update { true }
                        bpAlbumRepository.addAlbums()
                        _isRefreshing.update { false }
                    }
                }
            }
        }
    }
    
    fun getAlbumFromID(albumID: Long): Flow<Album> {
        return bpAlbumRepository.getAlbum(albumID)
    }
    fun getAllSongsOfAlbum(albumID: Long): Flow<List<Song>>{
        return bpAlbumRepository.getAllSongsOfAlbum(albumID)
    }
}