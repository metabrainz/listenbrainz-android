package org.listenbrainz.android.ui.screens.listens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
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
    scrollRequestState: Boolean,
    onScrollToTop: (suspend () -> Unit) -> Unit,
) {
    
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

    /** Content **/

    // Listens list
    val listens by viewModel.listensFlow.collectAsState()
    val listeningNow by viewModel.listeningNow.collectAsState()
    val playerState by viewModel.playerState.collectAsState(null)

    Box(modifier = Modifier.fillMaxSize()) {

        LazyColumn(state = listState) {
            item {
                UserData(
                    viewModel = viewModel
                )
            }

            item {
                val pagerState = rememberPagerState { 1 }

                // TODO: Figure out the use of ListeningNowOnSpotify. It is hidden for now
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    when (page) {
                        0 -> {
                            ListeningNowCard(
                                listeningNow,
                                getCoverArtUrl(
                                    caaReleaseMbid = listeningNow?.trackMetadata?.mbidMapping?.caaReleaseMbid,
                                    caaId = listeningNow?.trackMetadata?.mbidMapping?.caaId
                                )
                            ) {
                                listeningNow?.let { listen -> viewModel.playListen(listen) }
                            }
                        }

                        1 -> {
                            AnimatedVisibility(
                                visible = playerState?.track?.name != null,
                                enter = slideInVertically(),
                                exit = slideOutVertically()
                            ) {
                                ListeningNowOnSpotify(
                                    playerState = playerState,
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
                    trackName = listen.trackMetadata.trackName,
                    artistName = listen.trackMetadata.artistName,
                    coverArtUrl = getCoverArtUrl(
                        caaReleaseMbid = listen.trackMetadata.mbidMapping?.caaReleaseMbid,
                        caaId = listen.trackMetadata.mbidMapping?.caaId
                    )
                ) {
                    viewModel.playListen(listen)
                }
            }
        }

        // Loading Animation
        AnimatedVisibility(
            visible = viewModel.isLoading,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(initialAlpha = 0.4f),
            exit = fadeOut(animationSpec = tween(durationMillis = 250))
        ) {
            LoadingAnimation()
        }
    }
}

@Preview
@Composable
fun ListensScreenPreview() {
    ListensScreen(onScrollToTop = {}, scrollRequestState = false)
}
