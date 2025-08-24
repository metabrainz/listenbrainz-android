package org.listenbrainz.android.viewmodel

import android.net.Uri
import org.listenbrainz.android.model.githubupdates.GithubUpdatesListItem

data class AppUpdatesUiState(
    val latestStableRelease: GithubUpdatesListItem? = null,
    val latestRelease: GithubUpdatesListItem? = null,
    val isUpdateAvailable: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isInstallPermissionGranted: Boolean = false,
    val isInstallPermissionRationaleVisible: Boolean = false,
    val isWaitingForPermissionToUpdateApp: Boolean = false,
    val downloadedApkUri: Uri? = null,
    val isInstallAppDialogVisible: Boolean = false
)
