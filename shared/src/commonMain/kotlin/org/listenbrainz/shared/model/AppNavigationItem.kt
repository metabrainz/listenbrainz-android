package org.listenbrainz.shared.model

/**
 * Represents navigation destinations in the app.
 * Icon resolution is handled by each platform via string IDs.
 */
sealed class AppNavigationItem(
    val route: String,
    val iconUnselectedId: String,
    val iconSelectedId: String,
    val title: String
) {
    data object BrainzPlayer : AppNavigationItem("brainzplayer", "player_unselected", "player_selected", "Player")
    data object Explore : AppNavigationItem("explore", "explore_unselected", "explore_selected", "Explore")
    data object Profile : AppNavigationItem("profile", "profile_unselected", "profile_selected", "Profile") {
        const val ARG_USERNAME = "username"

        fun withUserArg(username: String): String {
            return "$route?$ARG_USERNAME=$username"
        }
    }
    data object Feed : AppNavigationItem("feed", "feed_unselected", "feed_selected", "Feed")
    data object Settings : AppNavigationItem("settings", "ic_settings", "ic_settings", "Settings")
    data object About : AppNavigationItem("about", "ic_info", "ic_info", "About")
    data object Artist : AppNavigationItem("artist", "ic_artist", "ic_artist", "Artist")
    data object Album : AppNavigationItem("album", "ic_album", "ic_album", "Artist > Album")
    data object PlaylistScreen : AppNavigationItem("playlist", "ic_queue_music", "ic_queue_music", "Playlist")
}
