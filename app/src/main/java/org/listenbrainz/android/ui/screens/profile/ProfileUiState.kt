package org.listenbrainz.android.ui.screens.profile

import com.spotify.protocol.types.PlayerState
import org.listenbrainz.android.model.Listen
import org.listenbrainz.android.model.ListenBitmap
import org.listenbrainz.android.model.PinnedRecording
import org.listenbrainz.android.model.SimilarUser
import org.listenbrainz.android.ui.screens.profile.listens.ListeningNowUiState

data class ProfileUiState(
    val listensTabUiState: ListensTabUiState = ListensTabUiState()
)

data class ListensTabUiState (
    val isLoading: Boolean = true,
    val isSelf: Boolean = false,
    val listenCount: Int? = null,
    val followersCount: Int? = null,
    val followingCount: Int? = null,
    val listeningNow: ListeningNowUiState? = null,
    val pinnedSong: PinnedRecording? = null,
    val compatibility: Float? = null,
    val recentListens: List<Listen>? = emptyList(),
    val followers: List<String>? = emptyList(),
    val following: List<String>? = emptyList(),
    val similarUsers: List<SimilarUser>? = emptyList(),
    val similarArtists: List<String> = emptyList()
)

data class ListeningNowUiState(
    val listeningNow: Listen? = null,
    val listeningNowBitmap: ListenBitmap = ListenBitmap(),
    val playerState: PlayerState? = null,
    val songDuration: Long = 0L,
    val songCurrentPosition: Long = 0L,
    val progress: Float = 0f
)
