package org.listenbrainz.shared.viewmodel

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import androidx.core.graphics.createBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.listenbrainz.shared.ui.screens.onboarding.listeningApps.AppInfo
import org.listenbrainz.shared.util.AndroidUtils.getAllInstalledApps
import org.listenbrainz.shared.util.AndroidUtils.getListeningApps
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.shared.util.Log

data class ListeningAppsUiState(
    val listeningApps: List<AppInfo> = emptyList(),
    val allApps: List<AppInfo> = emptyList()
)

class ListeningAppsViewModel(
    private val context: Context,
    private val appPreferences: AppPreferences,
    private val logger: Log = Log
) : ViewModel() {

    private val _listeningAppsFlow = MutableStateFlow<List<AppInfo>>(emptyList())
    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())

    val uiState: StateFlow<ListeningAppsUiState> = combine(
        _listeningAppsFlow,
        _allApps,
        ::ListeningAppsUiState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ListeningAppsUiState()
    )

    val isListeningAllowed: Flow<Boolean> = appPreferences.isListeningAllowed.getFlow()

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

    fun updateListeningApps() {
        viewModelScope.launch {
            val musicAppsPackageNames = mutableSetOf<String>()
            val fetchedApps = mutableListOf<AppInfo>()
            // First add from the existing listening apps
            val listeningApps = appPreferences.listeningApps.getFlow().firstOrNull()
            listeningApps?.let { list ->
                musicAppsPackageNames.addAll(list)
            }
            val whiteListedApps = appPreferences.listeningWhitelist.getFlow().first()

            // -- NO SUSPEND FUNCTIONS START --
            context.getListeningApps().forEach {
                musicAppsPackageNames.add(it.packageName)
            }
            getAppInfoFromPackageNames(musicAppsPackageNames.toList()).forEach {
                fetchedApps.add(
                    it.copy(
                        isWhitelisted = it.packageName in whiteListedApps
                    )
                )
            }
            // -- NO SUSPEND FUNCTIONS END --

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

            // Updating the listening apps preference
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

            // Adding all installed apps to the list
            val allApps = getAppInfoFromPackageNames(
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
                    logger.w("Couldn't get icon for $packageName " + e.toString())
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
                logger.d("Couldn't get info for package $packageName")
            }
        }
        return fetchedApps
    }

    fun onAppCheckChange(isChecked: Boolean, appInfo: AppInfo) {
        viewModelScope.launch {
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
        viewModelScope.launch {
            appPreferences.isListeningAllowed.set(boolean)
        }
    }
}
