package org.listenbrainz.android.ui.screens.listens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spotify.android.appremote.api.SpotifyAppRemote
import org.listenbrainz.android.R
import org.listenbrainz.android.model.Listen
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.components.LoadingAnimation
import org.listenbrainz.android.ui.screens.profile.UserData
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Utils.getCoverArtUrl
import org.listenbrainz.android.viewmodel.ListensViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListensScreen(
    viewModel: ListensViewModel = hiltViewModel(),
    spotifyClientId: String = stringResource(id = R.string.spotifyClientId),
    scrollRequestState: Boolean,
    onScrollToTop: (suspend () -> Unit) -> Unit,
) {
    DisposableEffect(Unit) {
        viewModel.connect(spotifyClientId = spotifyClientId)
        onDispose {
            SpotifyAppRemote.disconnect(viewModel.spotifyAppRemote)
        }
    }

    LaunchedEffect(Unit){
        viewModel.appPreferences.username.let {username ->
            if (username != null) {
                viewModel.fetchUserListens(userName = username)
            }
        }
    }

    val listState = rememberLazyListState()

    // Scroll to the top when shouldScrollToTop becomes true
    LaunchedEffect(scrollRequestState) {
        onScrollToTop {
            listState.scrollToItem(0)
        }
    }

    fun onListenTap(listen: Listen) {
        if (listen.trackMetadata.additionalInfo?.spotifyId != null) {
            Uri.parse(listen.trackMetadata.additionalInfo.spotifyId).lastPathSegment?.let { trackId ->
                viewModel.playUri("spotify:track:${trackId}")
            }
        } else {
            // Execute the API request asynchronously
            viewModel.playFromYoutubeMusic(
                listen.trackMetadata.trackName,
                listen.trackMetadata.artistName
            )
        }
    }


    // Listens list
    val listens = viewModel.listensFlow.collectAsState().value
    val listeningNow = viewModel.listeningNow.collectAsState().value

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        var showBlacklist by remember { mutableStateOf(false) }

        LazyColumn(state = listState) {
            item {
                UserData(
                    viewModel = viewModel
                )
            }

            item {
                val pagerState = rememberPagerState()

                HorizontalPager(state = pagerState, pageCount = 2, modifier = Modifier.fillMaxSize()) { page ->
                    when (page) {
                        0 -> {
                            AnimatedVisibility(visible = viewModel.listeningNow.collectAsState().value != null) {
                                ListeningNowCard(
                                    listeningNow!!,
                                    getCoverArtUrl(
                                        caaReleaseMbid = listeningNow.trackMetadata.mbidMapping?.caaReleaseMbid,
                                        caaId = listeningNow.trackMetadata.mbidMapping?.caaId
                                    )
                                ) {
                                    onListenTap(listeningNow)
                                }
                            }
                        }

                        1 -> {
                            AnimatedVisibility(visible = viewModel.playerState?.track?.name != null) {
                                ListeningNowOnSpotify(
                                    playerState = viewModel.playerState,
                                    bitmap = viewModel.bitmap
                                )
                            }
                        }
                    }
                }
            }

            items(items = listens) { listen ->
                ListenCardSmall(
                    modifier = Modifier.padding(
                        horizontal = ListenBrainzTheme.paddings.horizontal,
                        vertical = ListenBrainzTheme.paddings.lazyListAdjacent
                    ),
                    releaseName = listen.trackMetadata.trackName,
                    artistName = listen.trackMetadata.artistName,
                    coverArtUrl = getCoverArtUrl(
                        caaReleaseMbid = listen.trackMetadata.mbidMapping?.caaReleaseMbid,
                        caaId = listen.trackMetadata.mbidMapping?.caaId
                    )
                ) {
                    onListenTap(listen)
                }
            }
        }

        // Loading Animation
        AnimatedVisibility(
            visible = viewModel.isLoading,
            enter = fadeIn(initialAlpha = 0.4f),
            exit = fadeOut(animationSpec = tween(durationMillis = 250))
        ) {
            LoadingAnimation()
        }

        // BlackList Dialog
        if (showBlacklist) {
            ListeningAppsList(viewModel = viewModel) { showBlacklist = false }
        }
        
        // FAB
        // FIXME: MOVE ACCESS OF SHARED PREFERENCES TO COROUTINES.
        if(viewModel.appPreferences.isNotificationServiceAllowed) {
            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp), visible = !showBlacklist
            ) {
                FloatingActionButton(
                    modifier = Modifier.border(1.dp, Color.Gray, shape = CircleShape),
                    shape = CircleShape,
                    onClick = { showBlacklist = true },
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = "Blacklist"
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ListensScreenPreview() {
    ListensScreen(onScrollToTop = {}, scrollRequestState = false)
}
