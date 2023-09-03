package org.listenbrainz.android.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spotify.protocol.types.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.listenbrainz.android.di.DefaultDispatcher
import org.listenbrainz.android.di.IoDispatcher
import org.listenbrainz.android.model.Listen
import org.listenbrainz.android.model.ListenBitmap
import org.listenbrainz.android.repository.listens.ListensRepository
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.remoteplayer.RemotePlaybackHandler
import org.listenbrainz.android.repository.socket.SocketRepository
import org.listenbrainz.android.util.LinkedService
import org.listenbrainz.android.util.Resource.Status.FAILED
import org.listenbrainz.android.util.Resource.Status.LOADING
import org.listenbrainz.android.util.Resource.Status.SUCCESS
import javax.inject.Inject

@HiltViewModel
class ListensViewModel @Inject constructor(
    val repository: ListensRepository,
    val appPreferences: AppPreferences,
    private val application: Application,
    private val socketRepository: SocketRepository,
    private val remotePlayerRepository: RemotePlaybackHandler,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : AndroidViewModel(application) {
    // TODO: remove dependency of this view-model on application
    //  by moving spotify app remote to a repository.
    
    private val _isSpotifyLinked = MutableStateFlow(appPreferences.linkedServices.contains(LinkedService.SPOTIFY))
    val isSpotifyLinked = _isSpotifyLinked.asStateFlow()
    
    var isLoading: Boolean by mutableStateOf(true)
    
    val playerState = remotePlayerRepository.getPlayerState().onEach { updateTrackCoverArt(it) }
    
    private val _songDuration = MutableStateFlow(0L)
    private val _songCurrentPosition = MutableStateFlow(0L)
    private val _progress = MutableStateFlow(0F)
    var bitmap: ListenBitmap = ListenBitmap()
    val progress = _progress.asStateFlow()
    val songCurrentPosition = _songCurrentPosition.asStateFlow()
    
    // Listens list flow
    private val _listensFlow = MutableStateFlow(listOf<Listen>())
    val listensFlow = _listensFlow.asStateFlow()
    
    // Listening now flow
    private val _listeningNowFlow: MutableStateFlow<Listen?> = MutableStateFlow(null)
    val listeningNow = _listeningNowFlow.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            socketRepository
                .listen(appPreferences.username!!)
                .collect { listen ->
                    if (listen.listenedAt == null)
                        _listeningNowFlow.value = listen
                    else
                        _listensFlow.getAndUpdate {
                            listOf(listen) + it
                        }
                }
        }
        
    }

    suspend fun validateUserToken(token: String): Boolean? {
        return repository.validateUserToken(token).data?.valid
    }

    suspend fun retrieveUsername(token: String): String? {
        return repository.validateUserToken(token).data?.user_name
    }
    
    fun fetchLinkedServices() {
        viewModelScope.launch {
            val token = appPreferences.getLbAccessToken()
            val userName = appPreferences.username
            if (token.isNotEmpty() && !userName.isNullOrEmpty()){
                val result = repository.getLinkedServices(token = token, username = userName)
                _isSpotifyLinked.emit(result.contains(LinkedService.SPOTIFY))
                appPreferences.linkedServices = result
            }
        }
    }

    fun fetchUserListens(userName: String) {
        viewModelScope.launch {
            val response = repository.fetchUserListens(userName)
            isLoading = when(response.status){
                SUCCESS -> {
                    // Updating listens
                    _listensFlow.update { response.data ?: emptyList() }
                    false
                }
                LOADING -> true
                FAILED -> false
            }
        }
    }
    
    private suspend fun updateTrackCoverArt(playerState: PlayerState?) = withContext(ioDispatcher) {
        // Get image from track
        bitmap = remotePlayerRepository.fetchSpotifyTrackCoverArt(playerState)
    }
    
    fun playListen(listen: Listen) {
        val spotifyId = listen.trackMetadata.additionalInfo?.spotifyId
        if (spotifyId != null){
            Uri.parse(spotifyId).lastPathSegment?.let { trackId ->
                remotePlayerRepository.playUri(
                    trackId = trackId,
                    onFailure = { playFromYoutubeMusic(listen) }
                )
            }
        } else {
            playFromYoutubeMusic(listen)
        }
    }
    
    private fun playFromYoutubeMusic(listen: Listen) {
        viewModelScope.launch {
            remotePlayerRepository.apply {
                playOnYoutube {
                    withContext(ioDispatcher) {
                        searchYoutubeMusicVideoId(
                            listen.trackMetadata.trackName,
                            listen.trackMetadata.artistName
                        )
                    }
                }
            }
        }
    }
    
    fun play(){
        remotePlayerRepository.play()
    }

    fun pause(){
        remotePlayerRepository.pause()
    }
    
    /*fun trackProgress() {
        var state: PlayerState?
        assertAppRemoteConnected()?.playerApi?.subscribeToPlayerState()?.setEventCallback { playerState ->
            if(bitmap.id != playerState.track.uri) {
                updateTrackCoverArt(playerState)
                state=playerState
            }
        }?.setErrorCallback(errorCallback)
        viewModelScope.launch(Dispatchers.Default) {
            do {
                // FIXME: Called even if spotify isn't there which leads to infinite logging.
                state = assertAppRemoteConnected()?.playerApi?.playerState?.await()?.data
                val pos = state?.playbackPosition?.toFloat() ?: 0f
                val duration=state?.track?.duration ?: 1
                if (progress.value != pos) {
                    _progress.emit(pos / duration.toFloat())
                    _songDuration.emit(duration)
                    _songCurrentPosition.emit(((pos / duration) * duration).toLong())
                }
                delay(900L)
            }while (!isPaused)
        }
    }*/

    /*fun seekTo(pos:Float,state: PlayerState?){
        val duration=state?.track?.duration ?: 1
        val position=(pos*duration).toLong()
        assertAppRemoteConnected()?.playerApi?.seekTo(position)?.setResultCallback {
            logMessage("seek command successful!")      //getString(R.string.command_feedback, "play"))
        }?.setErrorCallback(errorCallback)
        viewModelScope.launch(Dispatchers.Default) {
            if (progress.value != pos) {
                _progress.emit(pos / duration.toFloat())
                _songDuration.emit(duration ?: 0)
                _songCurrentPosition.emit(((pos / duration) * duration).toLong())
            }
        }
    }*/
}
