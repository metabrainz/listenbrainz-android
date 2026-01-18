package org.listenbrainz.shared

import org.listenbrainz.shared.model.AppNavigationItem

object BottomNavDefaults {
    fun items(): List<AppNavigationItem> = listOf(
        AppNavigationItem.Feed,
        AppNavigationItem.BrainzPlayer,
        AppNavigationItem.Explore,
        AppNavigationItem.Profile
    )
}