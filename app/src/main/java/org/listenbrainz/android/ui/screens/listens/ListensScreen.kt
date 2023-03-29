package org.listenbrainz.android.ui.screens.listens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.spotify.android.appremote.api.SpotifyAppRemote
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.LBSharedPreferences
import org.listenbrainz.android.viewmodel.ListensViewModel

@Composable
fun ListensScreen(
    navController: NavController,
    viewModel: ListensViewModel = hiltViewModel(),
    spotifyClientId: String = stringResource(id = R.string.spotifyClientId)
) {
    ListenBrainzTheme {
        
        DisposableEffect(Unit) {
            
            viewModel.connect(spotifyClientId = spotifyClientId)

            onDispose {
                SpotifyAppRemote.disconnect(viewModel.spotifyAppRemote)
            }
        }

        LaunchedEffect(Unit){
            LBSharedPreferences.username?.let {
                viewModel.fetchUserListens(userName = it)
            }
        }

        Column {
            if (viewModel.playerState?.track?.name != null) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(initialAlpha = 0.4f),
                    exit = fadeOut(animationSpec = tween(durationMillis = 250))
                ) {
                    NowPlaying(
                        playerState = viewModel.playerState,
                        bitmap = viewModel.bitmap
                    )
                }
            }

            AllUserListens(
                viewModel = viewModel,
                navController = navController
            )
        }
    }
}

@Preview
@Composable
fun ListensScreenPreview() {
    ListensScreen(navController = NavController(LocalContext.current))
}