package org.listenbrainz.android.ui.screens.brainzplayer.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BrainzNavigationItem(var route: String, var icon: ImageVector, var title: String){
    object Home : BrainzNavigationItem("home", Icons.Rounded.Home, "Home")
    object Songs : BrainzNavigationItem("songs", Icons.Rounded.MusicNote, "Songs")
    object Artists : BrainzNavigationItem("artists", Icons.Rounded.Person, "Artists")
    object Albums : BrainzNavigationItem("albums", Icons.Rounded.Album, "Albums")
    object Playlists : BrainzNavigationItem("playlists", Icons.Rounded.QueueMusic, "Playlists")
}