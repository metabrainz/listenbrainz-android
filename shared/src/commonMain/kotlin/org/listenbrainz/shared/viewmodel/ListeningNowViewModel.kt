package org.listenbrainz.shared.viewmodel

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.listenbrainz.shared.model.Listen
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.shared.repository.PlatformContext
import org.listenbrainz.shared.repository.listens.ListensRepository
import org.listenbrainz.shared.repository.socket.SocketRepository
import org.listenbrainz.shared.util.ImagePalette
import org.listenbrainz.shared.util.Log
import org.listenbrainz.shared.util.Utils.getCoverArtUrl
import org.listenbrainz.shared.util.fetchBitmapFromUrl
import org.listenbrainz.shared.util.getPaletteFromImage
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

data class ListeningNowUIState(
    val song: Listen? = null,
    val palette: ImagePalette? = null,
    val imageURL: String? = null
) {
    val isListeningNow: Boolean
        get() = song != null
}

class ListeningNowViewModel(
    private val socketRepository: SocketRepository,
    private val appPreferences: AppPreferences,
    private val listensRepository: ListensRepository,
    private val ioDispatcher: CoroutineDispatcher,
    private val logger: Log = Log
) : ViewModel() {
    private val _listeningNowUIState = MutableStateFlow(ListeningNowUIState())
    val listeningNowUIState = _listeningNowUIState.asStateFlow()
    var dismissJob: Job? = null

    init {
        viewModelScope.launch(ioDispatcher) {
            appPreferences.username.getFlow().collectLatest { username ->
                fetchListenFromAPI(username)
                logger.d("Socket listening", "Listening for $username")
                socketRepository
                    .listen { username }
                    .collectLatest { listen ->
                        updateUIState(listen)
                    }
            }
        }
    }

    private suspend fun fetchListenFromAPI(username: String) {
        val result = listensRepository.getNowPlaying(username)
        if (result.isSuccess) {
            val listen = result.data?.payload?.listens?.firstOrNull()
            if (listen == null) {
                _listeningNowUIState.update {
                    ListeningNowUIState()
                }
                logger.d(TAG, "fetchListenFromAPI: No listen found")
                return
            }
            logger.d(TAG, "fetchListenFromAPI: $listen")

            updateUIState(listen)
        } else if (result.isFailed) {
            logger.d(TAG, "fetchListenFromAPI: ${result.error?.toast}")
        }
    }

    private suspend fun updateUIState(listen: Listen?) {
        if (listen == null) {
            _listeningNowUIState.value = ListeningNowUIState()
            return
        }

        _listeningNowUIState.update {
            ListeningNowUIState(
                imageURL = getCoverArtUrl(
                    caaReleaseMbid = listen.trackMetadata?.mbidMapping?.caaReleaseMbid,
                    caaId = listen.trackMetadata?.mbidMapping?.caaId,
                    size = 500
                ),
                song = listen
            )
        }

        dismissJob?.cancelAndJoin()
        dismissJob = viewModelScope.launch {
            delay(listen.dismissDurationMs)
            if (_listeningNowUIState.value.song == listen) {
                _listeningNowUIState.value = ListeningNowUIState()
            }
        }
    }

    fun updatePalette(context: PlatformContext) {
        val url = listeningNowUIState.value.imageURL ?: return
        viewModelScope.launch {
            try {
                val bitmap = fetchBitmapFromUrl(context,url)
                if (bitmap != null) {
                    _listeningNowUIState.update {
                        it.copy(palette = getPaletteFromImage(bitmap))
                    }
                }
            } catch (e: Exception) {
                logger.d("ListeningNowLayout", "Error loading socket image palette: ${e.message}")
            }
        }
    }

    companion object {
        private const val TAG = "ListeningNowViewModel"

        /** Time left to dismiss this [Listen] if it was a listening now.*/
        val Listen.dismissDurationMs: Long
            get() {
                val listenDurationMs = trackMetadata
                    ?.additionalInfo
                    ?.durationMs
                    ?.toLong()
                    // Default to 6 minutes for now listening dismiss
                    ?: 6.minutes.inWholeMilliseconds

                val listenedAt = listenedAt
                val delayToDismiss = if (listenedAt != null) {
                    val durationCompleted = Clock.System.now().toEpochMilliseconds() - (listenedAt * 1000L)
                    (listenDurationMs - durationCompleted).coerceAtLeast(0)
                } else {
                    listenDurationMs
                }

                return delayToDismiss
            }
    }
}