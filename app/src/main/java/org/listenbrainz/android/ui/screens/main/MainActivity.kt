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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.navigation3.runtime.serialization.NavKeySerializer
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.listenbrainz.android.service.ListenServiceState
import org.listenbrainz.android.application.App
import org.listenbrainz.shared.model.UiMode
import org.listenbrainz.android.ui.components.OnboardingScreenBackground
import org.listenbrainz.android.ui.navigation.NavigationItem
import org.listenbrainz.android.ui.navigation.TopBarActions
import org.listenbrainz.android.ui.screens.appupdates.AppUpdateDialog
import org.listenbrainz.android.ui.screens.appupdates.InstallAppDialog
import org.listenbrainz.android.ui.screens.appupdates.InstallPermissionRationaleDialog
import org.listenbrainz.android.ui.screens.appupdates.PlayStoreUpdateAvailableDialog
import org.listenbrainz.android.ui.screens.appupdates.PlayStoreUpdateReadyDialog
import org.listenbrainz.android.ui.screens.onboarding.auth.createaccount.ListenBrainzCreateAccountScreen
import org.listenbrainz.android.ui.screens.onboarding.auth.templogin.TempLoginScreen
import org.listenbrainz.android.ui.screens.onboarding.introduction.IntroductionScreens
import org.listenbrainz.android.ui.screens.onboarding.listeningApps.ListeningAppSelectionScreen
import org.listenbrainz.android.ui.screens.onboarding.permissions.PermissionScreen
import org.listenbrainz.android.ui.screens.settings.SettingsCallbacksToHomeScreen
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.AppUpdatesViewModel
import org.listenbrainz.android.viewmodel.DashBoardViewModel
import org.listenbrainz.shared.permission.PermissionManager
import org.listenbrainz.shared.permission.PermissionScope
import org.listenbrainz.shared.permission.missing
import java.lang.ref.WeakReference

class MainActivity : ComponentActivity() {

    private val dashBoardViewModel: DashBoardViewModel by viewModel()

    private val appUpdatesViewModel: AppUpdatesViewModel by viewModel()

    private val listenServiceState: ListenServiceState by inject()
    private val permissionManager: PermissionManager by inject()

    private lateinit var backStack: NavBackStack<NavKey>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        setUiMode()
        dashBoardViewModel.updateListeningApps(WeakReference(this))
        appUpdatesViewModel.checkForUpdatesDuringLaunch(this)
        backStack = savedInstanceState?.getBundle(BACK_STACK_KEY)
            ?.let { decodeFromSavedState(backStackSerializer, it) }
            ?: NavBackStack(
                pendingOnboardingSteps(refreshPermissions = true).firstOrNull()
                    ?: NavigationItem.HomeScreen
            )

        setContent {
            ListenBrainzTheme {
                DisposableEffect(Unit) {
                    dashBoardViewModel.connectToSpotify()
                    onDispose {
                        dashBoardViewModel.disconnectSpotify()
                    }
                }

                SetStatusAndNavigationBarTheme(backStack)
                OnboardingScreenBackground(backStack)
//                ConsentScreenDataInitializer(dashBoardViewModel)
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeAt(backStack.lastIndex) },
                    entryProvider = entryProvider {
                        entry<NavigationItem.OnboardingScreens.IntroductionScreen> {
                            IntroductionScreens {
                                navigateInOnboarding()
                            }
                        }
                        entry<NavigationItem.OnboardingScreens.TempLoginScreen> {
                            LaunchedEffect(Unit) {
                                dashBoardViewModel.appPreferences.getLoginStatusFlow()
                                    .collectLatest {
                                        if (dashBoardViewModel.appPreferences.isUserLoggedIn()) {
                                            backStack.remove(NavigationItem.OnboardingScreens.TempLoginScreen)
                                            navigateInOnboarding()
                                        }
                                    }
                            }
                            TempLoginScreen(onLoginFinished = {
                                //Handled above in LaunchedEffect
                            })
                        }
                        entry<NavigationItem.OnboardingScreens.PermissionScreen> {
                            PermissionScreen(
                                onExit = {
                                    navigateInOnboarding()
                                },
                                onExitAfterGrantingAllPermissions = {
                                    navigateInOnboarding()
                                    backStack.remove(NavigationItem.OnboardingScreens.PermissionScreen)
                                })
                        }
                        entry<NavigationItem.OnboardingScreens.ListeningAppScreen> {
                            ListeningAppSelectionScreen(
                                onClickNext = {
                                    navigateInOnboarding()
                                }
                            )
                        }
                        entry<NavigationItem.HomeScreen> {
                            HomeScreen(
                                settingsCallbacks = SettingsCallbacksToHomeScreen(
                                    onLoginRequest = {
                                        backStack.add(NavigationItem.OnboardingScreens.TempLoginScreen)
                                    },
                                    onOnboardingRequest = {
                                        dashBoardViewModel.resetOnboardingCompleted()
                                        backStack.add(
                                            pendingOnboardingSteps(onboardingCompleted = false).firstOrNull()
                                                ?: NavigationItem.HomeScreen
                                        )
                                    },
                                    checkForUpdates = {
                                        appUpdatesViewModel.checkForUpdates(
                                            activity = this@MainActivity
                                        )
                                    },
                                    navigateToCreateAccount = {
                                        backStack.add(NavigationItem.CreateAccountScreen)
                                    },
                                )
                            )
                        }
                        entry<NavigationItem.CreateAccountScreen> {
                            ListenBrainzCreateAccountScreen(){
                                backStack.remove(NavigationItem.CreateAccountScreen)
                            }
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
                if (backStack[backStack.lastIndex] is NavigationItem.OnboardingScreens || backStack[backStack.lastIndex] == NavigationItem.CreateAccountScreen) {
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
        outState.putBundle(BACK_STACK_KEY, encodeToSavedState(backStackSerializer, backStack))
    }

    /**
     * Onboarding screens that still apply, in the order they are shown.
     *
     * Blocks the main thread, so the next screen is known before the UI is displayed or updated.
     * @param onboardingCompleted overrides the stored value, which may not be written yet.
     */
    private fun pendingOnboardingSteps(
        onboardingCompleted: Boolean? = null,
        refreshPermissions: Boolean = false
    ): List<NavigationItem.OnboardingScreens> = runBlocking {
        val appPreferences = dashBoardViewModel.appPreferences
        val isOnboardingCompleted = onboardingCompleted ?: appPreferences.onboardingCompleted.get()
        val isUserLoggedIn = appPreferences.isUserLoggedIn()
        val statuses = if (refreshPermissions) permissionManager.refresh() else permissionManager.statuses.value
        val isPermissionMissing = statuses
            .missing(permissionManager.permissionsFor(PermissionScope.ONBOARDING))
            .isNotEmpty()

        onboardingSteps.filter { step ->
            when (step) {
                NavigationItem.OnboardingScreens.TempLoginScreen -> !isUserLoggedIn
                NavigationItem.OnboardingScreens.PermissionScreen -> !isOnboardingCompleted && isPermissionMissing
                NavigationItem.OnboardingScreens.IntroductionScreen,
                NavigationItem.OnboardingScreens.ListeningAppScreen -> !isOnboardingCompleted
                NavigationItem.OnboardingScreens.LoginConsentScreen,
                NavigationItem.OnboardingScreens.LoginScreen -> false
            }
        }
    }

    /** Shows the pending onboarding screen after the current one, or completes onboarding. */
    private fun navigateInOnboarding() {
        val currentIndex = onboardingSteps.indexOf(backStack.lastOrNull())
        val next = pendingOnboardingSteps().firstOrNull { onboardingSteps.indexOf(it) > currentIndex }
        if (next != null) {
            backStack.add(next)
            return
        }

        dashBoardViewModel.markOnboardingCompleted()
        // Onboarding started from inside the app returns to where it was, otherwise to home
        if (backStack.all { it is NavigationItem.OnboardingScreens }) {
            backStack.add(NavigationItem.HomeScreen)
        }
        backStack.removeAll { it is NavigationItem.OnboardingScreens }
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
private val backStackSerializer = NavBackStackSerializer(elementSerializer = NavKeySerializer())

private val onboardingSteps = listOf(
    NavigationItem.OnboardingScreens.IntroductionScreen,
    NavigationItem.OnboardingScreens.TempLoginScreen,
    NavigationItem.OnboardingScreens.PermissionScreen,
    NavigationItem.OnboardingScreens.ListeningAppScreen
)
