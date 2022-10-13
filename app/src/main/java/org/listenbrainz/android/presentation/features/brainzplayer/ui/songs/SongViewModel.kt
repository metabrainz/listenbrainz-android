package org.listenbrainz.android.presentation.features.brainzplayer.ui.songs


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.listenbrainz.android.data.repository.SongRepository
import javax.inject.Inject

@HiltViewModel
class SongViewModel @Inject constructor(
    songRepository: SongRepository
) : ViewModel() {
    val songs = songRepository.getSongsStream()

    init {
        viewModelScope.launch {
            songs.collectLatest {
                if (it.isEmpty()) songRepository.addSongs()
            }
        }
    }
}