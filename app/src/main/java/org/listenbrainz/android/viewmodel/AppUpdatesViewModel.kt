package org.listenbrainz.android.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.listenbrainz.android.model.InstallSource
import org.listenbrainz.android.model.githubupdates.GithubUpdatesList
import org.listenbrainz.android.model.githubupdates.GithubUpdatesListItem
import org.listenbrainz.android.repository.appupdates.AppUpdatesRepository
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.util.Constants
import org.listenbrainz.android.util.Utils.isNewerVersion
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AppUpdatesViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val appUpdatesRepository: AppUpdatesRepository,
    application: Application
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "AppUpdatesViewModel"
    }

    private val _uiState = MutableStateFlow(AppUpdatesUiState())
    val uiState: StateFlow<AppUpdatesUiState> = _uiState.asStateFlow()

    init {
        checkInstallSource()
        checkForUpdatesDuringLaunch()
        checkInstallPermission()
    }

    private suspend fun incrementLaunchCount() {
        appPreferences.appLaunchCount.getAndUpdate { it + 1 }
        Log.d(TAG, "App launch count incremented")

    }

    private fun checkInstallSource() {
        viewModelScope.launch {
            val currentInstallSource = appPreferences.installSource.get()

            if (currentInstallSource == InstallSource.NOT_CHECKED) {
                val detectedSource = appUpdatesRepository.detectInstallSource(getApplication())
                Log.d(TAG, "Detected install source: $detectedSource")
                appPreferences.installSource.set(detectedSource)
            } else {
                Log.d(
                    TAG,
                    "Install source already checked: $currentInstallSource"
                )
            }
        }
    }

    private fun checkForUpdatesDuringLaunch() {
        viewModelScope.launch {
            incrementLaunchCount()
            val lastVersionCheckLaunchCount = appPreferences.lastVersionCheckLaunchCount.get()
            val lastPromptLaunchCount = appPreferences.lastUpdatePromptLaunchCount.get()
            val currentLaunchCount = appPreferences.appLaunchCount.get()

            Log.d(TAG, "Current launch count: $currentLaunchCount")
            Log.d(
                TAG,
                "Last version check launch count: $lastVersionCheckLaunchCount"
            )
            Log.d(TAG, "Last update prompt launch count: $lastPromptLaunchCount")

            // Check if we should check for updates
            val shouldCheckForUpdates =
                (currentLaunchCount - lastVersionCheckLaunchCount >= Constants.VERSION_CHECK_DURATION ||
                        lastVersionCheckLaunchCount == 0)

            // Check if we should prompt the user (only if they've previously declined)
            val shouldPromptAgain =
                currentLaunchCount - lastPromptLaunchCount >= Constants.RE_PROMPT_USER_AFTER_DENIAL ||
                        lastPromptLaunchCount == 0

            if (shouldCheckForUpdates && shouldPromptAgain) {
                checkForUpdates()
            } else {
                Log.d(
                    TAG,
                    "Skipping update check"
                )
            }
        }
    }

    fun checkForUpdates(onUpdateNotAvailable: ()-> Unit = {}) {
        viewModelScope.launch() {
            val installSource = appPreferences.installSource.get()
            val currentLaunchCount = appPreferences.appLaunchCount.get()
            val currentVersion = appPreferences.version
            if (installSource != InstallSource.PLAY_STORE) {
                Log.d(TAG, "Checking for updates from github...")
                val downloadId = appPreferences.downloadId.get()
                fetchAppReleases()
                if (downloadId != 0L) {
                    Log.d(TAG, "Resuming existing download with ID: $downloadId")
                    appUpdatesRepository.queryDownloadStatus(
                        downloadId = downloadId,
                        onCompletedDownload = { uri ->
                            Log.d(TAG, "Download was completed with URI: $uri")
                            //Two situations are possible here:
                            // 1. App update was installed successfully, do the cleanup if not done already
                            if (uri != null && (getFileNameFromUri(uri) == uiState.value.latestRelease?.tagName ||
                                        getFileNameFromUri(uri) == uiState.value.latestStableRelease?.tagName)
                                && !isNewerVersion(currentVersion, getFileNameFromUri(uri))
                            ) {
                                cleanUpAPKAfterInstall(uri)
                            }
                            //2. Download completed but user didn't install the update, but will have to check if the downloaded apk is still the latest one
                            else if (uri != null && (getFileNameFromUri(uri) == uiState.value.latestRelease?.tagName ||
                                        getFileNameFromUri(uri) == uiState.value.latestStableRelease?.tagName)
                            ) {
                                // Check if the downloaded APK matches the latest release
                                val latestRelease = uiState.value.latestRelease
                                val latestStableRelease = uiState.value.latestStableRelease
                                val downloadedFileName = getFileNameFromUri(uri)
                                if (downloadedFileName == latestRelease?.tagName || downloadedFileName == latestStableRelease?.tagName) {
                                    // The downloaded APK is still the latest one, show the install dialog
                                    _uiState.update {
                                        it.copy(
                                            downloadedApkUri = uri,
                                            isInstallAppDialogVisible = true
                                        )
                                    }
                                } else {
                                    cleanUpAPKAfterInstall(uri)
                                }
                            } else {
                                Log.d(TAG, "Download completed but URI is null")
                                viewModelScope.launch {
                                    appPreferences.downloadId.set(0L)
                                }
                            }
                        },
                        onDownloadError = { error ->
                            Log.e(TAG, "Error in download query: $error")
                            viewModelScope.launch {
                                appPreferences.downloadId.set(0L)
                            }
                        },
                        onDownloadRunning = {
                            Log.d(TAG, "Download is still running...")
                            appUpdatesRepository.registerDownloadBroadcastReceiver(
                                downloadId = downloadId,
                                onCompletedDownload = { uri ->
                                    _uiState.update {
                                        it.copy(
                                            downloadedApkUri = uri,
                                            isInstallAppDialogVisible = true
                                        )
                                    }
                                },
                                onDownloadError = {
                                    Log.e(
                                        TAG,
                                        "Error in download broadcast receiver: $it"
                                    )
                                }
                            )
                        })
                } else {
                    val latestStableRelease = _uiState.value.latestStableRelease
                    val latestRelease = _uiState.value.latestRelease
                    val isUpdateAvailable =
                        isNewerVersion(currentVersion, latestStableRelease?.tagName)
                                || isNewerVersion(currentVersion, latestRelease?.tagName)

                    Log.d(TAG, "Current version: $currentVersion")
                    Log.d(TAG, "Update available: $isUpdateAvailable")

                    _uiState.update {
                        it.copy(isUpdateAvailable = isUpdateAvailable)
                    }
                    if (!isUpdateAvailable) {
                        onUpdateNotAvailable()
                    }
                }
                appPreferences.lastVersionCheckLaunchCount.set(currentLaunchCount)
            }
        }
    }

    private suspend fun fetchAppReleases() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        val result = appUpdatesRepository.getAppReleasesFromGithub()
        if (result.isSuccess) {
            processReleases(result.data)
        } else {
            Log.e(TAG, "Error fetching releases: ${result.error?.toast}")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = result.error?.toast ?: "Unknown error occurred"
                )
            }
        }
    }

    private fun processReleases(releases: GithubUpdatesList?) {
        if (releases.isNullOrEmpty()) {
            Log.d(TAG, "No releases found")
            _uiState.value = _uiState.value.copy(isLoading = false)
            return
        }

        val latestStableRelease = releases.firstOrNull { !it.prerelease.isTrue() }
        val latestRelease = releases.firstOrNull()

        _uiState.value = _uiState.value.copy(
            latestStableRelease = latestStableRelease,
            latestRelease = latestRelease,
            isLoading = false
        )
    }


    fun userPromptedForUpdate() {
        viewModelScope.launch {
            val currentLaunchCount = appPreferences.appLaunchCount.get()
            appPreferences.lastUpdatePromptLaunchCount.set(currentLaunchCount)
            Log.d(
                TAG,
                "User prompted for update at launch count: $currentLaunchCount"
            )
        }
    }

    fun downloadGithubUpdate(
        release: GithubUpdatesListItem,
        onCompletedDownload: (Uri?) -> Unit,
        onDownloadError: (String) -> Unit
    ) {
        val id = appUpdatesRepository.downloadGithubUpdate(
            release,
            onCompletedDownload = { uri ->
                _uiState.update {
                    it.copy(
                        downloadedApkUri = uri,
                        isInstallAppDialogVisible = true
                    )
                }
                onCompletedDownload(uri)
            },
            onDownloadError = onDownloadError
        )
        //Saving the download id to preferences
        if (id != null) {
            viewModelScope.launch {
                appPreferences.downloadId.set(id)
            }
        }
    }

    fun dismissUpdateDialog() {
        _uiState.update {
            it.copy(
                isUpdateAvailable = false,
                latestStableRelease = null,
                latestRelease = null,
                error = null,
                isLoading = false
            )
        }
    }

    private fun checkInstallPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val packageManager = getApplication<Application>().packageManager
            val hasInstallPermission = packageManager.canRequestPackageInstalls()
            _uiState.update {
                it.copy(isInstallPermissionGranted = hasInstallPermission)
            }
        } else {
            // For older versions, permission is granted by default
            _uiState.update {
                it.copy(isInstallPermissionGranted = true)
            }
        }
    }

    fun showInstallPermissionRationale() {
        _uiState.update {
            it.copy(isInstallPermissionRationaleVisible = true)
        }
    }

    fun hideInstallPermissionRationale() {
        _uiState.update {
            it.copy(isInstallPermissionRationaleVisible = false)
        }
    }

    fun onInstallPermissionGranted() {
        _uiState.update { currentState ->
            val newState = currentState.copy(
                isInstallPermissionGranted = true,
                isInstallPermissionRationaleVisible = false
            )

            // If user was waiting for permission to install app, install it now
            if (currentState.isWaitingForPermissionToUpdateApp && currentState.downloadedApkUri != null) {
                installApk(currentState.downloadedApkUri)
                newState.copy(isWaitingForPermissionToUpdateApp = false)
            } else {
                newState
            }
        }
    }

    fun refreshInstallPermissionStatus() {
        checkInstallPermission()
    }

    fun showInstallAppDialog() {
        _uiState.update {
            it.copy(isInstallAppDialogVisible = true)
        }
    }

    fun hideInstallAppDialog() {
        _uiState.update {
            it.copy(
                isInstallAppDialogVisible = false,
                downloadedApkUri = null
            )
        }
    }

    fun installDownloadedApp() {
        val currentState = _uiState.value
        if (currentState.downloadedApkUri != null) {
            if (currentState.isInstallPermissionGranted) {
                installApk(currentState.downloadedApkUri)
            } else {
                _uiState.update {
                    it.copy(
                        isWaitingForPermissionToUpdateApp = true,
                        isInstallPermissionRationaleVisible = true
                    )
                }
            }
        }
    }

    private fun installApk(uri: Uri) {
        try {
            val context = getApplication<Application>()
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
            hideInstallAppDialog()
            Log.d(TAG, "APK installation started")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting APK installation", e)
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        return try {
            when (uri.scheme) {
                "file" -> {
                    File(uri.path ?: "").name
                }

                "content" -> {
                    val path = uri.path
                    if (path != null) {
                        val segments = path.split("/")
                        segments.lastOrNull()
                    } else {
                        null
                    }
                }

                else -> {
                    uri.lastPathSegment
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting file name from URI: $uri", e)
            null
        }
    }


    private fun cleanUpAPKAfterInstall(uri: Uri) {
        viewModelScope.launch {
            try {
                val file = getFileFromUri(uri)
                if (file?.exists() == true) {
                    val deleted = file.delete()
                    Log.d(TAG, "Deleted APK file after installation: $deleted")
                } else {
                    Log.w(TAG, "APK file not found for cleanup")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting APK file after installation", e)
            } finally {
                appPreferences.downloadId.set(0L)
            }
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            when (uri.scheme) {
                "file" -> {
                    File(uri.path ?: return null)
                }

                "content" -> {
                    // For FileProvider URIs, reconstruct the actual file path
                    val context = getApplication<Application>()
                    val authority = "${context.packageName}.provider"
                    if (uri.authority == authority) {
                        val relativePath = uri.path?.removePrefix("/downloads/")
                        if (relativePath != null) {
                            File(context.getExternalFilesDir("Download"), relativePath)
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                }

                else -> null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file from URI", e)
            null
        }
    }

    private fun Boolean?.isTrue(): Boolean = this == true
}
