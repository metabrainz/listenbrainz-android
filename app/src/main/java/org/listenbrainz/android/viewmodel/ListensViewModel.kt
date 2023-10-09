package org.listenbrainz.android.viewmodel

import androidx.lifecycle.viewModelScope
import com.spotify.protocol.types.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.listenbrainz.android.di.IoDispatcher
import org.listenbrainz.android.model.Listen
import org.listenbrainz.android.model.ListenBitmap
import org.listenbrainz.android.repository.listens.ListensRepository
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.remoteplayer.RemotePlaybackHandler
import org.listenbrainz.android.repository.social.SocialRepository
import org.listenbrainz.android.repository.socket.SocketRepository
import org.listenbrainz.android.ui.screens.listens.ListeningNowUiState
import org.listenbrainz.android.ui.screens.listens.ListensUiState
import org.listenbrainz.android.util.LinkedService
import org.listenbrainz.android.util.Resource.Status.FAILED
import org.listenbrainz.android.util.Resource.Status.SUCCESS
import javax.inject.Inject

@HiltViewModel
class ListensViewModel @Inject constructor(
    val repository: ListensRepository,
    val appPreferences: AppPreferences,
    socialRepository: SocialRepository,
    private val socketRepository: SocketRepository,
    private val remotePlaybackHandler: RemotePlaybackHandler,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : SocialViewModel<ListensUiState>(socialRepository, appPreferences, remotePlaybackHandler, ioDispatcher) {
    
    private val isSpotifyLinked = MutableStateFlow(appPreferences.linkedServices.contains(LinkedService.SPOTIFY))
    private val isLoading = MutableStateFlow(true)
    private val playerState = remotePlaybackHandler.getPlayerState().onEach { updateTrackCoverArt(it) }
    private val songDuration = MutableStateFlow(0L)
    private val songCurrentPosition = MutableStateFlow(0L)
    private val progress = MutableStateFlow(0F)
    private val listensFlow = MutableStateFlow(listOf<Listen>())
    private val listeningNowBitmap = MutableStateFlow(ListenBitmap())
    private val listeningNowFlow: MutableStateFlow<Listen?> = MutableStateFlow(null)
    
    override val uiState: StateFlow<ListensUiState> = createUiStateFlow()
    
    init {
        viewModelScope.launch(ioDispatcher) {
            socketRepository
                .listen(appPreferences.username!!)
                .collect { listen ->
                    if (listen.listenedAt == null)
                        listeningNowFlow.value = listen
                    else
                        listensFlow.getAndUpdate {
                            listOf(listen) + it
                        }
                }
        }
    }
    
    override fun createUiStateFlow(): StateFlow<ListensUiState> =
        combine(
            listensFlow,
            combine(
                listeningNowFlow,
                listeningNowBitmap,
                playerState,
                songDuration,
                songCurrentPosition,
                progress
            ) { array -> // listen, bitmap, playerState, songDuration, songCurrentPosition, progress ->
                ListeningNowUiState(
                    array[0] as Listen?,
                    array[1] as ListenBitmap,
                    array[2] as PlayerState?,
                    array[3] as Long,
                    array[4] as Long,
                    array[5] as Float
                )
            },
            isSpotifyLinked,
            isLoading,
            errorFlow,
        ){ listens, listeningNowState, isSpotifyLinked, isLoading, error->
            ListensUiState(listens, listeningNowState, isSpotifyLinked, isLoading, error)
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            ListensUiState()
        )

    suspend fun validateUserToken(token: String): Boolean? {
        return repository.validateUserToken(token).data?.valid
    }

    suspend fun retrieveUsername(token: String): String? {
        return repository.validateUserToken(token).data?.user_name
    }
    
    fun fetchLinkedServices() {
        viewModelScope.launch {
            val result = repository.getLinkedServices(token = appPreferences.getLbAccessToken(), username = appPreferences.username)
            if (result.status.isSuccessful()) {
                result.data!!.toLinkedServicesList().also { services ->
                    isSpotifyLinked.emit(services.contains(LinkedService.SPOTIFY))
                    appPreferences.linkedServices = services
                }
            }
        }
    }

    fun fetchUserListens(userName: String) {
        viewModelScope.launch {
            val response = repository.fetchUserListens(userName)
            isLoading.emit(
                when(response.status){
                    SUCCESS -> {
                        // Updating listens
                        listensFlow.update { response.data?.payload?.listens ?: emptyList() }
                        false
                    }
                    FAILED -> false
                    else -> throw IllegalStateException()
                }
            )
        }
    }
    
    private suspend fun updateTrackCoverArt(playerState: PlayerState?) = withContext(ioDispatcher) {
        // Get image from track
        listeningNowBitmap.emit(
            remotePlaybackHandler.fetchSpotifyTrackCoverArt(playerState)
        )
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
