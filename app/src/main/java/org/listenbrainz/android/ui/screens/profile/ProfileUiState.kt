package org.listenbrainz.android.ui.screens.profile

import com.spotify.protocol.types.PlayerState
import org.listenbrainz.android.model.Listen
import org.listenbrainz.android.model.ListenBitmap
import org.listenbrainz.android.model.PinnedRecording
import org.listenbrainz.android.model.SimilarUser
import org.listenbrainz.android.model.user.AllPinnedRecordings
import org.listenbrainz.android.model.user.ListeningActivity
import org.listenbrainz.android.model.user.UserFeedback
import org.listenbrainz.android.ui.screens.profile.listens.ListeningNowUiState
import org.listenbrainz.android.ui.screens.profile.stats.StatsRange
import org.listenbrainz.android.ui.screens.profile.stats.UserGlobal

data class ProfileUiState(
    val isSelf: Boolean = false,
    val listensTabUiState: ListensTabUiState = ListensTabUiState(),
    val statsTabUIState: StatsTabUIState = StatsTabUIState(),
    val tasteTabUIState: TasteTabUIState = TasteTabUIState(),
)

data class ListensTabUiState (
    val isLoading: Boolean = true,
    val listenCount: Int? = null,
    val followersCount: Int? = null,
    val followingCount: Int? = null,
    val listeningNow: ListeningNowUiState? = null,
    val pinnedSong: PinnedRecording? = null,
    val compatibility: Float? = null,
    val recentListens: List<Listen>? = emptyList(),
    val followers: List<Pair<String,Boolean>>? = emptyList(),
    val following: List<Pair<String,Boolean>>? = emptyList(),
    val similarUsers: List<SimilarUser>? = emptyList(),
    val similarArtists: List<String> = emptyList(),
    val isFollowing: Boolean = false
)

data class TasteTabUIState (
    val isLoading: Boolean = true,
    val lovedSongs: UserFeedback? = null,
    val hatedSongs: UserFeedback? = null,
    val pins: AllPinnedRecordings? = null,
    )

data class StatsTabUIState(
    val isLoading: Boolean = true,
    val userListeningActivity: Map<Pair<UserGlobal, StatsRange>, List<ListeningActivity?>> = mapOf(),
    val sortedListeningActivity: List<ListeningActivity?>? = listOf()
)

data class ListeningNowUiState(
    val listeningNow: Listen? = null,
    val listeningNowBitmap: ListenBitmap = ListenBitmap(),
    val playerState: PlayerState? = null,
    val songDuration: Long = 0L,
    val songCurrentPosition: Long = 0L,
    val progress: Float = 0f
)
