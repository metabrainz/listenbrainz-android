package org.listenbrainz.android.presentation.features.yim.navigation

enum class YimScreens {
    YimHomeScreen,
    YimMainScreen;
    companion object {
        fun fromRoute(route: String?): YimScreens =
            when (route?.substringBefore("/")) {
                YimHomeScreen.name -> YimHomeScreen
                YimMainScreen.name -> YimMainScreen
                null -> YimHomeScreen
                else -> throw java.lang.IllegalArgumentException("Route $route invalid.")
            }
    }
}