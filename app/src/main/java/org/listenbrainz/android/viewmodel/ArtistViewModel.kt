package org.listenbrainz.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.listenbrainz.android.repository.ArtistRepository
import org.listenbrainz.android.model.Album
import org.listenbrainz.android.model.Artist
import org.listenbrainz.android.model.Song
import org.listenbrainz.android.util.AlbumsData
import javax.inject.Inject

@HiltViewModel
class ArtistViewModel @Inject constructor(
    private val artistRepository: ArtistRepository,
) : ViewModel() {
    val artists = artistRepository.getArtists()

    init {
        if (AlbumsData.albumsOnDevice)
            fetchArtistsFromDevice()
    }
    
    // TODO: Integrate a refresh button using this function.
    fun fetchArtistsFromDevice(){
        viewModelScope.launch(Dispatchers.IO) {
            artists.collectLatest {
                if (it.isEmpty()) artistRepository.addArtists()
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