package org.listenbrainz.android.ui.screens.listens

import com.spotify.protocol.types.PlayerState
import org.listenbrainz.android.model.Listen
import org.listenbrainz.android.model.ListenBitmap
import org.listenbrainz.android.model.ResponseError

data class ListensUiState(
    val listens: List<Listen> = emptyList(),
    val listeningNowUiState: ListeningNowUiState = ListeningNowUiState(),
    val isLoading: Boolean = true,
    val error: ResponseError? = null
)

data class ListeningNowUiState(
    val listeningNow: Listen? = null,
    val listeningNowBitmap: ListenBitmap = ListenBitmap(),
    val playerState: PlayerState? = null,
    val songDuration: Long = 0L,
    val songCurrentPosition: Long = 0L,
    val progress: Float = 0f
)