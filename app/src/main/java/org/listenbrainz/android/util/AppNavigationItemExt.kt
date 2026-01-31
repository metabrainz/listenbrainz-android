package org.listenbrainz.android.util

import androidx.annotation.DrawableRes
import org.listenbrainz.android.R
import org.listenbrainz.shared.model.AppNavigationItem

/**
 * Extension to resolve drawable resource IDs for navigation icons on Android.
 */
@get:DrawableRes
val AppNavigationItem.iconUnselected: Int
    get() = iconMap[iconUnselectedId] ?: R.drawable.ic_info

@get:DrawableRes
val AppNavigationItem.iconSelected: Int
    get() = iconMap[iconSelectedId] ?: R.drawable.ic_info

private val iconMap = mapOf(
    "player_unselected" to R.drawable.player_unselected,
    "player_selected" to R.drawable.player_selected,
    "explore_unselected" to R.drawable.explore_unselected,
    "explore_selected" to R.drawable.explore_selected,
    "profile_unselected" to R.drawable.profile_unselected,
    "profile_selected" to R.drawable.profile_selected,
    "feed_unselected" to R.drawable.feed_unselected,
    "feed_selected" to R.drawable.feed_selected,
    "ic_settings" to R.drawable.ic_settings,
    "ic_info" to R.drawable.ic_info,
    "ic_artist" to R.drawable.ic_artist,
    "ic_album" to R.drawable.ic_album,
    "ic_queue_music" to R.drawable.ic_queue_music
)
