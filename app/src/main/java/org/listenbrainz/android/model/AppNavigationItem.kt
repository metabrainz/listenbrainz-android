package org.listenbrainz.android.model

import androidx.annotation.DrawableRes
import org.listenbrainz.android.R

sealed class AppNavigationItem(val route: String, @DrawableRes val iconUnselected: Int, @DrawableRes val iconSelected: Int, val title: String) {
    object BrainzPlayer : AppNavigationItem("brainzplayer", R.drawable.player_unselected, R.drawable.player_selected, "Player")
    object Explore : AppNavigationItem("explore", R.drawable.explore_unselected, R.drawable.explore_selected, "Explore")
    object Profile : AppNavigationItem("profile", R.drawable.profile_unselected, R.drawable.profile_selected, "Profile")
    object Feed : AppNavigationItem("feed", R.drawable.feed_unselected, R.drawable.feed_selected, "Feed")
    object Settings: AppNavigationItem("settings", R.drawable.ic_settings, R.drawable.ic_settings, "Settings")
    object About: AppNavigationItem("about", R.drawable.ic_info, R.drawable.ic_info, "About")
}

