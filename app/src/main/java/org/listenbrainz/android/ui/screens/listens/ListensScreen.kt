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
import org.listenbrainz.android.model.TrackMetadata
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.components.LoadingAnimation
import org.listenbrainz.android.ui.screens.profile.UserData
import org.listenbrainz.android.ui.screens.settings.PreferencesUiState
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Utils
import org.listenbrainz.android.viewmodel.ListensViewModel

@Composable
fun ListensScreen(
    viewModel: ListensViewModel = hiltViewModel(),
    scrollRequestState: Boolean,
    onScrollToTop: (suspend () -> Unit) -> Unit,
) {
    
    val uiState by viewModel.uiState.collectAsState()
    val preferencesUiState by viewModel.preferencesUiState.collectAsState()
    
    ListensScreen(
        scrollRequestState = scrollRequestState,
        onScrollToTop = onScrollToTop,
        uiState = uiState,
        preferencesUiState = preferencesUiState,
        updateNotificationServicePermissionStatus = {
            viewModel.updateNotificationServicePermissionStatus()
        },
        validateUserToken = { token ->
            viewModel.validateUserToken(token)
        },
        setToken = {
            viewModel.setAccessToken(it)
        },
        playListen = {
            viewModel.playListen(it)
        }
    )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListensScreen(
    scrollRequestState: Boolean,
    onScrollToTop: (suspend () -> Unit) -> Unit,
    uiState: ListensUiState,
    preferencesUiState: PreferencesUiState,
    updateNotificationServicePermissionStatus: () -> Unit,
    validateUserToken: suspend (String) -> Boolean,
    setToken: (String) -> Unit,
    playListen: (TrackMetadata) -> Unit
) {
    val listState = rememberLazyListState()
    
    // Scroll to the top when shouldScrollToTop becomes true
    LaunchedEffect(scrollRequestState) {
        onScrollToTop {
            listState.scrollToItem(0)
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        
        LazyColumn(state = listState) {
            item {
                UserData(
                    preferencesUiState,
                    updateNotificationServicePermissionStatus,
                    validateUserToken,
                    setToken
                )
            }
            
            item {
                val pagerState = rememberPagerState { 1 }
                
                // TODO: Figure out the use of ListeningNowOnSpotify. It is hidden for now
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    when (page) {
                        0 -> {
                            uiState.listeningNowUiState.listeningNow.let { listeningNow ->
                                ListeningNowCard(
                                    listeningNow,
                                    Utils.getCoverArtUrl(
                                        caaReleaseMbid = listeningNow?.trackMetadata?.mbidMapping?.caaReleaseMbid,
                                        caaId = listeningNow?.trackMetadata?.mbidMapping?.caaId
                                    )
                                ) {
                                    listeningNow?.let { listen -> playListen(listen.trackMetadata) }
                                }
                            }
                        }
                        
                        1 -> {
                            AnimatedVisibility(
                                visible = uiState.listeningNowUiState.playerState?.track?.name != null,
                                enter = slideInVertically(),
                                exit = slideOutVertically()
                            ) {
                                ListeningNowOnSpotify(
                                    playerState = uiState.listeningNowUiState.playerState,
                                    bitmap = uiState.listeningNowUiState.listeningNowBitmap
                                )
                            }
                        }
                    }
                }
            }
            
            items(items = uiState.listens) { listen ->
                ListenCardSmall(
                    modifier = Modifier.padding(
                        horizontal = ListenBrainzTheme.paddings.horizontal,
                        vertical = ListenBrainzTheme.paddings.lazyListAdjacent
                    ),
                    trackName = listen.trackMetadata.trackName,
                    artistName = listen.trackMetadata.artistName,
                    coverArtUrl = Utils.getCoverArtUrl(
                        caaReleaseMbid = listen.trackMetadata.mbidMapping?.caaReleaseMbid,
                        caaId = listen.trackMetadata.mbidMapping?.caaId
                    )
                ) {
                    playListen(listen.trackMetadata)
                }
            }
        }
        
        // Loading Animation
        AnimatedVisibility(
            visible = uiState.isLoading,
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
    ListensScreen(
        onScrollToTop = {},
        scrollRequestState = false,
        updateNotificationServicePermissionStatus = {},
        uiState = ListensUiState(),
        preferencesUiState = PreferencesUiState(),
        validateUserToken = { true },
        setToken = {},
        playListen = {}
    )
}
