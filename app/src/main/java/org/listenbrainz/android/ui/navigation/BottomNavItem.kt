package org.listenbrainz.android.ui.navigation

import org.listenbrainz.android.model.AppNavigationItem

enum class BottomNavItem(
    val appNav: AppNavigationItem,
    val navId: String
) {
    FEED(AppNavigationItem.Feed, "feed"),
    EXPLORE(AppNavigationItem.Explore, "explore"),
    PLAYER(AppNavigationItem.BrainzPlayer, "player"),
    PROFILE(AppNavigationItem.Profile, "profile");
}
