package org.listenbrainz.android.presentation.features.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import org.listenbrainz.android.R

sealed class NavigationItem(var route: String, var icon: Int, var title: String) {
    object Home : NavigationItem("home", R.drawable.ic_house, "Home")
    object BrainzPlayer : NavigationItem("brainzplayer", R.drawable.ic_brainzplayer_icon, "Player")
    object Listens : NavigationItem("listens", R.drawable.ic_listen, "Listens")
    object Profile : NavigationItem("profile", R.drawable.user, "Profile")
}

sealed class BrainzNavigationItem(var route: String, var icon: ImageVector, var title: String){
    object Home : BrainzNavigationItem("home", Icons.Rounded.Home, "Home")
    object Songs : BrainzNavigationItem("songs", Icons.Rounded.MusicNote, "Songs")
    object Artists : BrainzNavigationItem("artists", Icons.Rounded.Person, "Artists")
    object Albums : BrainzNavigationItem("albums", Icons.Rounded.Album, "Albums")
    object Playlists : BrainzNavigationItem("playlists", Icons.Rounded.QueueMusic, "Playlists")
}
