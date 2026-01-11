package org.listenbrainz.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.listenbrainz.android.repository.brainzplayer.SongRepository

class SongViewModel(
    private val songRepository: SongRepository,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    val songs = songRepository.getSongsStream()
    
    // Refreshing variables.
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()
    
    init {
        fetchSongsFromDevice()
    }
    
    fun fetchSongsFromDevice(userRequestedRefresh: Boolean = false){
        viewModelScope.launch(ioDispatcher) {
            if (userRequestedRefresh){
                _isRefreshing.update { true }
                songRepository.addSongs(userRequestedRefresh = true)
                _isRefreshing.update { false }
            } else {
                songs.collectLatest {
                    if (it.isEmpty()) {
                        _isRefreshing.update { true }
                        songRepository.addSongs()
                        _isRefreshing.update { false }
                    }
                }
            }
        }
    }
}