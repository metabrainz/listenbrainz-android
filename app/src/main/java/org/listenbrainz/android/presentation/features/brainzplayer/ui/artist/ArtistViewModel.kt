package org.listenbrainz.android.presentation.features.brainzplayer.ui.artist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.listenbrainz.android.data.repository.AppPreferences
import org.listenbrainz.android.data.repository.ArtistRepository
import org.listenbrainz.android.data.sources.brainzplayer.Album
import org.listenbrainz.android.data.sources.brainzplayer.Artist
import org.listenbrainz.android.data.sources.brainzplayer.Song
import javax.inject.Inject

@HiltViewModel
class ArtistViewModel @Inject constructor(
    private val artistRepository: ArtistRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {
    val artists = artistRepository.getArtists()
    
    // Refreshing variables.
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()
    
    init {
        fetchArtistsFromDevice()
    }
    
    fun fetchArtistsFromDevice(userRequestedRefresh: Boolean = false){
        viewModelScope.launch(Dispatchers.IO) {
            if (userRequestedRefresh){
                _isRefreshing.update { true }
                appPreferences.albumsOnDevice = artistRepository.addArtists(userRequestedRefresh = true)
                _isRefreshing.update { false }
            } else {
                artists.collectLatest {
                    if (it.isEmpty()) {
                        _isRefreshing.update { true }
                        appPreferences.albumsOnDevice = artistRepository.addArtists()
                        _isRefreshing.update { false }
                    }
                }
            }
        }
    }
    
    fun getArtistByID(artistID: String): Flow<Artist> {
        return artistRepository.getArtist(artistID)
    }

    fun getAllSongsOfArtist(artist: Artist): Flow<List<Song>> {
        return flowOf(artist.songs)
    }

    fun getAllAlbumsOfArtist(artist: Artist): Flow<List<Album>> {
        return flowOf(artist.albums)
    }
}