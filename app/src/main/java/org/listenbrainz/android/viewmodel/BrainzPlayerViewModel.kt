package org.listenbrainz.android.viewmodel

import android.content.Context
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
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.listenbrainz.shared.model.Playable
import org.listenbrainz.shared.model.Playable.Companion.EMPTY_PLAYABLE
import org.listenbrainz.shared.model.PlayableType
import org.listenbrainz.android.model.Playlist.Companion.currentlyPlaying
import org.listenbrainz.android.model.RepeatMode
import org.listenbrainz.shared.model.Song
import org.listenbrainz.android.repository.brainzplayer.BPAlbumRepository
import org.listenbrainz.android.repository.brainzplayer.PlaylistRepository
import org.listenbrainz.android.repository.brainzplayer.SongRepository
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.android.service.BrainzPlayerService
import org.listenbrainz.android.service.BrainzPlayerServiceConnection
import org.listenbrainz.android.util.BrainzPlayerExtensions.currentPlaybackPosition
import org.listenbrainz.android.util.BrainzPlayerExtensions.isPlayEnabled
import org.listenbrainz.android.util.BrainzPlayerExtensions.isPlaying
import org.listenbrainz.android.util.BrainzPlayerExtensions.isPrepared
import org.listenbrainz.android.util.BrainzPlayerExtensions.toSong
import org.listenbrainz.android.util.BrainzPlayerUtils.MEDIA_ROOT_ID
import org.listenbrainz.android.util.Resource

class BrainzPlayerViewModel(
    private val brainzPlayerServiceConnection: BrainzPlayerServiceConnection,
    private val songRepository: SongRepository,
    val appPreferences: AppPreferences,
    private val playlistRepository: PlaylistRepository,
    private val albumRepository: BPAlbumRepository,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    private val _mediaItems = MutableStateFlow<Resource<List<Song>>>(Resource.loading())
    private val _songDuration = MutableStateFlow(0L)
    private val _songCurrentPosition = MutableStateFlow(0L)
    private val _progress = MutableStateFlow(0F)
    val mediaItems = _mediaItems.asStateFlow()
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
    val currentPlayable = appPreferences.currentPlayable.getFlow()
        .map { it ?: EMPTY_PLAYABLE }
        .stateIn(viewModelScope, SharingStarted.Eagerly, EMPTY_PLAYABLE)

    var playerBackGroundColor by mutableStateOf(Color.Transparent)
        private set

    private val _searchQuery = MutableStateFlow(TextFieldValue(""))
    val searchQuery: StateFlow<TextFieldValue> = _searchQuery

    private val _searchItems = MutableStateFlow<List<Song>>(emptyList())
    val searchItems: StateFlow<List<Song>> = _searchItems

    init {
        if (!isPlaying.value) {
            setCurrentPlayable(EMPTY_PLAYABLE)
        }

        updatePlayerPosition()
        _mediaItems.value = Resource.loading()
        brainzPlayerServiceConnection.subscribe(
            MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {
                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>
                ) {
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
            if (result is SuccessResult) {
                val bitmap = result.image.toBitmap()
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
        Log.d("PAGER", "handleSongChangeFromPager:$position , ${currentPlayable.value.currentSongIndex} ")
        if(position > currentPlayable.value.currentSongIndex){
            skipToNextSong()
        }
        else if(position < currentPlayable.value.currentSongIndex){
            skipToPreviousSong()
        }
    }

    fun skipToNextSong() {
        brainzPlayerServiceConnection.transportControls?.skipToNext() ?: return
        val playable = currentPlayable.value
        setCurrentPlayable(
            playable.copy(
                currentSongIndex = (playable.currentSongIndex + 1)
                    .coerceAtMost(playable.songs.size)
            )
        )
    }

    fun skipToPreviousSong() {
        brainzPlayerServiceConnection.transportControls?.seekTo(0) ?: return // Always reset to the start since the song won't change if playing time exceeds a certain threshold.
        brainzPlayerServiceConnection.transportControls?.skipToPrevious() ?: return
        val playable = currentPlayable.value
        setCurrentPlayable(
            playable.copy(
                currentSongIndex = (playable.currentSongIndex - 1).coerceAtLeast(0)
            )
        )
    }


    fun onSeek(seekTo: Float) {
        viewModelScope.launch { _progress.emit(seekTo) }
    }

    fun onSeeked() {
        brainzPlayerServiceConnection.transportControls?.seekTo((_songDuration.value * progress.value).toLong())
    }

    fun shuffle() {
        val transportControls = brainzPlayerServiceConnection.transportControls
        transportControls?.setShuffleMode(if (isShuffled.value) SHUFFLE_MODE_NONE else SHUFFLE_MODE_ALL)
    }

    fun repeatMode() {
        when (repeatMode.value) {
            RepeatMode.REPEAT_MODE_OFF -> brainzPlayerServiceConnection.transportControls?.setRepeatMode(
                REPEAT_MODE_ONE
            )
            RepeatMode.REPEAT_MODE_ALL -> brainzPlayerServiceConnection.transportControls?.setRepeatMode(
                REPEAT_MODE_NONE
            )
            RepeatMode.REPEAT_MODE_ONE -> brainzPlayerServiceConnection.transportControls?.setRepeatMode(
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

        if (brainzPlayerServiceConnection.transportControls == null) return

        val isPrepared = playbackState.value.isPrepared
        if (isPrepared && mediaItem.mediaID == currentlyPlayingSong.value.toSong.mediaID) {
            playbackState.value.let { playbackState ->
                when {
                    playbackState.isPlaying -> if (toggle) brainzPlayerServiceConnection.transportControls?.pause() else {}
                    playbackState.isPlayEnabled -> {
                        mediaItem.lastListenedTo = System.currentTimeMillis()
                        viewModelScope.launch { songRepository.updateSong(mediaItem) }
                        brainzPlayerServiceConnection.transportControls?.play()
                    }
                    else -> Unit
                }
            }
        } else {
            mediaItem.lastListenedTo = System.currentTimeMillis()
            viewModelScope.launch { songRepository.updateSong(mediaItem) }
            brainzPlayerServiceConnection.transportControls?.playFromMediaId(mediaItem.mediaID.toString(), null)
        }
    }

    fun queueChanged(mediaItem: Song, toggle: Boolean ) {
        brainzPlayerServiceConnection.transportControls?.playFromMediaId(mediaItem.mediaID.toString(), null) ?: return
        playbackState.value.let { playbackState ->
            when {
                playbackState.isPlaying -> if (!toggle) brainzPlayerServiceConnection.transportControls?.pause() else {}
                playbackState.isPlayEnabled -> brainzPlayerServiceConnection.transportControls?.play()
                else -> Unit
            }
        }
    }

    fun changePlayable(newPlayableList: List<Song>, playableType: PlayableType, playableId: Long, currentIndex: Int, seekTo: Long = 0L ) {
        setCurrentPlayable(Playable(playableType, playableId, newPlayableList, currentIndex, seekTo))
    }
    
    /**Skip to the given song at given [index] in the current playlist.*/
    fun skipToPlayable(index: Int){
        val playable = currentPlayable.value
        setCurrentPlayable(playable.copy(currentSongIndex = index))
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

    private fun setCurrentPlayable(playable: Playable) {
        viewModelScope.launch(ioDispatcher) {
            appPreferences.currentPlayable.set(playable)
        }
    }

    fun playNext(songs: List<Song>) {
        if (songs.isEmpty()) return

        val playable = currentPlayable.value
        val currentSongIndex =
            playable.songs.indexOfFirst { song -> song.mediaID == currentlyPlayingSong.value.toSong.mediaID }
                .takeIf { it != -1 }
                ?.plus(1)
        if (isPlaying.value && currentSongIndex != null) {
            val currentSongs = playable.songs.toMutableList()
            currentSongs.addAll(currentSongIndex, songs)
            val updatedPlayable = playable.copy(
                songs = currentSongs
            )
            setCurrentPlayable(updatedPlayable)
            updatedPlayable.songs.let {
                changePlayable(
                    it,
                    PlayableType.ALL_SONGS,
                    updatedPlayable.id,
                    updatedPlayable.songs.indexOfFirst { song -> song.mediaID == currentlyPlayingSong.value.toSong.mediaID }
                        .coerceAtLeast(0),
                    songCurrentPosition.value
                )
            }
            queueChanged(
                currentlyPlayingSong.value.toSong,
                isPlaying.value
            )
        } else {
            // No song is playing, so start playing the selected song
            changePlayable(
                songs,
                PlayableType.SONG,
                songs.first().mediaID,
                0,
                0L
            )
            playOrToggleSong(songs.first(), true)
        }
    }

    fun enqueueNext(songs: List<Song>) {
        if (songs.isEmpty()) return

        val playable = currentPlayable.value
        val currentIndex = playable.songs.indexOfFirst {
            it.mediaID == currentlyPlayingSong.value.toSong.mediaID
        }
        val updatedSongs = playable.songs.toMutableList()
        if (currentIndex != -1) {
            updatedSongs.addAll(currentIndex + 1, songs)
        }
        val updatedPlayable = playable.copy(songs = updatedSongs)
        setCurrentPlayable(updatedPlayable)
        changePlayable(
            updatedSongs,
            PlayableType.ALL_SONGS,
            updatedPlayable.id,
            currentIndex.coerceAtLeast(0),
            songCurrentPosition.value
        )
        queueChanged(
            currentlyPlayingSong.value.toSong,
            isPlaying.value
        )
    }

    fun addToQueue(
        songs: List<Song>
    ) {
        val playable = currentPlayable.value
        val currentSongs = playable.songs.toMutableList()
        currentSongs.addAll(songs)
        val updatedPlayable = playable.copy(
            songs = currentSongs
        )
        setCurrentPlayable(updatedPlayable)
        var currentSongIndex = updatedPlayable.songs.indexOfFirst { song -> song.mediaID == currentlyPlayingSong.value.toSong.mediaID }
        if (currentSongIndex == -1) {
            currentSongIndex = 0
        }

        updatedPlayable.songs.let {
            changePlayable(
                it,
                PlayableType.ALL_SONGS,
                updatedPlayable.id,
                currentSongIndex,
                songCurrentPosition.value
            )
        }
        queueChanged(
            currentlyPlayingSong.value.toSong,
            isPlaying.value
        )
    }
}
