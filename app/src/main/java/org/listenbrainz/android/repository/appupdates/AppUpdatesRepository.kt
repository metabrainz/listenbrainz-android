package org.listenbrainz.android.repository.appupdates

import android.content.Context
import android.net.Uri
import androidx.activity.ComponentActivity
import org.listenbrainz.shared.model.InstallSource
import org.listenbrainz.android.model.githubupdates.GithubUpdatesList
import org.listenbrainz.android.model.githubupdates.GithubUpdatesListItem
import org.listenbrainz.android.util.Resource

interface AppUpdatesRepository {

    suspend fun getAppReleasesFromGithub(): Resource<GithubUpdatesList>

    fun detectInstallSource(context: Context): InstallSource

    fun downloadGithubUpdate(
        release: GithubUpdatesListItem,
        onCompletedDownload: (Uri?) -> Unit,
        onDownloadError: (String) -> Unit
    ): Long?

    fun queryDownloadStatus(
        downloadId: Long,
        onCompletedDownload: (Uri?) -> Unit,
        onDownloadError: (String) -> Unit,
        onDownloadRunning: () -> Unit = {}
    )

    fun registerDownloadBroadcastReceiver(
        downloadId: Long,
        onCompletedDownload: (Uri?) -> Unit,
        onDownloadError: (String) -> Unit
    )

    suspend fun checkPlayStoreUpdate(activity: ComponentActivity): Boolean

    suspend fun startPlayStoreFlexibleUpdate(
        activity: ComponentActivity,
        onUpdateProgress: (Int) -> Unit,
        onUpdateDownloaded: () -> Unit,
        onUpdateError: (String) -> Unit
    ): Boolean

    suspend fun completePlayStoreFlexibleUpdate(activity: ComponentActivity): Boolean
}