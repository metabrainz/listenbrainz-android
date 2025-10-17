package org.listenbrainz.android.viewmodel

import android.app.Activity
import android.app.Application
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.createBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.listenbrainz.android.di.IoDispatcher
import org.listenbrainz.android.model.PermissionStatus
import org.listenbrainz.android.model.UiMode
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.remoteplayer.RemotePlaybackHandler
import org.listenbrainz.android.ui.screens.onboarding.auth.LoginConsentScreenUIState
import org.listenbrainz.android.ui.screens.onboarding.listeningApps.AppInfo
import org.listenbrainz.android.ui.screens.onboarding.permissions.PermissionEnum
import org.listenbrainz.android.util.Log
import org.listenbrainz.android.util.Utils.getAllInstalledApps
import org.listenbrainz.android.util.Utils.getListeningApps
import javax.inject.Inject

@HiltViewModel
class DashBoardViewModel @Inject constructor(
    val appPreferences: AppPreferences,
    private val application: Application,
    private val remotePlaybackHandler: RemotePlaybackHandler,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AndroidViewModel(application) {

    val usernameFlow = appPreferences.username.getFlow()
    val permissionStatusFlow = MutableStateFlow(emptyMap<PermissionEnum, PermissionStatus>())

    val permissionsRequestedAteastOnce = appPreferences.requestedPermissionsList.getFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _listeningAppsFlow = MutableStateFlow<List<AppInfo>>(emptyList())
    val listeningAppsFlow = _listeningAppsFlow.asStateFlow()
    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val allApps = _allApps.asStateFlow()

    private val _consentScreenUIState = MutableStateFlow(LoginConsentScreenUIState())
    val consentScreenUIState = _consentScreenUIState.asStateFlow()

    init {
        viewModelScope.launch {
            val cacheData = appPreferences.consentScreenDataCache.getFlow().first()
            if(cacheData.isNotEmpty()){
                _consentScreenUIState.update { uIState ->
                    uIState.copy(
                        data = cacheData,
                        isLoading = false
                    )
                }
            }
        }
    }

    object CrashReporter {
        var isEnabled: Boolean = true

        fun logException(throwable: Throwable) {
            if (isEnabled) {
                Sentry.captureException(throwable)
            }
        }

        fun logMessage(message: String) {
            if (isEnabled) {
                Sentry.captureMessage(message)
            }
        }
    }

    // Sets Ui mode for XML layouts.
    fun setUiMode() {
        viewModelScope.launch {
            when (withContext(ioDispatcher) {
                appPreferences.themePreference.get()
            }) {
                UiMode.DARK -> AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES
                )

                UiMode.LIGHT -> AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO
                )

                else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }


    fun updatePermissionStatus(activity: ComponentActivity) {
        viewModelScope.launch(ioDispatcher) {
            val requiredPermissions = PermissionEnum.getAllRelevantPermissions()
            val permissionMap = mutableMapOf<PermissionEnum, PermissionStatus>()
            val permissionsReqeustedOnce = appPreferences.requestedPermissionsList.getFlow().first()
            requiredPermissions.forEach { permission ->
                if (permission.isGranted(activity)) {
                    permissionMap[permission] = PermissionStatus.GRANTED
                    //This is to ensure that the permission is marked as requested (for devices which already gave permission before any prompt)
                    markPermissionAsRequested(permission)
                } else if (permission.isPermissionPermanentlyDeclined(
                        activity,
                        permissionsReqeustedOnce
                    )
                ) {
                    permissionMap[permission] = PermissionStatus.DENIED_TWICE
                } else {
                    permissionMap[permission] = PermissionStatus.NOT_REQUESTED
                }
            }
            permissionStatusFlow.emit(permissionMap)

        }
    }

    fun markPermissionAsRequested(permission: PermissionEnum) {
        viewModelScope.launch(ioDispatcher) {
            val permissions =
                appPreferences.requestedPermissionsList.getFlow().firstOrNull()?.toMutableList()
            if (permissions != null && !permissions.contains(permission.permission)) {
                permissions.add(permission.permission)
                appPreferences.requestedPermissionsList.set(permissions)
            }
        }
    }

    fun addListeningApps(apps: List<AppInfo>) {
        viewModelScope.launch {
            val currentApps = _listeningAppsFlow.value.toMutableList()
            apps.forEach { app ->
                if (!currentApps.any { it.packageName == app.packageName }) {
                    currentApps.add(app)
                }
            }
            _listeningAppsFlow.emit(currentApps)
            appPreferences.listeningApps.set(currentApps.map { it.packageName })
            //Removing from all apps list
            val updatedAllApps =
                _allApps.value.filter { it.packageName !in currentApps.map { app -> app.packageName } }
            _allApps.emit(updatedAllApps)
        }
    }

    fun updateListeningApps(context: Activity) {
        viewModelScope.launch(Dispatchers.IO) {
            val musicAppsPackageNames = mutableSetOf<String>()
            val fetchedApps = mutableListOf<AppInfo>()
            //First add from the existing listening apps
            val listeningApps = appPreferences.listeningApps.getFlow().firstOrNull()
            listeningApps?.let { list ->
                musicAppsPackageNames.addAll(list)
            }
            val whiteListedApps = appPreferences.listeningWhitelist.getFlow().first()
            context.getListeningApps().forEach {
                musicAppsPackageNames.add(it.packageName)
            }
            getAppInfoFromPackageNames(context, musicAppsPackageNames.toList()).forEach {
                fetchedApps.add(
                    it.copy(
                        isWhitelisted = it.packageName in whiteListedApps
                    )
                )
            }

            // Sort so that Spotify, YouTube Music, and YouTube are at the top if present
            val preferredOrder = listOf(
                "com.spotify.music",
                "com.google.android.apps.youtube.music",
                "com.google.android.youtube"
            )
            val sortedApps = fetchedApps.sortedWith(compareBy({
                val idx = preferredOrder.indexOf(it.packageName)
                if (idx == -1) Int.MAX_VALUE else idx
            }, { it.appName.lowercase() }))

            _listeningAppsFlow.emit(sortedApps)

            //Updating the listening apps preference
            val updatedListeningApps = mutableListOf<String>()
            listeningApps?.let {
                updatedListeningApps.addAll(it)
            }
            musicAppsPackageNames.forEach { app ->
                if (!updatedListeningApps.contains(app)) {
                    updatedListeningApps.add(app)
                }
            }
            appPreferences.listeningApps.set(updatedListeningApps)

            //Adding all installed apps to the list
            val allApps = getAppInfoFromPackageNames(
                context = context,
                packages = context
                    .getAllInstalledApps()
                    .map { it.packageName },
                isListening = false
            )
                .filter {
                    it.packageName !in updatedListeningApps
                }
            _allApps.emit(allApps)
        }
    }

    private fun getAppInfoFromPackageNames(
        context: Activity,
        packages: List<String>,
        isListening: Boolean = true
    ): List<AppInfo> {
        val pm = context.packageManager
        val fetchedApps = mutableListOf<AppInfo>()
        packages.forEach { packageName ->
            try {
                val appInfo = pm.getApplicationInfo(packageName, 0)
                val appLabel = pm.getApplicationLabel(appInfo)

                val iconBitmap = try {
                    val appDrawable = pm.getApplicationIcon(appInfo)
                    when (appDrawable) {
                        is BitmapDrawable -> appDrawable.bitmap
                        else -> {
                            val width = appDrawable.intrinsicWidth.takeIf { it > 0 } ?: 48
                            val height = appDrawable.intrinsicHeight.takeIf { it > 0 } ?: 48
                            val bitmap = createBitmap(width, height)
                            val canvas = Canvas(bitmap)
                            appDrawable.setBounds(0, 0, canvas.width, canvas.height)
                            appDrawable.draw(canvas)
                            bitmap
                        }
                    }
                } catch (e: Exception) {
                    Log.w("Couldn't get icon for $packageName " + e.toString())
                    createBitmap(48, 48).apply {
                        eraseColor(Color.GRAY)
                    }
                }
                fetchedApps.add(
                    AppInfo(
                        appName = appLabel.toString(),
                        packageName = packageName,
                        icon = iconBitmap,
                        isWhitelisted = false,
                        isListening = isListening
                    )
                )
            } catch (e: Exception) {
                Log.d("Couldn't get info for package $packageName")
            }
        }
        return fetchedApps
    }

    fun onAppCheckChange(isChecked: Boolean, appInfo: AppInfo) {
        viewModelScope.launch(ioDispatcher) {
            val whitelist = appPreferences.listeningWhitelist.getFlow().first().toMutableList()
            if (isChecked) {
                if (!whitelist.contains(appInfo.packageName)) {
                    whitelist.add(appInfo.packageName)
                }
            } else {
                whitelist.remove(appInfo.packageName)
            }
            appPreferences.listeningWhitelist.set(whitelist)
            //Update the flow to reflect the changes
            val updatedApps = _listeningAppsFlow.value.map { app ->
                if (app.packageName == appInfo.packageName) {
                    app.copy(isWhitelisted = isChecked)
                } else {
                    app
                }
            }
            _listeningAppsFlow.emit(updatedApps)
        }
    }

    fun onListeningStatusChange(boolean: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            appPreferences.isListeningAllowed.set(boolean)
        }
    }

    fun changeConsentScreenUIState(consentScreenUIState: LoginConsentScreenUIState) {
        _consentScreenUIState.update {
            consentScreenUIState
        }
        if(consentScreenUIState.data?.isNotEmpty() == true) {
            viewModelScope.launch(ioDispatcher) {
                appPreferences.consentScreenDataCache.set(consentScreenUIState.data)
            }
        }
    }

    fun onLoadConsentScreen() {
        _consentScreenUIState.update { it ->
            it.copy(
                isLoading = true,
                errorMessage = null,
            )
        }
        _consentScreenUIState.value.webView?.loadUrl("https://listenbrainz.org/login")
    }

    suspend fun isNotificationListenerServiceAllowed(): Boolean {
        return withContext(ioDispatcher) {
            appPreferences.isNotificationServiceAllowed
                    && appPreferences.isListeningAllowed.get()
        } && appPreferences.lbAccessToken.get().isNotEmpty()
    }


    fun connectToSpotify() {
        viewModelScope.launch {
            remotePlaybackHandler.connectToSpotify {
                // TODO: Propagate error to UI
            }
        }
    }

    fun disconnectSpotify() {
        viewModelScope.launch { remotePlaybackHandler.disconnectSpotify() }
    }

    init {
        observeCrashReportingPreference()
    }

    private fun observeCrashReportingPreference() {
        viewModelScope.launch {
            appPreferences.isCrashReportingEnabled.getFlow().collect { enabled ->
                setCrashReportingEnabled(enabled)
            }
        }
    }

    private fun setCrashReportingEnabled(enabled: Boolean) {
        CrashReporter.isEnabled = enabled
        Log.d("Crash reporting ${if (enabled) "enabled" else "disabled"}")
    }

    fun toggleCrashReporting(enabled: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            appPreferences.isCrashReportingEnabled.set(enabled)
            // observer automatically updates CrashReporter.isEnabled
        }
    }

}