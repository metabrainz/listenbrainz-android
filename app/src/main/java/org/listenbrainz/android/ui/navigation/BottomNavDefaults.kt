package org.listenbrainz.android.ui.navigation

import org.listenbrainz.shared.model.AppNavigationItem

object BottomNavDefaults {
    fun items(): List<AppNavigationItem> = listOf(
        AppNavigationItem.Feed,
        AppNavigationItem.BrainzPlayer,
        AppNavigationItem.Explore,
        AppNavigationItem.Profile
    )
}
