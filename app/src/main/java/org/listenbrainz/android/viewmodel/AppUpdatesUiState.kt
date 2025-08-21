package org.listenbrainz.android.viewmodel

import org.listenbrainz.android.model.githubupdates.GithubUpdatesListItem

data class AppUpdatesUiState(
    val latestStableRelease: GithubUpdatesListItem? = null,
    val latestRelease: GithubUpdatesListItem? = null,
    val isUpdateAvailable: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
