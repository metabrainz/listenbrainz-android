package org.listenbrainz.android.repository.appupdates

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.listenbrainz.android.di.IoDispatcher
import org.listenbrainz.android.model.InstallSource
import org.listenbrainz.android.model.githubupdates.GithubUpdatesList
import org.listenbrainz.android.service.GithubAppUpdatesService
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils.parseResponse
import javax.inject.Inject

class AppUpdatesRepositoryImpl @Inject constructor(
    private val githubAppUpdatesService: GithubAppUpdatesService,
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
                installerPackageName == "com.google.android.feedback") {
                Log.d("AppUpdatesRepository", "App installed from Google Play Store")
                InstallSource.PLAY_STORE
            } else {
                //Mostly will get null as the package name while debugging
                Log.d("AppUpdatesRepository", "App not installed from Google Play Store: $installerPackageName")
                InstallSource.NOT_PLAY_STORE
            }
        } catch (e: Exception) {
            Log.e("AppUpdatesRepository", "Error detecting install source", e)
            InstallSource.NOT_PLAY_STORE
        }
    }
}
