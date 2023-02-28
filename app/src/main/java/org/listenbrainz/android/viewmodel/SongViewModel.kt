package org.listenbrainz.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.listenbrainz.android.repository.SongRepository
import org.listenbrainz.android.util.SongData
import javax.inject.Inject

@HiltViewModel
class SongViewModel @Inject constructor(
    private val songRepository: SongRepository
) : ViewModel() {
    val songs = songRepository.getSongsStream()

    init {
        if (SongData.songsOnDevice)
            fetchSongsFromDevice()
    }
    
    // TODO: Integrate a refresh button using this function.
    fun fetchSongsFromDevice(){
        viewModelScope.launch(Dispatchers.IO) {
            songs.collectLatest {
                if (it.isEmpty()) songRepository.addSongs()
            }
        }
    }
}