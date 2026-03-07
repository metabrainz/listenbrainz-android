package org.listenbrainz.android.ui.navigation

import org.listenbrainz.android.model.AppNavigationItem

object BottomNavDefaults {
    fun items(): List<AppNavigationItem> = listOf(
        AppNavigationItem.Feed,
        AppNavigationItem.BrainzPlayer,
        AppNavigationItem.Explore,
        AppNavigationItem.Profile
    )
}
