package org.listenbrainz.android.ui.screens.main

import android.content.Intent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.core.view.WindowCompat
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.listenbrainz.android.ui.components.OnboardingScreenBackground
import org.listenbrainz.android.ui.screens.appupdates.AppUpdateDialog
import org.listenbrainz.android.ui.screens.appupdates.InstallAppDialog
import org.listenbrainz.android.ui.screens.appupdates.InstallPermissionRationaleDialog
import org.listenbrainz.android.ui.screens.appupdates.PlayStoreUpdateAvailableDialog
import org.listenbrainz.android.ui.screens.appupdates.PlayStoreUpdateReadyDialog
import org.listenbrainz.android.ui.screens.newsbrainz.NewsBrainzScreen
import org.listenbrainz.android.ui.screens.onboarding.auth.login.LoginScreen
import org.listenbrainz.android.ui.screens.onboarding.introduction.IntroductionScreens
import org.listenbrainz.android.ui.screens.onboarding.listeningApps.ListeningAppSelectionScreen
import org.listenbrainz.android.ui.screens.onboarding.permissions.PermissionScreen
import org.listenbrainz.android.ui.screens.settings.ListenSubmissionSettings
import org.listenbrainz.android.ui.screens.settings.SettingsCallbacksToHomeScreen
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Utils.getActivity
import org.listenbrainz.android.util.shareLink
import org.listenbrainz.android.viewmodel.AppUpdatesViewModel
import org.listenbrainz.shared.model.UiMode
import org.listenbrainz.shared.navigation.NavigationItem
import org.listenbrainz.shared.ui.App
import org.listenbrainz.shared.viewmodel.DashBoardViewModel

@Composable
fun ListenBrainzApp(
    dashBoardViewModel: DashBoardViewModel = koinViewModel(),
    appUpdatesViewModel: AppUpdatesViewModel = koinViewModel()
) {
    val activity = checkNotNull(LocalContext.current.getActivity())

    ListenBrainzTheme {
        App(
            decorations = { backStack ->
                SetStatusAndNavigationBarTheme(backStack, dashBoardViewModel)
                OnboardingScreenBackground(backStack)
            },
            entries = { navigator ->
                entry<NavigationItem.OnboardingScreens.IntroductionScreen> {
                    IntroductionScreens {
                        navigator.onboarding.next()
                    }
                }
                entry<NavigationItem.OnboardingScreens.LoginScreen> {
                    LaunchedEffect(Unit) {
                        dashBoardViewModel.appPreferences.getLoginStatusFlow()
                            .collectLatest {
                                if (dashBoardViewModel.appPreferences.isUserLoggedIn()) {
                                    navigator.onboarding.onLoggedIn()
                                }
                            }
                    }
                    LoginScreen(onLoginFinished = {
                        //Handled above in LaunchedEffect
                    })
                }
                entry<NavigationItem.OnboardingScreens.PermissionScreen> {
                    PermissionScreen(
                        dashBoardViewModel = dashBoardViewModel,
                        onExit = {
                            navigator.onboarding.next()
                        },
                        onExitAfterGrantingAllPermissions = {
                            navigator.onboarding.onPermissionsGranted()
                        })
                }
                entry<NavigationItem.OnboardingScreens.ListeningAppScreen> {
                    ListeningAppSelectionScreen(
                        dashBoardViewModel = dashBoardViewModel,
                        onClickNext = {
                            navigator.onboarding.next()
                        }
                    )
                }
                entry<NavigationItem.HomeScreen> {
                    HomeScreen(
                        dashBoardViewModel = dashBoardViewModel,
                        settingsCallbacks = SettingsCallbacksToHomeScreen(
                            onLoginRequest = {
                                navigator.backStack.add(NavigationItem.OnboardingScreens.LoginScreen)
                            },
                            onOnboardingRequest = {
                                dashBoardViewModel.resetOnboardingCompleted()
                                navigator.onboarding.restart()
                            },
                            checkForUpdates = {
                                appUpdatesViewModel.checkForUpdates(activity = activity)
                            },
                        ),
                        onNewsClick = {
                            navigator.backStack.add(NavigationItem.NewsBrainzScreen)
                        },
                        onDonateClick = {
                            activity.startActivity(Intent(activity, DonateActivity::class.java))
                        },
                        listenSubmissionSettings = { ListenSubmissionSettings() }
                    )
                }
                entry<NavigationItem.NewsBrainzScreen> {
                    val uriHandler = LocalUriHandler.current
                    NewsBrainzScreen(
                        modifier = Modifier.safeDrawingPadding(),
                        onItemClicked = { post -> uriHandler.openUri(post.URL) },
                        onItemLongClicked = { post -> shareLink(post.URL) },
                        onBack = { navigator.backStack.removeAt(navigator.backStack.lastIndex) }
                    )
                }
            }
        )

        AppUpdateDialog(viewModel = appUpdatesViewModel)
        InstallPermissionRationaleDialog(viewModel = appUpdatesViewModel)
        InstallAppDialog(viewModel = appUpdatesViewModel)
        PlayStoreUpdateReadyDialog(appUpdatesViewModel)
        PlayStoreUpdateAvailableDialog(appUpdatesViewModel)
    }
}

@Composable
private fun SetStatusAndNavigationBarTheme(
    backStack: NavBackStack<NavKey>,
    dashBoardViewModel: DashBoardViewModel
) {
    val window = checkNotNull(LocalContext.current.getActivity()).window
    val isDarkTheme = isSystemInDarkTheme()
    val uiMode by dashBoardViewModel.appPreferences.themePreference.getFlow()
        .collectAsState(initial = UiMode.FOLLOW_SYSTEM)

    LaunchedEffect(backStack.lastIndex, uiMode) {
        val isStatusBarIconColorLight =
            if (backStack[backStack.lastIndex] is NavigationItem.OnboardingScreens) {
                true
            } else {
                when (uiMode) {
                    UiMode.FOLLOW_SYSTEM -> isDarkTheme
                    UiMode.DARK -> true
                    UiMode.LIGHT -> false
                }
            }
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !isStatusBarIconColorLight
            isAppearanceLightNavigationBars = !isStatusBarIconColorLight
        }
    }
}
