package org.listenbrainz.android.ui.screens.main

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.listenbrainz.android.application.App
import org.listenbrainz.android.model.PermissionStatus
import org.listenbrainz.android.model.UiMode
import org.listenbrainz.android.ui.components.OnboardingScreenBackground
import org.listenbrainz.android.ui.navigation.NavigationItem
import org.listenbrainz.android.ui.navigation.TopBarActions
import org.listenbrainz.android.ui.screens.appupdates.AppUpdateDialog
import org.listenbrainz.android.ui.screens.appupdates.InstallAppDialog
import org.listenbrainz.android.ui.screens.appupdates.InstallPermissionRationaleDialog
import org.listenbrainz.android.ui.screens.onboarding.auth.ConsentScreenDataInitializer
import org.listenbrainz.android.ui.screens.onboarding.auth.ListenBrainzLogin
import org.listenbrainz.android.ui.screens.onboarding.auth.LoginConsentScreen
import org.listenbrainz.android.ui.screens.onboarding.introduction.IntroductionScreens
import org.listenbrainz.android.ui.screens.onboarding.listeningApps.ListeningAppSelectionScreen
import org.listenbrainz.android.ui.screens.onboarding.permissions.PermissionScreen
import org.listenbrainz.android.ui.screens.settings.SettingsCallbacksToHomeScreen
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.AppUpdatesViewModel
import org.listenbrainz.android.viewmodel.DashBoardViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var _dashBoardViewModel: DashBoardViewModel
    private val dashBoardViewModel get() = _dashBoardViewModel

    private lateinit var _appUpdatesViewModel: AppUpdatesViewModel
    private val appUpdatesViewModel get() = _appUpdatesViewModel

    private val onboardingScreensQueue: MutableList<NavKey> =
        mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        _dashBoardViewModel = ViewModelProvider(this)[DashBoardViewModel::class.java]
        _appUpdatesViewModel = ViewModelProvider(this)[AppUpdatesViewModel::class.java]

        dashBoardViewModel.setUiMode()
        dashBoardViewModel.updatePermissionStatus(this)
        dashBoardViewModel.updateListeningApps(this)
        appUpdatesViewModel.checkForUpdatesDuringLaunch(this)

        setContent {
            ListenBrainzTheme {

                onboardingNavigationSetup(dashBoardViewModel)
                DisposableEffect(Unit) {
                    dashBoardViewModel.connectToSpotify()
                    onDispose {
                        dashBoardViewModel.disconnectSpotify()
                    }
                }

                val backStack =
                    rememberNavBackStack(
                        if (onboardingScreensQueue.isNotEmpty()) {
                            onboardingScreensQueue.removeAt(0)
                        } else NavigationItem.HomeScreen
                    )
                SetStatusAndNavigationBarTheme(backStack)
                OnboardingScreenBackground(backStack)
                ConsentScreenDataInitializer(dashBoardViewModel)
                NavDisplay(
                    backStack = backStack,
                    onBack = {
                        repeat(it) {
                            val screen = backStack.removeAt(backStack.lastIndex)
                            onboardingBackHandler(screen)
                        }
                    },
                    entryProvider = entryProvider {
                        entry<NavigationItem.OnboardingScreens.IntroductionScreen> {
                            IntroductionScreens {
                                onNavigateInOnboarding(
                                    backStack,
                                    dashBoardViewModel
                                )
                            }
                        }
                        entry<NavigationItem.OnboardingScreens.LoginConsentScreen> {
                            LaunchedEffect(Unit) {
                                dashBoardViewModel.appPreferences.getLoginStatusFlow()
                                    .collectLatest {
                                        if (dashBoardViewModel.appPreferences.isUserLoggedIn()) {
                                            onboardingScreensQueue.remove(NavigationItem.OnboardingScreens.LoginScreen)
                                            onboardingScreensQueue.remove(NavigationItem.OnboardingScreens.LoginConsentScreen)
                                            backStack.remove(NavigationItem.OnboardingScreens.LoginScreen)
                                            backStack.remove(NavigationItem.OnboardingScreens.LoginConsentScreen)
                                            onNavigateInOnboarding(backStack, dashBoardViewModel)
                                        }
                                    }
                            }
                            LoginConsentScreen(
                                dashBoardViewModel
                            ) {
                                onNavigateInOnboarding(
                                    backStack,
                                    dashBoardViewModel
                                )
                            }
                        }
                        entry<NavigationItem.OnboardingScreens.LoginScreen> {
                            LaunchedEffect(Unit) {
                                dashBoardViewModel.appPreferences.getLoginStatusFlow()
                                    .collectLatest {
                                        if (dashBoardViewModel.appPreferences.isUserLoggedIn()) {
                                            onboardingScreensQueue.remove(NavigationItem.OnboardingScreens.LoginScreen)
                                            onboardingScreensQueue.remove(NavigationItem.OnboardingScreens.LoginConsentScreen)
                                            backStack.remove(NavigationItem.OnboardingScreens.LoginScreen)
                                            backStack.remove(NavigationItem.OnboardingScreens.LoginConsentScreen)
                                            onNavigateInOnboarding(backStack, dashBoardViewModel)
                                        }
                                    }
                            }
                            ListenBrainzLogin(onLoginFinished = {
                                //Handled above in LaunchedEffect
                            })
                        }
                        entry<NavigationItem.OnboardingScreens.PermissionScreen> {
                            PermissionScreen(
                                onExit = {
                                    onNavigateInOnboarding(
                                        backStack,
                                        dashBoardViewModel
                                    )
                                },
                                onExitAfterGrantingAllPermissions = {
                                    onNavigateInOnboarding(
                                        backStack,
                                        dashBoardViewModel
                                    )
                                    backStack.remove(NavigationItem.OnboardingScreens.PermissionScreen)
                                    onboardingScreensQueue.remove(NavigationItem.OnboardingScreens.PermissionScreen)
                                })
                        }
                        entry<NavigationItem.OnboardingScreens.ListeningAppScreen> {
                            ListeningAppSelectionScreen(
                                onClickNext = {
                                    onNavigateInOnboarding(backStack, dashBoardViewModel)
                                }
                            )
                        }
                        entry<NavigationItem.HomeScreen> {
                            HomeScreen(
                                settingsCallbacks = SettingsCallbacksToHomeScreen(
                                    onLoginRequest = {
                                        backStack.add(NavigationItem.OnboardingScreens.LoginScreen)
                                    },
                                    onOnboardingRequest = {
                                        dashBoardViewModel.appPreferences.onboardingCompleted = false
                                        onboardingNavigationSetup(dashBoardViewModel)
                                        backStack.add(
                                            if (onboardingScreensQueue.isNotEmpty()) {
                                                onboardingScreensQueue.removeAt(0)
                                            } else NavigationItem.HomeScreen
                                        )
                                    },
                                    checkForUpdates = {
                                        appUpdatesViewModel.checkForUpdates(
                                            activity = this@MainActivity,
                                            onUpdateNotAvailable = {
                                                Toast.makeText(this@MainActivity, "No updates available", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    },
                                    topBarActions = TopBarActions()
                                )
                            )
                        }
                    },
                    transitionSpec = {
                        slideInHorizontally(initialOffsetX = { it }) togetherWith
                                slideOutHorizontally(targetOffsetX = { -it })
                    },
                    popTransitionSpec = {
                        slideInHorizontally(initialOffsetX = { -it }) togetherWith
                                slideOutHorizontally(targetOffsetX = { it })
                    },
                    predictivePopTransitionSpec = {
                        slideInHorizontally(initialOffsetX = { -it }) togetherWith
                                slideOutHorizontally(targetOffsetX = { it })
                    },
                )

                AppUpdateDialog(viewModel = appUpdatesViewModel)
                InstallPermissionRationaleDialog(viewModel = appUpdatesViewModel)
                InstallAppDialog(viewModel = appUpdatesViewModel)
            }

        }
    }

    @Composable
    fun SetStatusAndNavigationBarTheme(backStack: NavBackStack) {
        val isDarkTheme = isSystemInDarkTheme()
        val uiMode by dashBoardViewModel.appPreferences.themePreference.getFlow()
            .collectAsState(initial = UiMode.FOLLOW_SYSTEM)

        SideEffect {
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

    //Handling all onboarding navigation logic
    fun onboardingNavigationSetup(dashBoardViewModel: DashBoardViewModel) {
        //Blocking the main thread to ensure that the onboarding screens are set up before the UI is displayed
        runBlocking {
            if (!dashBoardViewModel.appPreferences.onboardingCompleted) {
                onboardingScreensQueue.addAll(
                    listOf(
                        NavigationItem.OnboardingScreens.IntroductionScreen,
                        NavigationItem.OnboardingScreens.LoginConsentScreen,
                        NavigationItem.OnboardingScreens.LoginScreen,
                        NavigationItem.OnboardingScreens.PermissionScreen,
                        NavigationItem.OnboardingScreens.ListeningAppScreen
                    )
                )
            }
            if (dashBoardViewModel.appPreferences.isUserLoggedIn()) {
                onboardingScreensQueue.remove(NavigationItem.OnboardingScreens.LoginConsentScreen)
                onboardingScreensQueue.remove(NavigationItem.OnboardingScreens.LoginScreen)
            } else {
                onboardingScreensQueue.add(NavigationItem.OnboardingScreens.LoginConsentScreen)
                onboardingScreensQueue.add(NavigationItem.OnboardingScreens.LoginScreen)
            }

            if (dashBoardViewModel.permissionStatusFlow.first()
                    .all { it.value == PermissionStatus.GRANTED }
            ) {
                onboardingScreensQueue.remove(NavigationItem.OnboardingScreens.PermissionScreen)
            }
        }
    }


    fun onNavigateInOnboarding(
        backStack: NavBackStack,
        dashBoardViewModel: DashBoardViewModel
    ) {
        if (onboardingScreensQueue.isNotEmpty()) {
            // Add the next onboarding screen to the back stack
            backStack.add(onboardingScreensQueue.removeAt(0))
        } else {
            // If no more onboarding screens, onboarding is complete
            dashBoardViewModel.appPreferences.onboardingCompleted = true
            if (backStack.all { it is NavigationItem.OnboardingScreens }) {
                // If the back stack has only onboarding screens, add home screen to the back stack
                backStack.add(NavigationItem.HomeScreen)
            }
            // Remove all onboarding screens from the back stack
            val onboardingItems = backStack.filterIsInstance<NavigationItem.OnboardingScreens>()
            backStack.removeAll(onboardingItems)
            // If there are still items in the back stack, means onboarding screen was called from app
            // No need to navigate to home screen
        }
    }

    fun onboardingBackHandler(key: NavKey) {
        if (!dashBoardViewModel.appPreferences.onboardingCompleted && key is NavigationItem.OnboardingScreens) {
            onboardingScreensQueue.add(0, key)
        }
    }


    override fun onResume() {
        super.onResume()
        dashBoardViewModel.updatePermissionStatus(this)
        appUpdatesViewModel.refreshInstallPermissionStatus()

        // Handle permission granted flow - if permission was granted, update state
        val uiState = appUpdatesViewModel.uiState.value
        if (uiState.isInstallPermissionGranted && uiState.isWaitingForPermissionToUpdateApp) {
            appUpdatesViewModel.onInstallPermissionGranted()
        }

        lifecycleScope.launch {
            App.startListenService(appPreferences = dashBoardViewModel.appPreferences)
        }
    }
}
