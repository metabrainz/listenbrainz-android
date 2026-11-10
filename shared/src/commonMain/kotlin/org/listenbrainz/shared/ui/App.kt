package org.listenbrainz.shared.ui

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import org.koin.compose.viewmodel.koinViewModel
import org.listenbrainz.shared.navigation.navigationSavedStateConfiguration
import org.listenbrainz.shared.onboarding.OnboardingNavigator
import org.listenbrainz.shared.onboarding.startDestination
import org.listenbrainz.shared.viewmodel.DashBoardViewModel

/** What screens use to navigate. */
class AppNavigator internal constructor(
    val backStack: NavBackStack<NavKey>,
    val onboarding: OnboardingNavigator
)

/**
 * The app's navigation host. Draws nothing until the onboarding state has loaded, so the first
 * screen is known before anything is shown.
 *
 * @param entries the screens, provided by the platform until they move to this module.
 * @param decorations drawn behind the screens, e.g. backgrounds and system bar styling.
 */
@Composable
fun App(
    entries: EntryProviderScope<NavKey>.(AppNavigator) -> Unit,
    decorations: @Composable (NavBackStack<NavKey>) -> Unit = {},
    dashBoardViewModel: DashBoardViewModel = koinViewModel()
) {
    val onboardingState by dashBoardViewModel.onboardingState.collectAsState()
    val startDestination = onboardingState?.startDestination() ?: return

    val backStack = rememberNavBackStack(navigationSavedStateConfiguration, startDestination)
    val navigator = remember(backStack) {
        AppNavigator(
            backStack = backStack,
            onboarding = OnboardingNavigator(
                backStack = backStack,
                state = { checkNotNull(dashBoardViewModel.onboardingState.value) },
                onOnboardingCompleted = dashBoardViewModel::markOnboardingCompleted
            )
        )
    }

    DisposableEffect(Unit) {
        dashBoardViewModel.connectToSpotify()
        onDispose {
            dashBoardViewModel.disconnectSpotify()
        }
    }

    decorations(backStack)
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeAt(backStack.lastIndex) },
        entryProvider = entryProvider { entries(navigator) },
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
