package org.listenbrainz.shared

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel
import org.listenbrainz.android.ui.components.OnboardingScreenBackground
import org.listenbrainz.android.ui.navigation.TopBarActions
import org.listenbrainz.android.ui.screens.album.AlbumScreen
import org.listenbrainz.android.ui.screens.artist.ArtistScreen
import org.listenbrainz.android.ui.screens.newsbrainz.NewsBrainzScreen
import org.listenbrainz.android.ui.screens.onboarding.auth.login.LoginScreen
import org.listenbrainz.android.ui.screens.onboarding.introduction.IntroductionScreens
import org.listenbrainz.android.ui.screens.onboarding.permissions.PermissionScreen
import org.listenbrainz.android.ui.screens.settings.SettingsCallbacksToHomeScreen
import org.listenbrainz.android.ui.screens.settings.SettingsScreen
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.shareLink
import org.listenbrainz.shared.di.ensureKoinStarted
import org.listenbrainz.shared.navigation.NavigationItem
import org.listenbrainz.shared.ui.App
import org.listenbrainz.shared.ui.fadeOnTopTransition
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
                    val goToArtistPage: (String) -> Unit = { navigator.backStack.add(NavigationItem.ArtistScreen(it)) }
                    // Search isn't on iOS yet
                    val topBarActions = TopBarActions(
                        navigateToSettingsScreen = { navigator.backStack.add(NavigationItem.SettingsScreen) },
                        popBackStackInSettingsScreen = { navigator.backStack.remove(NavigationItem.SettingsScreen) }
                    )
                    entry<NavigationItem.HomeScreen> {
                        IosHomeScreen(
                            dashBoardViewModel = dashBoardViewModel,
                            topBarActions = topBarActions,
                            onNewsClick = { navigator.backStack.add(NavigationItem.NewsBrainzScreen) },
                            goToArtistPage = goToArtistPage
                        )
                    }
                    // Entries share a ViewModelStore, so each page keys its view model
                    entry<NavigationItem.ArtistScreen> { key ->
                        IosPageScaffold { snackbarState ->
                            ArtistScreen(
                                artistMbid = key.mbid,
                                viewModel = koinViewModel(key = "artist:${key.mbid}"),
                                goToArtistPage = goToArtistPage,
                                goToUserPage = {},
                                goToAlbumPage = { navigator.backStack.add(NavigationItem.AlbumScreen(it)) },
                                snackBarState = snackbarState,
                                topBarActions = topBarActions
                            )
                        }
                    }
                    entry<NavigationItem.AlbumScreen> { key ->
                        IosPageScaffold { snackbarState ->
                            AlbumScreen(
                                albumMbid = key.mbid,
                                viewModel = koinViewModel(key = "album:${key.mbid}"),
                                snackBarState = snackbarState,
                                topBarActions = topBarActions
                            )
                        }
                    }
                    entry<NavigationItem.SettingsScreen>(metadata = fadeOnTopTransition) {
                        val uriHandler = LocalUriHandler.current
                        IosPageScaffold {
                            SettingsScreen(
                                topBarActions = topBarActions,
                                callbacks = SettingsCallbacksToHomeScreen(
                                    checkForUpdates = null,
                                    onLoginRequest = {
                                        navigator.backStack.add(NavigationItem.OnboardingScreens.LoginScreen)
                                    },
                                    onOnboardingRequest = {
                                        dashBoardViewModel.resetOnboardingCompleted()
                                        navigator.onboarding.restart()
                                    }
                                ),
                                onNavigationReorderClick = null,
                                onDonateClick = { uriHandler.openUri("https://metabrainz.org/donate") }
                            )
                        }
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
