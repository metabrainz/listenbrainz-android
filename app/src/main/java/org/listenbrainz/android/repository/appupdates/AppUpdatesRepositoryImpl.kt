package org.listenbrainz.android.repository.appupdates

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.listenbrainz.android.di.IoDispatcher
import org.listenbrainz.android.model.InstallSource
import org.listenbrainz.android.model.githubupdates.GithubUpdatesList
import org.listenbrainz.android.model.githubupdates.GithubUpdatesListItem
import org.listenbrainz.android.service.GithubAppUpdatesService
import org.listenbrainz.android.service.GithubUpdatesDownloadService
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils.parseResponse
import javax.inject.Inject

const val TAG = "AppUpdatesRepository"

class AppUpdatesRepositoryImpl @Inject constructor(
    private val githubAppUpdatesService: GithubAppUpdatesService,
    private val downloadService: GithubUpdatesDownloadService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AppUpdatesRepository {

    override suspend fun getAppReleasesFromGithub(): Resource<GithubUpdatesList> =
        withContext(ioDispatcher) {
            parseResponse {
                githubAppUpdatesService.getAppReleases()
            }
        }

    override fun detectInstallSource(context: Context): InstallSource {
        val packageName = context.packageName
        val packageManager = context.packageManager

        return try {
            val installerPackageName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val installSourceInfo = packageManager.getInstallSourceInfo(packageName)
                installSourceInfo.installingPackageName
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstallerPackageName(packageName)
            }

            if (installerPackageName == "com.android.vending" ||
                installerPackageName == "com.google.android.feedback"
            ) {
                Log.d(TAG, "App installed from Google Play Store")
                InstallSource.PLAY_STORE
            } else {
                //Mostly will get null as the package name while debugging
                Log.d(
                    TAG,
                    "App not installed from Google Play Store: $installerPackageName"
                )
                InstallSource.NOT_PLAY_STORE
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting install source", e)
            InstallSource.NOT_PLAY_STORE
        }
    }

    override fun downloadGithubUpdate(release: GithubUpdatesListItem,
                                      onCompletedDownload: (Uri?) -> Unit,
                                      onDownloadError: (String) -> Unit): Long? {
        val downloadUrl = release.assets?.firstOrNull {
            it?.browserDownloadUrl?.endsWith("apk") == true
        }?.browserDownloadUrl
        if (downloadUrl == null) {
            Log.e(TAG, "Download url not found.")
            return null
        }
        return downloadService.downloadUpdate(
            fileName = "AppUpdate${release.tagName?:""}",
            downloadUrl = downloadUrl,
            onCompletedDownload = onCompletedDownload,
            onDownloadError = onDownloadError
            )
    }
}
