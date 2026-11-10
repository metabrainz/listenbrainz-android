package org.listenbrainz.shared.onboarding

import androidx.navigation3.runtime.NavKey
import org.listenbrainz.shared.navigation.NavigationItem

/** Onboarding screens, in the order they are shown. */
enum class OnboardingStep(val key: NavigationItem.OnboardingScreens) {
    INTRODUCTION(NavigationItem.OnboardingScreens.IntroductionScreen),
    LOGIN(NavigationItem.OnboardingScreens.LoginScreen),
    PERMISSIONS(NavigationItem.OnboardingScreens.PermissionScreen),
    LISTENING_APPS(NavigationItem.OnboardingScreens.ListeningAppScreen)
}

data class OnboardingState(
    val isOnboardingCompleted: Boolean,
    val isUserLoggedIn: Boolean,
    val isPermissionMissing: Boolean
)

/** The steps that still apply, in the order they are shown. */
fun OnboardingState.pendingSteps(): List<OnboardingStep> = OnboardingStep.entries.filter { step ->
    when (step) {
        OnboardingStep.LOGIN -> !isUserLoggedIn
        OnboardingStep.PERMISSIONS -> !isOnboardingCompleted && isPermissionMissing
        OnboardingStep.INTRODUCTION,
        OnboardingStep.LISTENING_APPS -> !isOnboardingCompleted
    }
}

fun OnboardingState.startDestination(): NavigationItem =
    pendingSteps().firstOrNull()?.key ?: NavigationItem.HomeScreen

/**
 * Moves through onboarding on [backStack]. The next screen is always derived from the screen on
 * top of the back stack and the current [state], so nothing besides the back stack needs saving.
 */
class OnboardingNavigator(
    private val backStack: MutableList<NavKey>,
    private val state: () -> OnboardingState,
    private val onOnboardingCompleted: () -> Unit
) {

    private val stepOf: Map<NavKey, OnboardingStep> = OnboardingStep.entries.associateBy { it.key }

    /** Shows the pending step after the current screen, or completes onboarding. */
    fun next() = next(state())

    /** Starts onboarding again on top of the current back stack. */
    fun restart() {
        backStack.add(state().copy(isOnboardingCompleted = false).startDestination())
    }

    fun onLoggedIn() {
        backStack.remove(OnboardingStep.LOGIN.key)
        // [state] may lag behind the stored login
        next(state().copy(isUserLoggedIn = true))
    }

    fun onPermissionsGranted() {
        next(state().copy(isPermissionMissing = false))
        backStack.remove(OnboardingStep.PERMISSIONS.key)
    }

    private fun next(state: OnboardingState) {
        val current = backStack.lastOrNull()?.let(stepOf::get)
        val next = state.pendingSteps().firstOrNull { current == null || it > current }
        if (next != null) {
            backStack.add(next.key)
            return
        }

        onOnboardingCompleted()
        // Return to where onboarding was started from, if anywhere
        if (backStack.all { it in stepOf }) {
            backStack.add(NavigationItem.HomeScreen)
        }
        backStack.removeAll { it in stepOf }
    }
}
