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
import org.listenbrainz.android.viewmodel.LikeState
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.brainzplayer.SongRepository
import javax.inject.Inject

@HiltViewModel
class SongViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {
    val songs = songRepository.getSongsStream()

    // Liking/disliking state
    private val _likeState = MutableStateFlow(LikeState.NEUTRAL)
    val likeState = _likeState.asStateFlow()

    fun onLikeTap() {
        _likeState.value = when (_likeState.value) {
            LikeState.NEUTRAL -> LikeState.LIKED
            LikeState.LIKED -> LikeState.DISLIKED
            LikeState.DISLIKED -> LikeState.NEUTRAL
        }

        submitLikeToApi()
    }

    fun onLikeLongPress() {
        _likeState.value = LikeState.NEUTRAL
        submitLikeToApi()
    }

    private fun submitLikeToApi() {
        val score = when (_likeState.value) {
            LikeState.LIKED -> 1
            LikeState.DISLIKED -> -1
            LikeState.NEUTRAL -> 0
        }
    }

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