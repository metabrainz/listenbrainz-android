package org.listenbrainz.shared.model

import org.jetbrains.compose.resources.DrawableResource
import org.listenbrainz.shared.resources.Res
import org.listenbrainz.shared.resources.explore_selected
import org.listenbrainz.shared.resources.explore_unselected
import org.listenbrainz.shared.resources.feed_selected
import org.listenbrainz.shared.resources.feed_unselected
import org.listenbrainz.shared.resources.ic_album
import org.listenbrainz.shared.resources.ic_artist
import org.listenbrainz.shared.resources.ic_info
import org.listenbrainz.shared.resources.ic_queue_music
import org.listenbrainz.shared.resources.ic_search
import org.listenbrainz.shared.resources.ic_settings
import org.listenbrainz.shared.resources.profile_selected
import org.listenbrainz.shared.resources.profile_unselected

/**
 * Represents navigation destinations in the app.
 * 
 */
sealed class AppNavigationItem(
    val route: String,
    val iconUnselected: DrawableResource,
    val iconSelected: DrawableResource,
    val title: String
) {
    data object Explore : AppNavigationItem("explore", Res.drawable.explore_unselected, Res.drawable.explore_selected, "Explore")
    data object Profile : AppNavigationItem("profile", Res.drawable.profile_unselected, Res.drawable.profile_selected, "Profile") {
        const val ARG_USERNAME = "username"

        fun withUserArg(username: String): String {
            return "$route?$ARG_USERNAME=$username"
        }
    }
    data object Feed : AppNavigationItem("feed", Res.drawable.feed_unselected, Res.drawable.feed_selected, "Feed")
    data object Settings : AppNavigationItem("settings", Res.drawable.ic_settings, Res.drawable.ic_settings, "Settings")
    data object About : AppNavigationItem("about", Res.drawable.ic_info, Res.drawable.ic_info, "About")
    data object Artist : AppNavigationItem("artist", Res.drawable.ic_artist, Res.drawable.ic_artist, "Artist")
    data object Album : AppNavigationItem("album", Res.drawable.ic_album, Res.drawable.ic_album, "Artist > Album")
    data object PlaylistScreen : AppNavigationItem("playlist", Res.drawable.ic_queue_music, Res.drawable.ic_queue_music, "Playlist")
    data object SearchScreen: AppNavigationItem("search", Res.drawable.ic_search, Res.drawable.ic_search,"Search")
}
