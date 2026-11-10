package org.listenbrainz.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel
import org.listenbrainz.android.ui.components.OnboardingScreenBackground
import org.listenbrainz.android.ui.screens.onboarding.auth.login.LoginScreen
import org.listenbrainz.android.ui.screens.onboarding.introduction.IntroductionScreens
import org.listenbrainz.android.ui.screens.onboarding.permissions.PermissionScreen
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
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
                            onExit = { navigator.onboarding.next() },
                            onExitAfterGrantingAllPermissions = { navigator.onboarding.onPermissionsGranted() }
                        )
                    }
                    // No listening apps on iOS
                    entry<NavigationItem.OnboardingScreens.ListeningAppScreen> {
                        LaunchedEffect(Unit) { navigator.onboarding.next() }
                    }
                    entry<NavigationItem.HomeScreen> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Home", color = ListenBrainzTheme.colorScheme.text)
                        }
                    }
                }
            )
        }
    }
}
