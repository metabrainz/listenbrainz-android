package org.listenbrainz.android.ui.screens.listens

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.components.ListenCard
import org.listenbrainz.android.ui.navigation.AppNavigationItem
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.LBSharedPreferences.username
import org.listenbrainz.android.viewmodel.ListensViewModel

@Composable
fun ListensScreen(
    navController: NavController
) {
    val viewModel: ListensViewModel = hiltViewModel()
    
    ListenBrainzTheme {
        
        DisposableEffect(Unit) {
            viewModel.connect()
            
            onDispose {
                SpotifyAppRemote.disconnect(viewModel.spotifyAppRemote)
            }
        }
        
        // Content
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


@Composable
fun NowPlaying(
    playerState: PlayerState?,
    bitmap: Bitmap?
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .height(180.dp)
            .clickable(onClick = {
                //onItemClicked(listen)
            }),
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.onSurface
    ) {
        Row(
            modifier = Modifier
            .padding(16.dp)
        ) {
            Text(
                text = "Now playing",
                modifier = Modifier.padding(4.dp),
                color = MaterialTheme.colors.surface,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.subtitle1,
                textAlign = TextAlign.Center,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(top = 40.dp)
        ) {
            val painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(data = bitmap)
                    .placeholder(R.drawable.ic_coverartarchive_logo_no_text)
                    .error(R.drawable.ic_coverartarchive_logo_no_text)
                    .build()
            )

            Image(
                modifier = Modifier
                    .size(80.dp, 80.dp)
                    .clip(RoundedCornerShape(16.dp)),
                painter = painter,
                alignment = Alignment.CenterStart,
                contentDescription = "",
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                playerState?.track?.name?.let { track ->
                    Text(
                        text = track,

                        modifier = Modifier.padding(0.dp, 0.dp, 12.dp, 0.dp),
                        color = MaterialTheme.colors.surface,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.subtitle1,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = buildString {
                        append(playerState?.track?.artist?.name)
                    },
                    modifier = Modifier.padding(0.dp, 0.dp, 12.dp, 0.dp),
                    color = MaterialTheme.colors.surface,
                    style = MaterialTheme.typography.caption,
                    maxLines = 2
                )

                Row(verticalAlignment = Alignment.Bottom) {
                    playerState?.track?.album?.name?.let { album ->
                        Text(
                            text = album,
                            modifier = Modifier.padding(0.dp, 12.dp, 12.dp, 0.dp),
                            color = MaterialTheme.colors.surface,
                            style = MaterialTheme.typography.caption,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AllUserListens(
    modifier: Modifier = Modifier,
    viewModel: ListensViewModel,
    navController: NavController
) {
    if(username == ""){
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

    username?.let { viewModel.fetchUserListens(userName = it) }

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

@Composable
fun Loader() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.headphone_meb_loading))
    LottieAnimation(composition)
}