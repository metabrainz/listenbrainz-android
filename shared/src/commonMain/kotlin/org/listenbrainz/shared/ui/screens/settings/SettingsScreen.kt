package org.listenbrainz.shared.ui.screens.settings

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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.listenbrainz.shared.model.AppNavigationItem
import org.listenbrainz.shared.model.UiMode
import org.listenbrainz.shared.ui.navigation.TopBar
import org.listenbrainz.shared.ui.navigation.TopBarActions
import org.listenbrainz.shared.ui.theme.ListenBrainzTheme
import org.listenbrainz.shared.util.Constants
import org.listenbrainz.shared.viewmodel.SettingsViewModel
import org.listenbrainz.shared.util.showToast

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
    topBarActions: TopBarActions,
    callbacks: SettingsCallbacksToHomeScreen,
    onNavigationReorderClick: (() -> Unit)?,
    onDonateClick: () -> Unit,
    listenSubmissionSettings: @Composable () -> Unit = {}
) {
    val settingsUiState by viewModel.uiState.collectAsState()
    val isSubmittingLogs = settingsUiState.isSubmittingLogs
    val appPreferences = viewModel.appPreferences
    val scope = rememberCoroutineScope()
    val uiMode by appPreferences.themePreference.getFlow()
        .collectAsState(initial = UiMode.FOLLOW_SYSTEM)
    val isLoggedOut by remember {
        appPreferences
            .getLoginStatusFlow()
            .map { it == Constants.Strings.STATUS_LOGGED_OUT }
    }.collectAsState(initial = false)

    SettingsScreen(
        uiMode = uiMode,
        onUiModeChange = { scope.launch { appPreferences.themePreference.set(it) } },
        isLoggedOut = isLoggedOut,
        callbacks = remember {
            SettingsCallbacks(
                onLoginRequest = callbacks.onLoginRequest,
                logout = viewModel::logout,
                getVersion = viewModel::version,
                onDonateClick = onDonateClick,
                onOnboardingRequest = callbacks.onOnboardingRequest,
                checkForUpdates = callbacks.checkForUpdates,
                onNavigationReorderClick = onNavigationReorderClick
            )
        },
        topBarActions = topBarActions,
        submitLogs = {
            viewModel.logSubmit()
        },
        isSubmittingLogs = isSubmittingLogs,
        listenSubmissionSettings = listenSubmissionSettings
    )
}

@Composable
fun SettingsScreen(
    uiMode: UiMode,
    onUiModeChange: (UiMode) -> Unit,
    isLoggedOut: Boolean,
    callbacks: SettingsCallbacks,
    topBarActions: TopBarActions,
    submitLogs: () -> Unit,
    isSubmittingLogs: Boolean = false,
    listenSubmissionSettings: @Composable () -> Unit = {}
) {
    val uriHandler = LocalUriHandler.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var isCheckingForUpdates by remember { mutableStateOf(false) }
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

            listenSubmissionSettings()

            callbacks.onNavigationReorderClick?.let { onNavigationReorderClick ->
                SettingsTextOption(
                    title = "Reorder navigation bar",
                    subtitle = "Change bottom navigation order",
                    modifier = Modifier.clickable(onClick = onNavigationReorderClick)
                )

                HorizontalDivider()
            }

            SettingsDropdownOption(
                title = "Theme",
                subtitle = "Choose the app's theme on this device",
                options = UiMode.entries,
                selected = uiMode,
                optionLabel = { mode ->
                    when (mode) {
                        UiMode.FOLLOW_SYSTEM -> "Follow system"
                        UiMode.LIGHT -> "Light"
                        UiMode.DARK -> "Dark"
                    }
                },
                onSelect = onUiModeChange
            )

            HorizontalDivider()

            SettingsTextOption(
                modifier = Modifier.clickable {
                    callbacks.onOnboardingRequest()
                },
                title = "Restart onboarding",
                subtitle = "Revisit the onboarding flow again"
            )

            HorizontalDivider()

            callbacks.checkForUpdates?.let { checkForUpdates ->
                SettingsLoadingOption(
                    title = "Check for updates",
                    subtitle = "Check if a new version of the app is available",
                    isLoading = isCheckingForUpdates,
                    onClick = {
                        scope.launch {
                            isCheckingForUpdates = true
                            val isAvailable = checkForUpdates()
                            isCheckingForUpdates = false
                            if (!isAvailable) {
                                showToast("You are already on the latest version!!")
                            }
                        }
                    }
                )

                HorizontalDivider()
            }

            SettingsLoadingOption(
                title = "Report an issue",
                subtitle = "Submit app logs for further investigation",
                isLoading = isSubmittingLogs,
                onClick = {
                    submitLogs()
                }
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
                    uriHandler.openUri(Constants.ABOUT_URL)
                },
                title = "About ListenBrainz"
            )

            SettingsHyperlink(
                modifier = Modifier.clickable(onClick = callbacks.onDonateClick),
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
                                showToast("Logged out successfully!")
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
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenPreview() {
    ListenBrainzTheme {
        SettingsScreen(
            uiMode = UiMode.FOLLOW_SYSTEM,
            onUiModeChange = {},
            isLoggedOut = false,
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
