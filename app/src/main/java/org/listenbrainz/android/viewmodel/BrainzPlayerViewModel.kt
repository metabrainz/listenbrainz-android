package org.listenbrainz.android.viewmodel

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ALL
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_NONE
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ONE
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_ALL
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_NONE
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.listenbrainz.android.model.Playable
import org.listenbrainz.android.model.PlayableType
import org.listenbrainz.android.model.Playlist.Companion.currentlyPlaying
import org.listenbrainz.android.model.RepeatMode
import org.listenbrainz.android.model.Song
import org.listenbrainz.android.repository.brainzplayer.SongRepository
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.service.BrainzPlayerService
import org.listenbrainz.android.service.BrainzPlayerServiceConnection
import org.listenbrainz.android.util.BrainzPlayerExtensions.currentPlaybackPosition
import org.listenbrainz.android.util.BrainzPlayerExtensions.isPlayEnabled
import org.listenbrainz.android.util.BrainzPlayerExtensions.isPlaying
import org.listenbrainz.android.util.BrainzPlayerExtensions.isPrepared
import org.listenbrainz.android.util.BrainzPlayerExtensions.toSong
import org.listenbrainz.android.util.BrainzPlayerUtils.MEDIA_ROOT_ID
import org.listenbrainz.android.util.Resource
import javax.inject.Inject

@HiltViewModel
class BrainzPlayerViewModel @Inject constructor(
    private val brainzPlayerServiceConnection: BrainzPlayerServiceConnection,
    private val songRepository: SongRepository,
    val appPreferences: AppPreferences,
) : ViewModel() {
    val pagerState = MutableStateFlow(0)
    private val _mediaItems = MutableStateFlow<Resource<List<Song>>>(Resource.loading())
    private val _songDuration = MutableStateFlow(0L)
    private val _songCurrentPosition = MutableStateFlow(0L)
    private val _progress = MutableStateFlow(0F)
    val mediaItem = _mediaItems.asStateFlow()
    val progress = _progress.asStateFlow()
    val songCurrentPosition = _songCurrentPosition.asStateFlow()
    val songs = songRepository.getSongsStream()
    private val playbackState = brainzPlayerServiceConnection.playbackState
    val isShuffled = brainzPlayerServiceConnection.shuffleState
    val currentlyPlayingSong = brainzPlayerServiceConnection.currentPlayingSong
    val isPlaying = brainzPlayerServiceConnection.isPlaying
    val playButton = brainzPlayerServiceConnection.playButtonState
    val repeatMode = brainzPlayerServiceConnection.repeatModeState
    var isSearching by mutableStateOf(false)

    init {
        updatePlayerPosition()
        _mediaItems.value = Resource.loading()
        brainzPlayerServiceConnection.subscribe(
            MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {
                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>
                ) {
                    super.onChildrenLoaded(parentId, children)
                    val songs = children.map {
                        it.toSong
                    }
                    _mediaItems.value = Resource(Resource.Status.SUCCESS, songs)

                }
            })
        viewModelScope.launch(Dispatchers.IO) {
            songs.collectLatest {
                if (it.isEmpty()) songRepository.addSongs()
                _mediaItems.value = Resource(Resource.Status.SUCCESS, it)
                currentlyPlaying.items.plus(it)
            }
        }
    }

    fun skipToNextSong() {
        brainzPlayerServiceConnection.transportControls.skipToNext()
        pagerState.value++
        // Updating currently playing song.
        appPreferences.currentPlayable = appPreferences.currentPlayable
            ?.copy(currentSongIndex = ( appPreferences.currentPlayable?.currentSongIndex!! + 1)   // Since BP won't be visible to users with no songs, we don't need to worry.
                .coerceAtMost(appPreferences.currentPlayable?.songs!!.size)
            )
    }

    fun skipToPreviousSong() {
        brainzPlayerServiceConnection.transportControls.skipToPrevious()
        pagerState.value--.coerceAtLeast(0)
        // Updating currently playing song.
        appPreferences.currentPlayable = appPreferences.currentPlayable
            ?.copy(currentSongIndex = ( appPreferences.currentPlayable?.currentSongIndex!! - 1)   // Since BP won't be visible to users with no songs, we don't need to worry.
                .coerceAtLeast(0)
            )
    }

    fun onSeek(seekTo: Float) {
        viewModelScope.launch { _progress.emit(seekTo) }
    }

    fun onSeeked() {
        brainzPlayerServiceConnection.transportControls.seekTo((_songDuration.value * progress.value).toLong())
    }

    fun shuffle() {
        val transportControls = brainzPlayerServiceConnection.transportControls
        transportControls.setShuffleMode(if (isShuffled.value) SHUFFLE_MODE_NONE else SHUFFLE_MODE_ALL)
    }

    fun repeatMode() {
        when (repeatMode.value) {
            RepeatMode.REPEAT_MODE_OFF -> brainzPlayerServiceConnection.transportControls.setRepeatMode(
                REPEAT_MODE_ONE
            )
            RepeatMode.REPEAT_MODE_ALL -> brainzPlayerServiceConnection.transportControls.setRepeatMode(
                REPEAT_MODE_NONE
            )
            RepeatMode.REPEAT_MODE_ONE -> brainzPlayerServiceConnection.transportControls.setRepeatMode(
                REPEAT_MODE_ALL
            )
        }
    }

    fun searchSongs(query: String): List<Song>? {
        val listToSearch = _mediaItems.value.data

        if (query.isEmpty()) {
            isSearching = false
        }
        val result: List<Song>? = listToSearch?.filter {
            it.title.contains(query.trim(), ignoreCase = true)
        }
        isSearching = true
        return result
    }

    fun playOrToggleSong(mediaItem: Song, toggle: Boolean = false) {
        val isPrepared = playbackState.value.isPrepared
        if (isPrepared && mediaItem.mediaID == currentlyPlayingSong.value.toSong.mediaID) {
            playbackState.value.let { playbackState ->
                when {
                    playbackState.isPlaying -> if (toggle) brainzPlayerServiceConnection.transportControls.pause()
                    playbackState.isPlayEnabled -> brainzPlayerServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        } else {
            brainzPlayerServiceConnection.transportControls.playFromMediaId(mediaItem.mediaID.toString(), null)
        }
    }

    fun queueChanged(mediaItem: Song, toggle: Boolean ) {
        brainzPlayerServiceConnection.transportControls.playFromMediaId(mediaItem.mediaID.toString(), null)
        playbackState.value.let { playbackState ->
            when {
                playbackState.isPlaying -> if (!toggle) brainzPlayerServiceConnection.transportControls.pause()
                playbackState.isPlayEnabled -> brainzPlayerServiceConnection.transportControls.play()
                else -> Unit
            }
        }
    }

    fun changePlayable(newPlayableList: List<Song>, playableType: PlayableType, playableId: Long, currentIndex: Int, seekTo: Long = 0L ) {
        appPreferences.currentPlayable =
            Playable(playableType, playableId, newPlayableList, currentIndex, seekTo)
    }
    
    /**Skip to the given song at given [index] in the current playlist.*/
    fun skipToPlayable(index: Int){
        appPreferences.currentPlayable = appPreferences.currentPlayable?.copy(currentSongIndex = index)
    }

    override fun onCleared() {
        super.onCleared()
        brainzPlayerServiceConnection.unsubscribe(MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {})
    }

    private fun updatePlayerPosition() {
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                val pos = playbackState.value.currentPlaybackPosition.toFloat()
                if (progress.value != pos) {
                    _progress.emit(pos / BrainzPlayerService.currentSongDuration)
                    _songDuration.emit(BrainzPlayerService.currentSongDuration)
                    _songCurrentPosition.emit(((pos / BrainzPlayerService.currentSongDuration) * BrainzPlayerService.currentSongDuration).toLong())

                }
                delay(100L)
            }
        }
    }
}