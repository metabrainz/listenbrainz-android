package org.listenbrainz.android.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed class NavigationItem() :NavKey{
    @Serializable
    data object IntroductionScreen: NavigationItem()

    @Serializable
    data object PermissionScreen: NavigationItem()

    @Serializable
    data object LoginScreen: NavigationItem()

    @Serializable
    data object ListeningAppScreen: NavigationItem()

    @Serializable
    data object HomeScreen: NavigationItem()
}