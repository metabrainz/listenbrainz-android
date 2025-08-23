package org.listenbrainz.android.viewmodel

import android.app.Application
import android.net.Uri
import android.os.Build
import android.util.Log
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
import javax.inject.Inject

@HiltViewModel
class AppUpdatesViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val appUpdatesRepository: AppUpdatesRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AppUpdatesUiState())
    val uiState: StateFlow<AppUpdatesUiState> = _uiState.asStateFlow()

    init {
        checkInstallSource()
        checkForUpdates()
        checkInstallPermission()
    }

    private suspend fun incrementLaunchCount() {
            appPreferences.appLaunchCount.getAndUpdate { it + 1 }
            Log.d("AppUpdatesViewModel", "App launch count incremented")

    }

    private fun checkInstallSource() {
        viewModelScope.launch {
            val currentInstallSource = appPreferences.installSource.get()

            if (currentInstallSource == InstallSource.NOT_CHECKED) {
                val detectedSource = appUpdatesRepository.detectInstallSource(getApplication())
                Log.d("AppUpdatesViewModel", "Detected install source: $detectedSource")
                appPreferences.installSource.set(detectedSource)
            } else {
                Log.d(
                    "AppUpdatesViewModel",
                    "Install source already checked: $currentInstallSource"
                )
            }
        }
    }

    private fun checkForUpdates() {
        viewModelScope.launch {
            incrementLaunchCount()
            val currentLaunchCount = appPreferences.appLaunchCount.get()
            val lastVersionCheckLaunchCount = appPreferences.lastVersionCheckLaunchCount.get()
            val lastPromptLaunchCount = appPreferences.lastUpdatePromptLaunchCount.get()
            val installSource = appPreferences.installSource.get()

            Log.d("AppUpdatesViewModel", "Current launch count: $currentLaunchCount")
            Log.d(
                "AppUpdatesViewModel",
                "Last version check launch count: $lastVersionCheckLaunchCount"
            )
            Log.d("AppUpdatesViewModel", "Last update prompt launch count: $lastPromptLaunchCount")

            // Check if we should check for updates
            val shouldCheckForUpdates =
                    (currentLaunchCount - lastVersionCheckLaunchCount >= Constants.VERSION_CHECK_DURATION ||
                            lastVersionCheckLaunchCount == 0)

            // Check if we should prompt the user (only if they've previously declined)
            val shouldPromptAgain =
                currentLaunchCount - lastPromptLaunchCount >= Constants.RE_PROMPT_USER_AFTER_DENIAL ||
                        lastPromptLaunchCount == 0

            if (shouldCheckForUpdates && shouldPromptAgain) {
                if (installSource != InstallSource.PLAY_STORE) {
                    Log.d("AppUpdatesViewModel", "Checking for updates from github...")
                    fetchAppReleases()
                    appPreferences.lastVersionCheckLaunchCount.set(currentLaunchCount)
                }
            } else {
                Log.d(
                    "AppUpdatesViewModel",
                    "Skipping update check"
                )
            }
        }
    }

    private fun fetchAppReleases() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = appUpdatesRepository.getAppReleasesFromGithub()
            if(result.isSuccess){
                processReleases(result.data)
            }
            else{
                Log.e("AppUpdatesViewModel", "Error fetching releases: ${result.error?.toast}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.error?.toast ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    private fun processReleases(releases: GithubUpdatesList?) {
        if (releases.isNullOrEmpty()) {
            Log.d("AppUpdatesViewModel", "No releases found")
            _uiState.value = _uiState.value.copy(isLoading = false)
            return
        }

        val latestStableRelease = releases.firstOrNull { !it.prerelease.isTrue() }
        val latestRelease = releases.firstOrNull()

        Log.d("AppUpdatesViewModel", "Latest stable release: ${latestStableRelease?.tagName}")
        Log.d("AppUpdatesViewModel", "Latest release: ${latestRelease?.tagName}")

        val currentVersion = appPreferences.version
        val isUpdateAvailable = isNewerVersion(currentVersion, latestStableRelease?.tagName)
                || isNewerVersion(currentVersion, latestRelease?.tagName)

        Log.d("AppUpdatesViewModel", "Current version: $currentVersion")
        Log.d("AppUpdatesViewModel", "Update available: $isUpdateAvailable")

        _uiState.value = _uiState.value.copy(
            latestStableRelease = latestStableRelease,
            latestRelease = latestRelease,
            isUpdateAvailable = isUpdateAvailable,
            isLoading = false
        )
    }


    fun userPromptedForUpdate() {
        viewModelScope.launch {
            val currentLaunchCount = appPreferences.appLaunchCount.get()
            appPreferences.lastUpdatePromptLaunchCount.set(currentLaunchCount)
            Log.d(
                "AppUpdatesViewModel",
                "User prompted for update at launch count: $currentLaunchCount"
            )
        }
    }

    fun downloadGithubUpdate(release: GithubUpdatesListItem,
                             onCompletedDownload: (Uri?) -> Unit,
                             onDownloadError: (String) -> Unit){
        val id = appUpdatesRepository.downloadGithubUpdate(
            release,
            onCompletedDownload,
            onDownloadError)
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
        _uiState.update {
            it.copy(
                isInstallPermissionGranted = true,
                isInstallPermissionRationaleVisible = false
            )
        }
    }

    fun refreshInstallPermissionStatus() {
        checkInstallPermission()
    }
    private fun Boolean?.isTrue(): Boolean = this == true
}
