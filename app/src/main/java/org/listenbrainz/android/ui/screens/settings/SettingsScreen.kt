package org.listenbrainz.android.ui.screens.settings

import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.listenbrainz.shared.model.AppNavigationItem
import org.listenbrainz.android.model.PermissionStatus
import org.listenbrainz.shared.model.UiMode
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.shared.repository.AppPreferencesImpl
import org.listenbrainz.android.ui.navigation.TopBar
import org.listenbrainz.android.ui.navigation.TopBarActions
import org.listenbrainz.android.ui.screens.main.DonateActivity
import org.listenbrainz.android.ui.screens.onboarding.permissions.PermissionEnum
import org.listenbrainz.android.ui.screens.profile.listens.ListeningAppsList
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Constants
import org.listenbrainz.android.util.Utils.submitLogs
import org.listenbrainz.android.viewmodel.DashBoardViewModel
import org.listenbrainz.android.viewmodel.ListensViewModel
import org.listenbrainz.android.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
    listensViewModel: ListensViewModel = koinViewModel(),
    dashBoardViewModel: DashBoardViewModel,
    topBarActions: TopBarActions,
    callbacks: SettingsCallbacksToHomeScreen,
    onNavigationReorderClick: () -> Unit
) {
    val permissions by dashBoardViewModel.permissionStatusFlow.collectAsState()
    val isBatteryOptimizationPermissionGranted =
        permissions[PermissionEnum.BATTERY_OPTIMIZATION] == PermissionStatus.GRANTED
    val preferencesUiState by listensViewModel.preferencesUiState.collectAsState()
    SettingsScreen(
        appPreferences = viewModel.appPreferences,
        preferencesUiState = preferencesUiState,
        callbacks = remember {
            SettingsCallbacks(
                onLoginRequest = callbacks.onLoginRequest,
                logout = viewModel::logout,
                getVersion = viewModel::version,
                fetchLinkedServices = listensViewModel::fetchLinkedServices,
                getPackageIcon = listensViewModel::getPackageIcon,
                getPackageLabel = listensViewModel::getPackageLabel,
                setWhitelist = listensViewModel::setWhitelist,
                onOnboardingRequest = callbacks.onOnboardingRequest,
                checkForUpdates = callbacks.checkForUpdates,
                onNavigationReorderClick = onNavigationReorderClick
            )
        },
        isBatteryOptimizationPermissionGranted = isBatteryOptimizationPermissionGranted,
        topBarActions = topBarActions
    )
}

@Composable
fun SettingsScreen(
    appPreferences: AppPreferences,
    callbacks: SettingsCallbacks,
    preferencesUiState: PreferencesUiState,
    isBatteryOptimizationPermissionGranted: Boolean = true,
    topBarActions: TopBarActions
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val activity = LocalActivity.current
    var showBlacklist by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val darkTheme = ListenBrainzTheme.uiModeIsDark
    val scope = rememberCoroutineScope()
    var isCheckingForUpdates by remember { mutableStateOf(false) }
    var darkThemeCheckedState by remember { mutableStateOf(darkTheme) }

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

    val isLoggedOut by appPreferences.getLoginStatusFlow()
        .map { it == Constants.Strings.STATUS_LOGGED_OUT }.collectAsState(initial = false)

    Column {
        TopBar(
            modifier = Modifier.statusBarsPadding(),
            topBarActions = topBarActions,
            title = AppNavigationItem.Settings.title
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ListenBrainzTheme.colorScheme.background)
                .padding(horizontal = 8.dp)
                .verticalScroll(rememberScrollState()),

            verticalArrangement = Arrangement.spacedBy(ListenBrainzTheme.paddings.settings)
        ) {

            HorizontalDivider()

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

                        if (!isBatteryOptimizationPermissionGranted && PermissionEnum.BATTERY_OPTIMIZATION.isPermissionApplicable()) {
                            SettingsTextOption(
                                modifier = Modifier.clickable() {
                                    if (activity != null) {
                                        //Last two permissions are not required for Battery Optimization permission
                                        PermissionEnum.BATTERY_OPTIMIZATION.requestPermission(
                                            activity,
                                            emptyList()
                                        ) {}
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

            SettingsTextOption(
                title = "Reorder navigation bar",
                subtitle = "Change bottom navigation order",
                modifier = Modifier.clickable(onClick = callbacks.onNavigationReorderClick)
            )

            HorizontalDivider()

            SettingsSwitchOption(
                title = "Dark theme",
                subtitle = "Enable the dark theme on this device",
                isChecked = darkThemeCheckedState
            ) {
                when (darkTheme) {
                    false -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        scope.launch {
                            appPreferences.themePreference.set(UiMode.DARK)
                        }
                    }

                    true -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        scope.launch {
                            appPreferences.themePreference.set(UiMode.LIGHT)
                        }
                    }
                }
                darkThemeCheckedState = it
            }

            HorizontalDivider()

            SettingsTextOption(
                modifier = Modifier.clickable {
                    callbacks.onOnboardingRequest()
                },
                title = "Restart onboarding",
                subtitle = "Revisit the onboarding flow again"
            )

            HorizontalDivider()

            SettingsLoadingOption(
                title = "Check for updates",
                subtitle = "Check if a new version of the app is available",
                isLoading = isCheckingForUpdates,
                onClick = {
                    scope.launch {
                        isCheckingForUpdates = true
                        val isAvailable = callbacks.checkForUpdates()
                        isCheckingForUpdates = false
                        if (!isAvailable) {
                            Toast.makeText(
                                context,
                                "You are already on the latest version!!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )

            HorizontalDivider()

            SettingsTextOption(
                modifier = Modifier.clickable {
                    submitLogs(context)
                },
                title = "Report an issue",
                subtitle = "Submit app logs for further investigation",
                enabled = isNotificationServiceAllowed
            )


            HorizontalDivider()

            SettingsHeader(title = "Account settings")

            if (!isLoggedOut) {
                SettingsTextOption(
                    modifier = Modifier.clickable { showLogoutDialog = true },
                    title = "Logout"
                )

                SettingsHyperlink(
                    modifier = Modifier.clickable {
                        uriHandler.openUri("https://musicbrainz.org/account/delete")
                    },
                    title = "Delete account",
                    textColor = Color.Red,
                    iconColor = Color.Red
                )
            } else {
                SettingsTextOption(
                    modifier = Modifier.clickable {
                        callbacks.onLoginRequest()
                    },
                    title = "Login"
                )
            }

            HorizontalDivider()

            SettingsHeader(title = "About")

            SettingsHyperlink(
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Constants.ABOUT_URL.toUri()
                    context.startActivity(intent)
                },
                title = "About ListenBrainz"
            )

            SettingsHyperlink(
                modifier = Modifier.clickable {
                    context.startActivity(Intent(context, DonateActivity::class.java))
                },
                title = "Support MetaBrainz"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ListenBrainzTheme.paddings.settings),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "v. ${callbacks.getVersion()}",
                    color = Color(0xFF949494)
                )
            }

            HorizontalDivider()

            SettingsHeader(title = "Attributions")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ListenBrainzTheme.paddings.settings),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val annotatedStringAttributions: AnnotatedString = buildAnnotatedString {
                    val originalString =
                        "Animations by Korhan Ulusoy, Jake Cowan, KidA Studio, puput Santoso , Charts by Patrick Michalik and Paul Roux on LottieFiles from lottiefiles.com\n\n" +
                                "The complete resources with links can be found at\n" +
                                "https://github.com/metabrainz/listenbrainz-android/blob/main/asset_attributions.md"
                    val startIndexGithub = originalString.indexOf("https://github.com")
                    val endIndexGithub = startIndexGithub + 82
                    append(originalString)
                    addStyle(
                        style = SpanStyle(
                            textDecoration = TextDecoration.Underline
                        ), start = startIndexGithub, end = endIndexGithub
                    )
                    addStringAnnotation(
                        tag = "URL",
                        annotation = "https://github.com/metabrainz/listenbrainz-android/blob/main/asset_attributions.md",
                        start = startIndexGithub,
                        end = endIndexGithub
                    )
                }

                ClickableText(
                    text = annotatedStringAttributions,
                    style = TextStyle(
                        color = Color(0xFF949494),
                        fontSize = 12.sp
                    ),
                    onClick = { offset ->
                        annotatedStringAttributions
                            .getStringAnnotations("URL", offset, offset)
                            .firstOrNull()?.let { stringAnnotation ->
                                uriHandler.openUri(stringAnnotation.item)
                            }
                    }
                )
            }

            Spacer(modifier = Modifier.height(ListenBrainzTheme.paddings.settings))

            // BlackList Dialog
            if (showBlacklist) {
                ListeningAppsList(
                    preferencesUiState = preferencesUiState,
                    fetchLinkedServices = callbacks.fetchLinkedServices,
                    getPackageIcon = callbacks.getPackageIcon,
                    getPackageLabel = callbacks.getPackageLabel,
                    setWhitelist = callbacks.setWhitelist,
                ) { showBlacklist = false }
            }

            if (showLogoutDialog) {
                AlertDialog(
                    title = {
                        Text(
                            text = "Logout?",
                            color = ListenBrainzTheme.colorScheme.text
                        )
                    },
                    text = {
                        Text(
                            text = "Are you sure you want to logout?",
                            color = ListenBrainzTheme.colorScheme.text
                        )
                    },
                    onDismissRequest = { showLogoutDialog = false },
                    confirmButton = {
                        Button(
                            onClick = { showLogoutDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Text(
                                text = "Cancel",
                                color = ListenBrainzTheme.colorScheme.text
                            )
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                callbacks.logout()
                                showLogoutDialog = false
                                Toast.makeText(
                                    context,
                                    "Logged out successfully!",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text(
                                text = "Confirm",
                                color = Color.White
                            )
                        }
                    }
                )
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenPreview() {
    ListenBrainzTheme {
        SettingsScreen(
            appPreferences = AppPreferencesImpl(LocalContext.current),
            preferencesUiState = PreferencesUiState(),
            callbacks = SettingsCallbacks(
                logout = {},
                getVersion = { "1.0.0" },
                fetchLinkedServices = {},
                getPackageIcon = { null },
                getPackageLabel = { "" },
                setWhitelist = {},
                onLoginRequest = {},
                onOnboardingRequest = {},
                checkForUpdates = suspend{false},
                onNavigationReorderClick = {}
            ),
            topBarActions = TopBarActions()
        )
    }
}