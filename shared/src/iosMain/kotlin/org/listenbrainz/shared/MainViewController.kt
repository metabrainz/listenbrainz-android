package org.listenbrainz.shared

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel
import org.listenbrainz.shared.ui.components.OnboardingScreenBackground
import org.listenbrainz.shared.ui.screens.newsbrainz.NewsBrainzScreen
import org.listenbrainz.shared.ui.screens.onboarding.auth.login.LoginScreen
import org.listenbrainz.shared.ui.screens.onboarding.introduction.IntroductionScreens
import org.listenbrainz.shared.ui.screens.main.HomeScreen
import org.listenbrainz.shared.ui.screens.onboarding.permissions.PermissionScreen
import org.listenbrainz.shared.ui.screens.settings.SettingsCallbacksToHomeScreen
import org.listenbrainz.shared.ui.theme.ListenBrainzTheme
import org.listenbrainz.shared.util.shareLink
import org.listenbrainz.shared.di.ensureKoinStarted
import org.listenbrainz.shared.navigation.NavigationItem
import org.listenbrainz.shared.ui.App
import org.listenbrainz.shared.viewmodel.DashBoardViewModel
import platform.UIKit.UIViewController

/** Entry point for the iOS app, iosApp wraps this in SwiftUI. */
fun MainViewController(): UIViewController {
    ensureKoinStarted()
    return ComposeUIViewController {
        val dashBoardViewModel: DashBoardViewModel = koinViewModel()
        ListenBrainzTheme {
            App(
                dashBoardViewModel = dashBoardViewModel,
                decorations = { backStack -> OnboardingScreenBackground(backStack) },
                entries = { navigator ->
                    entry<NavigationItem.OnboardingScreens.IntroductionScreen> {
                        IntroductionScreens { navigator.onboarding.next() }
                    }
                    entry<NavigationItem.OnboardingScreens.LoginScreen> {
                        LaunchedEffect(Unit) {
                            dashBoardViewModel.appPreferences.getLoginStatusFlow().collectLatest {
                                if (dashBoardViewModel.appPreferences.isUserLoggedIn()) {
                                    navigator.onboarding.onLoggedIn()
                                }
                            }
                        }
                        LoginScreen(onLoginFinished = {})
                    }
                    entry<NavigationItem.OnboardingScreens.PermissionScreen> {
                        PermissionScreen(
                            dashBoardViewModel = dashBoardViewModel,
                            onExit = { navigator.onboarding.next() },
                            onExitAfterGrantingAllPermissions = { navigator.onboarding.onPermissionsGranted() }
                        )
                    }
                    // No listening apps on iOS
                    entry<NavigationItem.OnboardingScreens.ListeningAppScreen> {
                        LaunchedEffect(Unit) { navigator.onboarding.next() }
                    }
                    entry<NavigationItem.HomeScreen> {
                        val uriHandler = LocalUriHandler.current
                        HomeScreen(
                            dashBoardViewModel = dashBoardViewModel,
                            settingsCallbacks = SettingsCallbacksToHomeScreen(
                                checkForUpdates = null,
                                onLoginRequest = {
                                    navigator.backStack.add(NavigationItem.OnboardingScreens.LoginScreen)
                                },
                                onOnboardingRequest = {
                                    dashBoardViewModel.resetOnboardingCompleted()
                                    navigator.onboarding.restart()
                                }
                            ),
                            onNewsClick = { navigator.backStack.add(NavigationItem.NewsBrainzScreen) },
                            onDonateClick = { uriHandler.openUri("https://metabrainz.org/donate") }
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
        }
    }
}
