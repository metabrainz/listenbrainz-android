package org.listenbrainz.android.ui.screens.main

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import org.listenbrainz.android.ui.navigation.NavigationItem
import org.listenbrainz.android.ui.screens.onboarding.auth.CreateAccountWebView
import org.listenbrainz.android.ui.screens.onboarding.auth.ListenBrainzLogin
import org.listenbrainz.android.ui.screens.onboarding.auth.LoginConsentScreen
import org.listenbrainz.android.ui.screens.onboarding.auth.OnboardingLoginScreen
import org.listenbrainz.android.ui.screens.onboarding.introduction.IntroductionScreens
import org.listenbrainz.android.ui.screens.onboarding.permissions.PermissionScreen
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.DashBoardViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var _dashBoardViewModel: DashBoardViewModel
    private val dashBoardViewModel get() = _dashBoardViewModel

    private val onboardingScreensQueue: MutableList<NavKey> =
        mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        _dashBoardViewModel = ViewModelProvider(this)[DashBoardViewModel::class.java]

        dashBoardViewModel.setUiMode()
        dashBoardViewModel.getPermissionStatus(this)

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
                        entry<NavigationItem.OnboardingScreens.LoginConsentScreen>{
                            LoginConsentScreen {
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
                                            backStack.remove(NavigationItem.OnboardingScreens.LoginScreen)
                                            onNavigateInOnboarding(backStack, dashBoardViewModel)
                                        }
                                    }
                            }
                            OnboardingLoginScreen(
                                onLoginClick = {
                                    backStack.add(NavigationItem.ListenBrainzLogin)
                                },
                                onCreateAccountClick = {
                                    backStack.add(NavigationItem.MusicBranizCreateAccount)
                                })
                        }
                        entry<NavigationItem.OnboardingScreens.PermissionScreen> {
                            PermissionScreen(onExit = {
                                onNavigateInOnboarding(
                                    backStack,
                                    dashBoardViewModel
                                )
                            })
                        }
                        entry<NavigationItem.HomeScreen> {
                            HomeScreen()
                        }
                        entry<NavigationItem.ListenBrainzLogin> {
                            Box(
                                Modifier
                                    .statusBarsPadding()
                                    .navigationBarsPadding()
                            ) {
                                ListenBrainzLogin {
                                    backStack.remove(NavigationItem.ListenBrainzLogin)
                                }
                            }
                        }
                        entry<NavigationItem.MusicBranizCreateAccount> {
                            CreateAccountWebView {
                                backStack.remove(NavigationItem.MusicBranizCreateAccount)
                                Toast.makeText(this@MainActivity, "Account Created!! Please verify email id", Toast.LENGTH_SHORT).show()
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
                    )
                )
            }
            if (dashBoardViewModel.appPreferences.isUserLoggedIn()) {
                onboardingScreensQueue.remove(NavigationItem.OnboardingScreens.LoginScreen)
            } else {
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
        dashBoardViewModel.getPermissionStatus(this)
        lifecycleScope.launch {
            App.startListenService(appPreferences = dashBoardViewModel.appPreferences)
        }
    }
}
