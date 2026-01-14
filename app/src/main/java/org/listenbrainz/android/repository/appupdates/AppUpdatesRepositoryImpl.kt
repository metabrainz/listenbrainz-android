package org.listenbrainz.android.repository.appupdates

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.listenbrainz.shared.model.InstallSource
import org.listenbrainz.android.model.githubupdates.GithubUpdatesList
import org.listenbrainz.android.model.githubupdates.GithubUpdatesListItem
import org.listenbrainz.android.service.GithubAppUpdatesService
import org.listenbrainz.android.service.GithubUpdatesDownloadService
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils.parseResponse

import kotlin.coroutines.resume

const val TAG = "AppUpdatesRepository"

class AppUpdatesRepositoryImpl(
    private val githubAppUpdatesService: GithubAppUpdatesService,
    private val downloadService: GithubUpdatesDownloadService,
    private val ioDispatcher: CoroutineDispatcher
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
            fileName = release.tagName?:"Unknown",
            downloadUrl = downloadUrl,
            onCompletedDownload = onCompletedDownload,
            onDownloadError = onDownloadError
            )
    }

    override fun queryDownloadStatus(
        downloadId: Long,
        onCompletedDownload: (Uri?) -> Unit,
        onDownloadError: (String) -> Unit,
        onDownloadRunning: () -> Unit
    ) {
        downloadService.queryDownloadStatus(
            downloadId = downloadId,
            onCompletedDownload = onCompletedDownload,
            onDownloadError = onDownloadError,
            onDownloadRunning = onDownloadRunning
        )
    }

    override fun registerDownloadBroadcastReceiver(
        downloadId: Long,
        onCompletedDownload: (Uri?) -> Unit,
        onDownloadError: (String) -> Unit
    ) {
        downloadService.registerDownloadBroadcastReceiver(
            downloadId = downloadId,
            onCompletedDownload = onCompletedDownload,
            onDownloadError = onDownloadError
        )
    }

    override suspend fun checkPlayStoreUpdate(activity: ComponentActivity): Boolean =
        withContext(ioDispatcher) {
            try {
                val appUpdateManager = AppUpdateManagerFactory.create(activity)
                suspendCancellableCoroutine { continuation ->
                    appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                        val isUpdateAvailable = appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        val isFlexibleUpdateAllowed = appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)

                        Log.d(TAG, "Play Store update available: $isUpdateAvailable, flexible allowed: $isFlexibleUpdateAllowed")
                        continuation.resume(isUpdateAvailable && isFlexibleUpdateAllowed)
                    }.addOnFailureListener { exception ->
                        Log.e(TAG, "Error checking for Play Store updates", exception)
                        continuation.resume(false)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking for Play Store updates", e)
                false
            }
        }

    override suspend fun startPlayStoreFlexibleUpdate(
        activity: ComponentActivity,
        onUpdateProgress: (Int) -> Unit,
        onUpdateDownloaded: () -> Unit,
        onUpdateError: (String) -> Unit
    ): Boolean = withContext(ioDispatcher) {
        try {
            val appUpdateManager = AppUpdateManagerFactory.create(activity)
            var listener: InstallStateUpdatedListener? = null

            suspendCancellableCoroutine { continuation ->
                listener = InstallStateUpdatedListener { installState ->
                    when (installState.installStatus()) {
                        InstallStatus.DOWNLOADING -> {
                            val bytesDownloaded = installState.bytesDownloaded()
                            val totalBytesToDownload = installState.totalBytesToDownload()
                            val progress = if (totalBytesToDownload > 0) {
                                ((bytesDownloaded * 100) / totalBytesToDownload).toInt()
                            } else {
                                0
                            }
                            onUpdateProgress(progress)
                        }
                        InstallStatus.DOWNLOADED -> {
                            onUpdateDownloaded()
                            listener?.let { appUpdateManager.unregisterListener(it) }
                        }
                        InstallStatus.FAILED -> {
                            onUpdateError("Update download failed")
                            listener?.let { appUpdateManager.unregisterListener(it) }
                        }
                        InstallStatus.CANCELED -> {
                            onUpdateError("Update download canceled")
                            listener?.let { appUpdateManager.unregisterListener(it) }
                        }
                        else -> {
                            // Other states like PENDING, INSTALLING
                        }
                    }
                }

                listener?.let { appUpdateManager.registerListener(it) }

                appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                    ) {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            activity,
                            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                            123 // Request code
                        )
                        continuation.resume(true)
                    } else {
                        listener?.let { appUpdateManager.unregisterListener(it) }
                        continuation.resume(false)
                    }
                }.addOnFailureListener { exception ->
                    Log.e(TAG, "Error starting flexible update", exception)
                    onUpdateError("Failed to start update: ${exception.message}")
                    listener?.let { appUpdateManager.unregisterListener(it) }
                    continuation.resume(false)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting flexible update", e)
            onUpdateError("Failed to start update: ${e.message}")
            false
        }
    }

    override suspend fun completePlayStoreFlexibleUpdate(activity: ComponentActivity): Boolean =
        withContext(ioDispatcher) {
            try {
                val appUpdateManager = AppUpdateManagerFactory.create(activity)
                appUpdateManager.completeUpdate()
                Log.d(TAG, "Completing flexible update")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error completing flexible update", e)
                false
            }
        }
}
