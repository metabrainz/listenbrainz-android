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

        @Serializable
        data object LoginScreen : OnboardingScreens

        @Serializable
        data object ListeningAppScreen : OnboardingScreens
    }

    @Serializable
    data object HomeScreen: NavigationItem

    @Serializable
    data object NewsBrainzScreen: NavigationItem

    @Serializable
    data class ArtistScreen(val mbid: String): NavigationItem

    @Serializable
    data class AlbumScreen(val mbid: String): NavigationItem
}