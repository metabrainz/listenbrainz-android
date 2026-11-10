package org.listenbrainz.android.ui.screens.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.listenbrainz.android.service.ListenServiceState
import org.listenbrainz.android.application.App
import org.listenbrainz.shared.model.UiMode
import org.listenbrainz.android.ui.components.OnboardingScreenBackground
import org.listenbrainz.android.ui.navigation.TopBarActions
import org.listenbrainz.android.ui.screens.appupdates.AppUpdateDialog
import org.listenbrainz.android.ui.screens.appupdates.InstallAppDialog
import org.listenbrainz.android.ui.screens.appupdates.InstallPermissionRationaleDialog
import org.listenbrainz.android.ui.screens.appupdates.PlayStoreUpdateAvailableDialog
import org.listenbrainz.android.ui.screens.appupdates.PlayStoreUpdateReadyDialog
import org.listenbrainz.android.ui.screens.onboarding.auth.login.LoginScreen
import org.listenbrainz.android.ui.screens.onboarding.introduction.IntroductionScreens
import org.listenbrainz.android.ui.screens.onboarding.listeningApps.ListeningAppSelectionScreen
import org.listenbrainz.android.ui.screens.onboarding.permissions.PermissionScreen
import org.listenbrainz.android.ui.screens.settings.SettingsCallbacksToHomeScreen
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.AppUpdatesViewModel
import org.listenbrainz.shared.viewmodel.DashBoardViewModel
import org.listenbrainz.android.viewmodel.ListeningAppsViewModel
import org.listenbrainz.shared.navigation.NavigationItem
import org.listenbrainz.shared.navigation.navBackStackSerializer
import org.listenbrainz.shared.navigation.navigationSavedStateConfiguration
import org.listenbrainz.shared.onboarding.OnboardingNavigator
import org.listenbrainz.shared.onboarding.startDestination

class MainActivity : ComponentActivity() {

    private val dashBoardViewModel: DashBoardViewModel by viewModel()

    private val appUpdatesViewModel: AppUpdatesViewModel by viewModel()

    private val listeningAppsViewModel: ListeningAppsViewModel by viewModel()

    private val listenServiceState: ListenServiceState by inject()

    private lateinit var backStack: NavBackStack<NavKey>
    private lateinit var onboardingNavigator: OnboardingNavigator
    private var isNavigationReady by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition { !isNavigationReady }
        enableEdgeToEdge()

        setUiMode()
        listeningAppsViewModel.updateListeningApps()
        appUpdatesViewModel.checkForUpdatesDuringLaunch(this)
        val restoredBackStack = savedInstanceState?.getBundle(BACK_STACK_KEY)
            ?.let { decodeFromSavedState(navBackStackSerializer, it, navigationSavedStateConfiguration) }
        lifecycleScope.launch(Dispatchers.Main.immediate) {
            val onboardingState = dashBoardViewModel
                .onboardingState
                .filterNotNull()
                .first()

            val backstack = restoredBackStack ?: NavBackStack<NavKey>(onboardingState.startDestination())

            setUpNavigation(backstack)
        }

        setContent {
            if (!isNavigationReady) return@setContent
            ListenBrainzTheme {
                DisposableEffect(Unit) {
                    dashBoardViewModel.connectToSpotify()
                    onDispose {
                        dashBoardViewModel.disconnectSpotify()
                    }
                }

                SetStatusAndNavigationBarTheme(backStack)
                OnboardingScreenBackground(backStack)
                // ConsentScreenDataInitializer()
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeAt(backStack.lastIndex) },
                    entryProvider = entryProvider {
                        entry<NavigationItem.OnboardingScreens.IntroductionScreen> {
                            IntroductionScreens {
                                onboardingNavigator.next()
                            }
                        }
                        entry<NavigationItem.OnboardingScreens.LoginScreen> {
                            LaunchedEffect(Unit) {
                                dashBoardViewModel.appPreferences.getLoginStatusFlow()
                                    .collectLatest {
                                        if (dashBoardViewModel.appPreferences.isUserLoggedIn()) {
                                            onboardingNavigator.onLoggedIn()
                                        }
                                    }
                            }
                            LoginScreen(onLoginFinished = {
                                //Handled above in LaunchedEffect
                            })
                        }
                        entry<NavigationItem.OnboardingScreens.PermissionScreen> {
                            PermissionScreen(
                                onExit = {
                                    onboardingNavigator.next()
                                },
                                onExitAfterGrantingAllPermissions = {
                                    onboardingNavigator.onPermissionsGranted()
                                })
                        }
                        entry<NavigationItem.OnboardingScreens.ListeningAppScreen> {
                            ListeningAppSelectionScreen(
                                onClickNext = {
                                    onboardingNavigator.next()
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
                                        dashBoardViewModel.resetOnboardingCompleted()
                                        onboardingNavigator.restart()
                                    },
                                    checkForUpdates = {
                                        appUpdatesViewModel.checkForUpdates(
                                            activity = this@MainActivity
                                        )
                                    },
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
                PlayStoreUpdateReadyDialog(appUpdatesViewModel)
                PlayStoreUpdateAvailableDialog(appUpdatesViewModel)
            }

        }
    }

    // Sets Ui mode for XML layouts.
    private fun setUiMode() {
        lifecycleScope.launch {
            when (dashBoardViewModel.appPreferences.themePreference.get()) {
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

    @Composable
    fun SetStatusAndNavigationBarTheme(backStack: NavBackStack<NavKey>) {
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::backStack.isInitialized) {
            outState.putBundle(
                BACK_STACK_KEY,
                encodeToSavedState(navBackStackSerializer, backStack, navigationSavedStateConfiguration)
            )
        }
    }

    private fun setUpNavigation(backStack: NavBackStack<NavKey>) {
        this.backStack = backStack
        onboardingNavigator = OnboardingNavigator(
            backStack = backStack,
            state = { checkNotNull(dashBoardViewModel.onboardingState.value) },
            onOnboardingCompleted = dashBoardViewModel::markOnboardingCompleted
        )
        isNavigationReady = true
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

private const val BACK_STACK_KEY = "back_stack"
