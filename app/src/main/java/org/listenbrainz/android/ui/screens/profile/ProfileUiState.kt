package org.listenbrainz.android.ui.screens.profile

import androidx.paging.PagingData
import com.spotify.protocol.types.PlayerState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.listenbrainz.android.model.Listen
import org.listenbrainz.android.model.ListenBitmap
import org.listenbrainz.android.model.PinnedRecording
import org.listenbrainz.android.model.SimilarUser
import org.listenbrainz.android.model.playlist.PlaylistData
import org.listenbrainz.android.model.user.AllPinnedRecordings
import org.listenbrainz.android.model.user.Artist
import org.listenbrainz.android.model.user.ListeningActivity
import org.listenbrainz.android.model.user.TopAlbums
import org.listenbrainz.android.model.user.TopArtists
import org.listenbrainz.android.model.user.TopSongs
import org.listenbrainz.android.model.user.UserFeedback
import org.listenbrainz.android.model.userPlaylist.UserPlaylist
import org.listenbrainz.android.model.userPlaylist.UserPlaylists
import org.listenbrainz.android.ui.screens.profile.listens.ListeningNowUiState
import org.listenbrainz.android.ui.screens.profile.stats.StatsRange
import org.listenbrainz.android.ui.screens.profile.stats.DataScope

data class ProfileUiState(
    val isSelf: Boolean = false,
    val listensTabUiState: ListensTabUiState = ListensTabUiState(),
    val statsTabUIState: StatsTabUIState = StatsTabUIState(),
    val tasteTabUIState: TasteTabUIState = TasteTabUIState(),
    val createdForTabUIState: CreatedForTabUIState = CreatedForTabUIState(),
    val playlistTabUIState: PlaylistTabUIState = PlaylistTabUIState()
)

data class ListensTabUiState(
    val isLoading: Boolean = true,
    val listenCount: Int? = null,
    val followersCount: Int? = null,
    val followingCount: Int? = null,
    val listeningNow: ListeningNowUiState? = null,
    val pinnedSong: PinnedRecording? = null,
    val compatibility: Float? = null,
    val recentListens: List<Listen>? = emptyList(),
    val followers: List<Pair<String, Boolean>>? = emptyList(),
    val following: List<Pair<String, Boolean>>? = emptyList(),
    val similarUsers: List<SimilarUser>? = emptyList(),
    val similarArtists: List<Artist> = emptyList(),
    val isFollowing: Boolean = false
)

data class TasteTabUIState(
    val isLoading: Boolean = true,
    val lovedSongs: UserFeedback? = null,
    val hatedSongs: UserFeedback? = null,
    val pins: AllPinnedRecordings? = null,
)

data class StatsTabUIState(
    val isLoading: Boolean = true,
    val userListeningActivity: Map<Pair<DataScope, StatsRange>, List<ListeningActivity?>> = mapOf(),
    val topArtists: Map<StatsRange, TopArtists?>? = null,
    val topAlbums: Map<StatsRange, TopAlbums?>? = null,
    val topSongs: Map<StatsRange, TopSongs?>? = null,
)

data class CreatedForTabUIState(
    val isLoading: Boolean = true,
    val createdForYouPlaylists: List<UserPlaylists>? = emptyList(),
    val createdForYouPlaylistData: Map<String, PlaylistData?>? = null
)

data class PlaylistTabUIState(
    val isLoading: Boolean = true,
    val userPlaylists: Flow<PagingData<UserPlaylist>> = emptyFlow(),
    val collabPlaylists: Flow<PagingData<UserPlaylist>> = emptyFlow(),
    val playlistData: Map<String, PlaylistData?>? = null,
)

data class ListeningNowUiState(
    val listeningNow: Listen? = null,
    val listeningNowBitmap: ListenBitmap = ListenBitmap(),
    val playerState: PlayerState? = null,
    val songDuration: Long = 0L,
    val songCurrentPosition: Long = 0L,
    val progress: Float = 0f
)
