package org.listenbrainz.android.viewmodel

import android.graphics.drawable.Drawable
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.listenbrainz.android.di.IoDispatcher
import org.listenbrainz.android.model.Listen
import org.listenbrainz.android.model.ListenBitmap
import org.listenbrainz.android.model.UiMode
import org.listenbrainz.android.repository.listens.ListensRepository
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.remoteplayer.RemotePlaybackHandler
import org.listenbrainz.android.repository.socket.SocketRepository
import org.listenbrainz.android.ui.screens.listens.ListeningNowUiState
import org.listenbrainz.android.ui.screens.listens.ListensUiState
import org.listenbrainz.android.ui.screens.settings.PreferencesUiState
import org.listenbrainz.android.util.LinkedService
import org.listenbrainz.android.util.Resource.Status.FAILED
import org.listenbrainz.android.util.Resource.Status.SUCCESS
import javax.inject.Inject

@HiltViewModel
class ListensViewModel @Inject constructor(
    private val repository: ListensRepository,
    private val appPreferences: AppPreferences,
    private val socketRepository: SocketRepository,
    private val remotePlaybackHandler: RemotePlaybackHandler,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : BaseViewModel<ListensUiState>() {
    
    private val isSpotifyLinked = MutableStateFlow(appPreferences.linkedServices.contains(LinkedService.SPOTIFY))
    private val isNotificationServiceAllowed = MutableStateFlow(appPreferences.isNotificationServiceAllowed)
    private val isLoading = MutableStateFlow(true)
    private val playerState = remotePlaybackHandler.getPlayerState().onEach { updateTrackCoverArt(it) }
    private val songDuration = MutableStateFlow(0L)
    private val songCurrentPosition = MutableStateFlow(0L)
    private val progress = MutableStateFlow(0F)
    private val listensFlow = MutableStateFlow(listOf<Listen>())
    private val listeningNowBitmap = MutableStateFlow(ListenBitmap())
    private val listeningNowFlow: MutableStateFlow<Listen?> = MutableStateFlow(null)
    
    override val uiState: StateFlow<ListensUiState> = createUiStateFlow()
    val preferencesUiState: StateFlow<PreferencesUiState> = createPreferencesUiStateFlow()
    
    init {
        viewModelScope.launch {
            val username = withContext(ioDispatcher) { appPreferences.username.get() }
            if (username.isEmpty()) return@launch
            
            launch { fetchUserListens(username = username) }
            
            withContext(ioDispatcher) {
                socketRepository
                    .listen(username)
                    .collect { listen ->
                        if (listen.listenedAt == null)
                            listeningNowFlow.emit(listen)
                        else
                            listensFlow.getAndUpdate {
                                listOf(listen) + it
                            }
                    }
            }
        }
    }
    
    override fun createUiStateFlow(): StateFlow<ListensUiState> =
        combine(
            listensFlow,
            createListeningNowUiStateFlow(),
            isLoading,
            errorFlow,
        ){ listens, listeningNowState, isLoading, error ->
            ListensUiState(listens, listeningNowState, isLoading, error)
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            ListensUiState()
        )
    
    private fun createListeningNowUiStateFlow(): StateFlow<ListeningNowUiState> =
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
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            ListeningNowUiState()
        )
    
    @Suppress("UNCHECKED_CAST")
    private fun createPreferencesUiStateFlow(): StateFlow<PreferencesUiState> =
        combine(
            isSpotifyLinked,
            appPreferences.username.getFlow(),
            appPreferences.lbAccessToken.getFlow(),
            isNotificationServiceAllowed,
            appPreferences.listeningWhitelist.getFlow(),
            appPreferences.listeningApps.getFlow(),
            appPreferences.themePreference.getFlow()
        ) { array ->
            PreferencesUiState(
                array[0] as Boolean,
                array[1] as String,
                array[2] as String,
                array[3] as Boolean,
                array[4] as List<String>,
                array[5] as List<String>,
                array[6] as UiMode,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            PreferencesUiState()
        )
    
    fun getPackageIcon(packageName: String): Drawable? = repository.getPackageIcon(packageName)
    fun getPackageLabel(packageName: String): String = repository.getPackageLabel(packageName)
    
    fun setWhitelist(list: List<String>) {
        viewModelScope.launch {
            appPreferences.listeningWhitelist.set(list)
        }
    }
    
    suspend fun validateUserToken(token: String): Boolean {
        return repository.validateToken(token).data?.valid ?: false
    }

    fun setAccessToken(token:String) {
        viewModelScope.launch {
            appPreferences.lbAccessToken.set(token)
        }
    }
    
    suspend fun saveUserDetails(token: String) {
        val result = repository.validateToken(token)
        if (result.status.isSuccessful() && result.data?.valid == true) {
            appPreferences.username.set(result.data.username ?: "")
            appPreferences.lbAccessToken.set(token)
        } else {
            errorFlow.emit(result.error)
        }
    }
    
    fun fetchLinkedServices() {
        viewModelScope.launch {
            val result = withContext(ioDispatcher) {
                repository.getLinkedServices(
                    token = appPreferences.lbAccessToken.get(),
                    username = appPreferences.username.get()
                )
            }
            if (result.status.isSuccessful()) {
                result.data!!.toLinkedServicesList().also { services ->
                    isSpotifyLinked.emit(services.contains(LinkedService.SPOTIFY))
                    appPreferences.linkedServices = services
                }
            }
        }
    }
    
    fun updateNotificationServicePermissionStatus() {
        viewModelScope.launch {
            isNotificationServiceAllowed.emit(
                withContext(ioDispatcher) {
                    appPreferences.isNotificationServiceAllowed
                }
            )
        }
    }

    private suspend fun fetchUserListens(username: String?) {
        val response = withContext(ioDispatcher) { repository.fetchUserListens(username) }
        when(response.status){
            SUCCESS -> {
                // Updating listens
                listensFlow.emit(response.data?.payload?.listens ?: emptyList())
            }
            FAILED -> {
                errorFlow.emit(response.error)
            }
            else -> throw IllegalStateException()
        }
        isLoading.emit(false)
    }
    
    suspend fun isNotificationServiceAllowed(): Boolean {
        return withContext(ioDispatcher) {
            appPreferences.isNotificationServiceAllowed
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
