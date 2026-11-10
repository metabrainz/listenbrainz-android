package org.listenbrainz.shared.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface NavigationItem :NavKey{

    @Serializable
    sealed interface OnboardingScreens: NavigationItem {
        @Serializable
        data object IntroductionScreen : OnboardingScreens

        @Serializable
        data object PermissionScreen : OnboardingScreens

        /* Temporary token based login */
        @Serializable
        data object TempLoginScreen : OnboardingScreens

        @Serializable
        data object ListeningAppScreen : OnboardingScreens
    }

    @Serializable
    data object HomeScreen: NavigationItem}