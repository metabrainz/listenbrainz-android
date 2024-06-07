package org.listenbrainz.android.ui.screens.profile

import org.listenbrainz.android.model.Listens
import org.listenbrainz.android.model.PinnedRecording
import org.listenbrainz.android.model.SimilarUser
import org.listenbrainz.android.ui.screens.listens.ListeningNowUiState

data class ProfileUiState(
    val listensTabUiState: ListensTabUiState? = null
)

data class ListensTabUiState (
    val listenCount : Int? = null,
    val followersCount : Int? = null,
    val followingCount : Int? = null,
    val listeningNow: ListeningNowUiState? = null,
    val pinnedSong : PinnedRecording? = null,
    val recentListens : List<Listens> = emptyList(),
    val followers : List<String>? = emptyList(),
    val following : List<String>? = emptyList(),
    val similarUsers : List<SimilarUser> = emptyList()
)
