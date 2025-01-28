package org.listenbrainz.android.viewmodel

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ALL
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_NONE
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ONE
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_ALL
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_NONE
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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
    private val _mediaItems = MutableStateFlow<Resource<List<Song>>>(Resource.loading())
    private val _songDuration = MutableStateFlow(0L)
    private val _songCurrentPosition = MutableStateFlow(0L)
    private val _progress = MutableStateFlow(0F)
    val mediaItem = _mediaItems.asStateFlow()
    val progress = _progress.asStateFlow()
    val songCurrentPosition = _songCurrentPosition.asStateFlow()
    val songs = songRepository.getSongsStream()
    val recentlyPlayed = songRepository.getRecentlyPlayedSongs()
    val songsPlayedToday = songRepository.getSongsPlayedToday()
    val songsPlayedThisWeek = songRepository.getSongsPlayedThisWeek()
    private val playbackState = brainzPlayerServiceConnection.playbackState
    val isShuffled = brainzPlayerServiceConnection.shuffleState
    val currentlyPlayingSong = brainzPlayerServiceConnection.currentPlayingSong
    val isPlaying = brainzPlayerServiceConnection.isPlaying
    val playButton = brainzPlayerServiceConnection.playButtonState
    val repeatMode = brainzPlayerServiceConnection.repeatModeState

    var playerBackGroundColor by mutableStateOf(Color.Transparent)

    private val _searchQuery = MutableStateFlow(TextFieldValue(""))
    val searchQuery: StateFlow<TextFieldValue> = _searchQuery

    private val _searchItems = MutableStateFlow<List<Song>>(emptyList())
    val searchItems: StateFlow<List<Song>> = _searchItems

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

        viewModelScope.launch {
            _searchQuery
                .map { it.text }
                .debounce(200) // 0.2 Seconds
                .distinctUntilChanged()
                .collect { query ->
                    SearchSong(query)
                }
        }
    }

    fun updateBackgroundColorForPlayer(
        albumArtUrl: String?,
        defaultColor: Color,
        context: Context,
        isDarkThemeEnabled: Boolean
    ) {
        viewModelScope.launch {
            var dominantColor: Color = defaultColor
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(albumArtUrl)
                .allowHardware(false)
                .build()
            val result = loader.execute(request)
            val bitmap = (result as? SuccessResult)?.drawable?.let { drawable ->
                (drawable as? BitmapDrawable)?.bitmap
            }
            bitmap?.let { bitmap ->
                val palette = Palette.from(bitmap).generate()
                val swatch = run {
                    if (isDarkThemeEnabled) {
                        palette.darkMutedSwatch ?: palette.darkVibrantSwatch ?: palette.lightMutedSwatch ?: palette.swatches.firstOrNull()
                    } else {
                        palette.lightMutedSwatch ?: palette.lightVibrantSwatch ?: palette.darkMutedSwatch ?: palette.swatches.firstOrNull()
                    }
                }
                dominantColor = if (swatch != null) {
                    Color(swatch.rgb)
                } else {
                    defaultColor
                }
            }
            playerBackGroundColor = dominantColor
        }
    }

    fun handleSongChangeFromPager(position:Int){
        Log.d("PAGER", "handleSongChangeFromPager:$position , ${appPreferences.currentPlayable?.currentSongIndex} ")
        if(position > (appPreferences.currentPlayable?.currentSongIndex ?: 0)){
            skipToNextSong()
        }
        else if(position < (appPreferences.currentPlayable?.currentSongIndex ?: 0)){
            skipToPreviousSong()
        }
    }

    fun skipToNextSong() {
        brainzPlayerServiceConnection.transportControls.skipToNext()
        // Updating currently playing song.
        appPreferences.currentPlayable = appPreferences.currentPlayable
            ?.copy(currentSongIndex = ( appPreferences.currentPlayable?.currentSongIndex!! + 1)   // Since BP won't be visible to users with no songs, we don't need to worry.
                .coerceAtMost(appPreferences.currentPlayable?.songs!!.size)
            )
    }

    fun skipToPreviousSong() {
        brainzPlayerServiceConnection.transportControls.seekTo(0) // Always reset to the start since the song won't change if playing time exceeds a certain threshold.
        brainzPlayerServiceConnection.transportControls.skipToPrevious()
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

    fun updateSearchQuery(newQuery: TextFieldValue) {
        _searchQuery.value = newQuery
    }

    private fun SearchSong(query: String) {
        val listToSearch = _mediaItems.value.data
        if (query.isEmpty()) {
            _searchItems.value = emptyList()
            return
        }
        val result: List<Song>? = listToSearch?.filter {
            it.title.contains(query.trim(), ignoreCase = true)
        }
        _searchItems.value = result ?: emptyList()
    }

    fun clearSearchResults() {
        _searchItems.value = emptyList()
        _searchQuery.value = TextFieldValue("")
    }


    fun playOrToggleSong(mediaItem: Song, toggle: Boolean = false) {
        val isPrepared = playbackState.value.isPrepared
        if (isPrepared && mediaItem.mediaID == currentlyPlayingSong.value.toSong.mediaID) {
            playbackState.value.let { playbackState ->
                when {
                    playbackState.isPlaying -> if (toggle) brainzPlayerServiceConnection.transportControls.pause()
                    playbackState.isPlayEnabled -> {
                        mediaItem.lastListenedTo = System.currentTimeMillis()
                        viewModelScope.launch { songRepository.updateSong(mediaItem) }
                        brainzPlayerServiceConnection.transportControls.play()
                    }
                    else -> Unit
                }
            }
        } else {
            mediaItem.lastListenedTo = System.currentTimeMillis()
            viewModelScope.launch { songRepository.updateSong(mediaItem) }
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