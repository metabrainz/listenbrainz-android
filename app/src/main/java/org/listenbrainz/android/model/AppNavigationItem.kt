package org.listenbrainz.android.model

import org.listenbrainz.android.R

sealed class AppNavigationItem(var route: String, var iconUnselected: Int, var iconSelected: Int, var title: String) {
    object Home : AppNavigationItem("home", R.drawable.house_regular, R.drawable.house_solid, "Home")
    object BrainzPlayer : AppNavigationItem("brainzplayer", R.drawable.headphone_regular, R.drawable.headphones_solid, "Player")
    object Explore : AppNavigationItem("explore", R.drawable.music_regular, R.drawable.music_solid, "Explore")
    object Profile : AppNavigationItem("profile", R.drawable.user_regular, R.drawable.user_solid, "Profile")
    object Search : AppNavigationItem("search", R.drawable.ic_search, R.drawable.ic_search, "Search")
}

