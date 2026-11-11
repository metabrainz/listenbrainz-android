package org.listenbrainz.shared.ui.screens.settings

import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.listenbrainz.shared.ui.navigation.TopBarActions
import org.listenbrainz.shared.ui.screens.profile.listens.ListeningAppsList
import org.listenbrainz.shared.ui.theme.ListenBrainzTheme
import org.listenbrainz.shared.model.PermissionStatus
import org.listenbrainz.shared.permission.AndroidPermission
import org.listenbrainz.shared.permission.PermissionManager
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.shared.repository.AppPreferencesImpl
import org.listenbrainz.shared.viewmodel.ListensViewModel

/** The Settings section for submitting listens from other apps' notifications. */
@Composable
fun ListenSubmissionSettings(
    appPreferences: AppPreferences = koinInject(),
    listensViewModel: ListensViewModel = koinViewModel(),
    permissionManager: PermissionManager = koinInject()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showBlacklist by remember { mutableStateOf(false) }
    val preferencesUiState by listensViewModel.preferencesUiState.collectAsState()
    val permissions by permissionManager.statuses.collectAsState()
    // Absent means not on this OS version, or not loaded yet: the row stays hidden
    val batteryOptimizationStatus = permissions[AndroidPermission.BATTERY_OPTIMIZATION]
    val isBatteryOptimizationPermissionGranted =
        batteryOptimizationStatus != PermissionStatus.NOT_GRANTED
                && batteryOptimizationStatus != PermissionStatus.PERMANENTLY_DENIED

    /** This preference state can only be changed when the user exits the app, and we always update
     * this state when the user exits the app, this will always be true.*/
    var isNotificationServiceAllowed by remember {
        mutableStateOf(appPreferences.isNotificationServiceAllowed)
    }
    val submitListensCheckedState by appPreferences.isListeningAllowed
        .getFlow().collectAsState(initial = true)
    val shouldListenNewPlayers by appPreferences.shouldListenNewPlayers.getFlow()
        .collectAsState(initial = false)
    val indentedModifier = Modifier.padding(start = 16.dp)

    LifecycleResumeEffect(key1 = Unit) {
        scope.launch {
            isNotificationServiceAllowed = withContext(Dispatchers.IO) {
                appPreferences.isNotificationServiceAllowed
            }
        }
        onPauseOrDispose {}
    }

    SettingsHeader(title = "Listen Submission")

    Column(verticalArrangement = Arrangement.spacedBy(ListenBrainzTheme.paddings.settings)) {
        SettingsSwitchOption(
            title = "Send listens",
            subtitle = "Enable sending listens from this device to ListenBrainz",
            enabled = isNotificationServiceAllowed,
            isChecked = submitListensCheckedState && isNotificationServiceAllowed
        ) { checked ->
            scope.launch {
                if (isNotificationServiceAllowed) {
                    // Set preference
                    appPreferences.isListeningAllowed.set(checked)
                }
            }
        }

        HorizontalDivider(modifier = indentedModifier)

        SettingsSwitchOption(
            title = "Notifications",
            subtitle = "Required to send listens",
            isChecked = isNotificationServiceAllowed
        ) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            context.startActivity(intent)
        }


        AnimatedVisibility(isNotificationServiceAllowed && submitListensCheckedState) {
            Column(
                verticalArrangement = Arrangement.spacedBy(ListenBrainzTheme.paddings.settings)
            ) {
                HorizontalDivider(modifier = indentedModifier)

                // Blacklist
                SettingsTextOption(
                    modifier = Modifier.clickable {
                        if (isNotificationServiceAllowed) {
                            showBlacklist = true
                        }
                    },
                    title = "Listening Apps",
                    subtitle = "Enable sending listens from individual apps on this device",
                    enabled = isNotificationServiceAllowed
                )

                HorizontalDivider(modifier = indentedModifier)

                if (!isBatteryOptimizationPermissionGranted) {
                    SettingsTextOption(
                        modifier = Modifier.clickable() {
                            scope.launch {
                                permissionManager.request(AndroidPermission.BATTERY_OPTIMIZATION)
                            }
                        },
                        title = "Disable Battery Optimization",
                        subtitle = "Required to send listens",
                    )

                    HorizontalDivider(modifier = indentedModifier)
                }

                SettingsSwitchOption(
                    title = "Enable new players",
                    subtitle = "When a new music app is detected, automatically use it to submit listens",
                    isChecked = shouldListenNewPlayers
                ) {
                    scope.launch {
                        appPreferences.shouldListenNewPlayers.set(it)
                    }
                }
            }
        }

    }

    HorizontalDivider()

    // BlackList Dialog
    if (showBlacklist) {
        ListeningAppsList(
            preferencesUiState = preferencesUiState,
            fetchLinkedServices = listensViewModel::fetchLinkedServices,
            getPackageIcon = listensViewModel::getPackageIcon,
            getPackageLabel = listensViewModel::getPackageLabel,
            setWhitelist = listensViewModel::setWhitelist,
        ) { showBlacklist = false }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenPreview() {
    ListenBrainzTheme {
        SettingsScreen(
            appPreferences = AppPreferencesImpl(LocalContext.current),
            callbacks = SettingsCallbacks(
                logout = {},
                getVersion = { "1.0.0" },
                onLoginRequest = {},
                onOnboardingRequest = {},
                onDonateClick = {},
                checkForUpdates = suspend { false },
                onNavigationReorderClick = {}
            ),
            topBarActions = TopBarActions(),
            submitLogs = {},
            isSubmittingLogs = false
        )
    }
}
