package org.listenbrainz.android.viewmodel

import android.net.Uri
import org.listenbrainz.android.model.githubupdates.GithubUpdatesListItem

data class AppUpdatesUiState(
    //Github release fields
    val latestStableRelease: GithubUpdatesListItem? = null,
    val latestRelease: GithubUpdatesListItem? = null,
    val isUpdateAvailable: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isInstallPermissionGranted: Boolean = false,
    val isInstallPermissionRationaleVisible: Boolean = false,
    val isWaitingForPermissionToUpdateApp: Boolean = false,
    val downloadedApkUri: Uri? = null,
    val isInstallAppDialogVisible: Boolean = false,
    // Play Store update fields
    val isPlayStoreUpdateAvailable: Boolean = false,
    val isPlayStoreUpdateDownloading: Boolean = false,
    val playStoreUpdateDownloadProgress: Int = 0,
    val isPlayStoreUpdateReadyToInstall: Boolean = false,
    val playStoreUpdateError: String? = null,
    val isPlayStoreFlexibleUpdateVisible: Boolean = false
)
