package org.listenbrainz.android.ui.screens.profile.listens

import org.listenbrainz.shared.model.Listen
import org.listenbrainz.shared.model.ListenBitmap
import org.listenbrainz.shared.model.ResponseError
import org.listenbrainz.shared.model.playback.SharedPlayerState

data class ListensUiState(
    val listens: List<Listen> = emptyList(),
    val listeningNowUiState: ListeningNowUiState = ListeningNowUiState(),
    val isLoading: Boolean = true,
    val error: ResponseError? = null
)

data class ListeningNowUiState(
    val listeningNow: Listen? = null,
    val listeningNowBitmap: ListenBitmap = ListenBitmap(),
    val playerState: SharedPlayerState? = null,
    val songDuration: Long = 0L,
    val songCurrentPosition: Long = 0L,
    val progress: Float = 0f
)