package org.listenbrainz.android.ui.screens.playlist

import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.User
import org.listenbrainz.android.model.playlist.PlaylistData

data class PlaylistDataUIState(
    val isLoading: Boolean = false,
    val createEditScreenUIState: CreateEditScreenUIState = CreateEditScreenUIState(),
    val error: ResponseError? = null
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