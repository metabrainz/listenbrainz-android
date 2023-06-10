package org.listenbrainz.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.brainzplayer.SongRepository
import javax.inject.Inject

@HiltViewModel
class SongViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {
    val songs = songRepository.getSongsStream()
    
    // Refreshing variables.
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()
    
    init {
        fetchSongsFromDevice()
    }
    
    fun fetchSongsFromDevice(userRequestedRefresh: Boolean = false){
        viewModelScope.launch(Dispatchers.IO) {
            if (userRequestedRefresh){
                _isRefreshing.update { true }
                appPreferences.songsOnDevice = songRepository.addSongs(userRequestedRefresh = true)
                _isRefreshing.update { false }
            } else {
                songs.collectLatest {
                    if (it.isEmpty()) {
                        _isRefreshing.update { true }
                        appPreferences.songsOnDevice = songRepository.addSongs()
                        _isRefreshing.update { false }
                    }
                }
            }
        }
    }
}