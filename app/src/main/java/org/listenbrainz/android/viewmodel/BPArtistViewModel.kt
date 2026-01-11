package org.listenbrainz.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.listenbrainz.android.model.Album
import org.listenbrainz.android.model.Artist
import org.listenbrainz.android.model.Song
import org.listenbrainz.android.repository.brainzplayer.BPArtistRepository

class BPArtistViewModel(
    private val bpArtistRepository: BPArtistRepository,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    val artists = bpArtistRepository.getArtists()
    
    // Refreshing variables.
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()
    
    init {
        fetchArtistsFromDevice()
    }
    
    fun fetchArtistsFromDevice(userRequestedRefresh: Boolean = false){
        viewModelScope.launch(ioDispatcher) {
            if (userRequestedRefresh){
                _isRefreshing.update { true }
                bpArtistRepository.addArtists(userRequestedRefresh = true)
                _isRefreshing.update { false }
            } else {
                artists.collectLatest {
                    if (it.isEmpty()) {
                        _isRefreshing.update { true }
                        bpArtistRepository.addArtists()
                        _isRefreshing.update { false }
                    }
                }
            }
        }
    }
    
    fun getArtistByID(artistID: String): Flow<Artist> {
        return bpArtistRepository.getArtist(artistID)
    }
    
    fun getAllSongsOfArtist(artist: Artist): Flow<List<Song>> {
        return flowOf(artist.songs)
    }
    
    fun getAllAlbumsOfArtist(artist: Artist): Flow<List<Album>> {
        return flowOf(artist.albums)
    }
}