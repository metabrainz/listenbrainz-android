package org.listenbrainz.android.ui.screens.listens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import org.listenbrainz.android.ui.components.ListenCard
import org.listenbrainz.android.ui.components.Loader
import org.listenbrainz.android.ui.navigation.AppNavigationItem
import org.listenbrainz.android.util.LBSharedPreferences
import org.listenbrainz.android.viewmodel.ListensViewModel

@Composable
fun AllUserListens(
    modifier: Modifier = Modifier,
    viewModel: ListensViewModel,
    navController: NavController
) {
    if(LBSharedPreferences.username == "") {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = {
                    navController.navigate(AppNavigationItem.Profile.route)
                })
                { Text(text = "OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    navController.popBackStack()
                })
                { Text(text = "Cancel") }
            },
            title = { Text(text = "Please login to your profile") },
            text = { Text(text = "We will fetch your listens once you have logged in") }
        )
        return
    }

    AnimatedVisibility(
        visible = viewModel.isLoading,
        enter = fadeIn(initialAlpha = 0.4f),
        exit = fadeOut(animationSpec = tween(durationMillis = 250))
    ){
        Loader()
    }
    LazyColumn(modifier) {
        items(viewModel.listens) { listen->
            ListenCard(
                listen,
                coverArt = listen.coverArt,     // TODO: Fix coverArts not working
                onItemClicked = {
                    if(it.track_metadata.additional_info?.spotify_id != null) {
                        Uri.parse(it.track_metadata.additional_info.spotify_id).lastPathSegment?.let { trackId ->
                            viewModel.playUri("spotify:track:${trackId}")
                        }
                    }
                }
            )
        }
    }
}