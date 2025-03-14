package org.listenbrainz.android.ui.screens.playlist

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import org.listenbrainz.android.viewmodel.PlaylistViewModel
import org.listenbrainz.android.viewmodel.SocialViewModel

@Composable
fun PlaylistDetailScreen(
    playlistMBID: String,
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    socialViewModel: SocialViewModel = hiltViewModel(),
    snackbarState: SnackbarHostState,
    goToArtistPage: (String) -> Unit
){

}

@Composable
private fun PlaylistDetailContent(
    goToArtistPage: (String) -> Unit
){

}