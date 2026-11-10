package org.listenbrainz.shared.onboarding

/** Onboarding screens, in the order they are shown. */
enum class OnboardingStep {
    INTRODUCTION,
    LOGIN,
    PERMISSIONS,
    LISTENING_APPS
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

/**
 * Moves through onboarding on [backStack]. The next screen is always derived from the screen on
 * top of the back stack and the current [state], so nothing besides the back stack needs saving.
 *
 * @param K the back stack's key type.
 * @param keyOf the key of each step's screen.
 */
class OnboardingNavigator<K : Any>(
    private val backStack: MutableList<K>,
    private val home: K,
    private val keyOf: (OnboardingStep) -> K,
    private val state: () -> OnboardingState,
    private val onOnboardingCompleted: () -> Unit
) {

    private val stepOf: Map<K, OnboardingStep> = OnboardingStep.entries.associateBy(keyOf)

    /** Shows the pending step after the current screen, or completes onboarding. */
    fun next() = next(state())

    /** Starts onboarding again on top of the current back stack. */
    fun restart() {
        val firstStep = state().copy(isOnboardingCompleted = false).pendingSteps().firstOrNull()
        backStack.add(firstStep?.let(keyOf) ?: home)
    }

    fun onLoggedIn() {
        backStack.remove(keyOf(OnboardingStep.LOGIN))
        // The stored login status may not have reached [state] yet
        next(state().copy(isUserLoggedIn = true))
    }

    fun onPermissionsGranted() {
        next(state().copy(isPermissionMissing = false))
        backStack.remove(keyOf(OnboardingStep.PERMISSIONS))
    }

    private fun next(state: OnboardingState) {
        val current = backStack.lastOrNull()?.let(stepOf::get)
        val next = state.pendingSteps().firstOrNull { current == null || it > current }
        if (next != null) {
            backStack.add(keyOf(next))
            return
        }

        onOnboardingCompleted()
        // Onboarding started from inside the app returns to where it was, otherwise to home
        if (backStack.all { it in stepOf }) {
            backStack.add(home)
        }
        backStack.removeAll { it in stepOf }
    }
}

/** Where a fresh back stack starts. */
fun <K : Any> OnboardingState.startDestination(home: K, keyOf: (OnboardingStep) -> K): K =
    pendingSteps().firstOrNull()?.let(keyOf) ?: home
