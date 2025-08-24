package org.listenbrainz.android.repository.appupdates

import android.content.Context
import android.net.Uri
import org.listenbrainz.android.model.InstallSource
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
}
