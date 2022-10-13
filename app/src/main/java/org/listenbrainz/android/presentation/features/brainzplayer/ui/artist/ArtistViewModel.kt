package org.listenbrainz.android.presentation.features.brainzplayer.ui.artist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.listenbrainz.android.data.repository.ArtistRepository
import org.listenbrainz.android.data.sources.brainzplayer.Album
import org.listenbrainz.android.data.sources.brainzplayer.Artist
import org.listenbrainz.android.data.sources.brainzplayer.Song
import javax.inject.Inject

@HiltViewModel
class ArtistViewModel @Inject constructor(
    private val artistRepository: ArtistRepository,
) : ViewModel() {
    val artists = artistRepository.getArtists()

    init {
        viewModelScope.launch {
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