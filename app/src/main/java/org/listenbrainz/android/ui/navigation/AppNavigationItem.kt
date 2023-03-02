package org.listenbrainz.android.ui.navigation

import org.listenbrainz.android.R

sealed class AppNavigationItem(var route: String, var icon: Int, var title: String) {
    object Home : AppNavigationItem("home", R.drawable.ic_house, "Home")
    object BrainzPlayer : AppNavigationItem("brainzplayer", R.drawable.ic_brainzplayer_icon, "Player")
    object Listens : AppNavigationItem("listens", R.drawable.ic_listen, "Listens")
    object Profile : AppNavigationItem("profile", R.drawable.user, "Profile")
}

