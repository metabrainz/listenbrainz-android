package org.listenbrainz.android.ui.screens.playlist

import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.User
import org.listenbrainz.android.model.playlist.PlaylistData

data class PlaylistDataUIState(
    val isLoading: Boolean = false,
    val createEditScreenUIState: CreateEditScreenUIState = CreateEditScreenUIState(),
    val playlistDetailUIState: PlaylistDetailUIState = PlaylistDetailUIState(),
    val error: ResponseError? = null,
    val successMsg: Int? = null
)

data class CreateEditScreenUIState(
    val collaboratorQueryText: String = "",
    val isSaving: Boolean = false,
    val isSearching: Boolean = false,
    val isLoading: Boolean = true,
    val name: String = "",
    val playlistData: PlaylistData? = null,
    val description: String = "",
    val isPublic: Boolean = false,
    val collaboratorSelected: List<String> = emptyList(),
    val usersSearched: List<User> = emptyList(),
    val playlistMBID: String? = null,
    val emptyTitleFieldError: Boolean = false
)

data class PlaylistDetailUIState(
    val isLoading: Boolean = true,
    val playlistData: PlaylistData? = null,
    val isRefreshing: Boolean = false,
    val isCoverArtLoading: Boolean = false,
    val playlistMBID: String? = null,
    val isPlaylistEditable: Boolean = false,
    val error: ResponseError? = null
)