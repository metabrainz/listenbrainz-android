package org.listenbrainz.android.ui.screens.brainzplayer.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BrainzPlayerNavigationItem(var route: String, var icon: ImageVector, var title: String){
    object Home : BrainzPlayerNavigationItem("home", Icons.Rounded.Home, "Home")
    object Songs : BrainzPlayerNavigationItem("songs", Icons.Rounded.MusicNote, "Songs")
    object Artists : BrainzPlayerNavigationItem("artists", Icons.Rounded.Person, "Artists")
    object Albums : BrainzPlayerNavigationItem("albums", Icons.Rounded.Album, "Albums")
    object Playlists : BrainzPlayerNavigationItem("playlists", Icons.Rounded.QueueMusic, "Playlists")
}