package org.listenbrainz.android.ui.screens.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.listenbrainz.android.service.ListenServiceState
import org.listenbrainz.android.application.App
import org.listenbrainz.shared.model.UiMode
import org.listenbrainz.android.viewmodel.AppUpdatesViewModel
import org.listenbrainz.shared.viewmodel.DashBoardViewModel
import org.listenbrainz.android.viewmodel.ListeningAppsViewModel

class MainActivity : ComponentActivity() {

    private val dashBoardViewModel: DashBoardViewModel by viewModel()

    private val appUpdatesViewModel: AppUpdatesViewModel by viewModel()

    private val listeningAppsViewModel: ListeningAppsViewModel by viewModel()

    private val listenServiceState: ListenServiceState by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition { dashBoardViewModel.onboardingState.value == null }
        enableEdgeToEdge()

        setUiMode()
        listeningAppsViewModel.updateListeningApps()
        appUpdatesViewModel.checkForUpdatesDuringLaunch(this)
        setContent {
            ListenBrainzApp()
        }
    }

    // Keeps AppCompat's night mode in step with the theme preference.
    private fun setUiMode() {
        lifecycleScope.launch {
            dashBoardViewModel.appPreferences.themePreference.getFlow().collect { uiMode ->
                when (uiMode) {
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
    }

    override fun onResume() {
        super.onResume()
        appUpdatesViewModel.refreshInstallPermissionStatus()

        // Handle permission granted flow - if permission was granted, update state
        val uiState = appUpdatesViewModel.uiState.value
        if (uiState.isInstallPermissionGranted && uiState.isWaitingForPermissionToUpdateApp) {
            appUpdatesViewModel.onInstallPermissionGranted()
        }

        lifecycleScope.launch {
            App.startListenService(appPreferences = dashBoardViewModel.appPreferences, serviceState = listenServiceState)
        }
    }
}
