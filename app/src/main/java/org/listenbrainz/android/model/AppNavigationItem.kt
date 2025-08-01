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
    object Artist: AppNavigationItem("artist", R.drawable.ic_artist, R.drawable.ic_artist,"Artist")
    object Album: AppNavigationItem("album", R.drawable.ic_album, R.drawable.ic_album, "Artist > Album")
    object PlaylistScreen: AppNavigationItem("playlist", R.drawable.ic_queue_music, R.drawable.ic_queue_music, "Playlist")
    object HueSound: AppNavigationItem("HueSound", R.drawable.ic_album, R.drawable.ic_album, "HueSound")
}

