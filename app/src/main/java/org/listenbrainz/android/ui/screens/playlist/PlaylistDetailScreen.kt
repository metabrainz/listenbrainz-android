package org.listenbrainz.android.ui.screens.playlist

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.listenbrainz.android.model.playlist.PlaylistTrack
import org.listenbrainz.android.ui.components.ErrorBar
import org.listenbrainz.android.ui.components.ListenCardSmallDefault
import org.listenbrainz.android.ui.components.LoadingAnimation
import org.listenbrainz.android.ui.components.SuccessBar
import org.listenbrainz.android.ui.screens.feed.RetryButton
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Utils.formatDurationSeconds
import org.listenbrainz.android.util.Utils.getCoverArtUrl
import org.listenbrainz.android.viewmodel.PlaylistDataViewModel
import org.listenbrainz.android.viewmodel.SocialViewModel

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistMBID: String,
    playlistViewModel: PlaylistDataViewModel = hiltViewModel(),
    socialViewModel: SocialViewModel = hiltViewModel(),
    snackbarState: SnackbarHostState,
    goToArtistPage: (String) -> Unit
) {
    val uiState by playlistViewModel.uiState.collectAsState()
    val socialUiState by socialViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.playlistDetailUIState.isRefreshing,
        onRefresh = {
            playlistViewModel.getDataInPlaylistScreen(playlistMBID, isRefresh = true)
        }
    )
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    LaunchedEffect(Unit) {
        playlistViewModel.getDataInPlaylistScreen(playlistMBID)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        AnimatedContent(
            uiState.playlistDetailUIState.isLoading and !uiState.playlistDetailUIState.isRefreshing,
            modifier = Modifier.fillMaxSize()
        ) { isLoading ->
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        LoadingAnimation()
                    }

                } else {
                    if (uiState.playlistDetailUIState.playlistData != null) {
                        PlaylistDetailContent(
                            playlistDetailUIState = uiState.playlistDetailUIState,
                            goToArtistPage = goToArtistPage,
                            onTrackClick = {
                                it.toMetadata().trackMetadata?.let { it1 ->
                                    socialViewModel.playListen(
                                        it1
                                    )
                                }
                            },
                            showsnackbar = {
                                scope.launch {
                                    snackbarState.showSnackbar(it)
                                }
                            },
                            onAddTrackClick = {
                                playlistViewModel.changeAddTrackBottomSheetState(true)
                            }
                        )
                    } else {
                        Column(modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            HelperText(modifier = Modifier.padding(16.dp),
                                text = "Couldn't load the playlist data")
                            RetryButton() {
                                playlistViewModel.getDataInPlaylistScreen(playlistMBID)
                            }
                        }
                    }
                }
            }
        }
        PullRefreshIndicator(
            modifier = Modifier.align(Alignment.TopCenter),
            refreshing = uiState.playlistDetailUIState.isRefreshing,
            contentColor = ListenBrainzTheme.colorScheme.lbSignatureInverse,
            backgroundColor = ListenBrainzTheme.colorScheme.level1,
            state = pullRefreshState
        )

        if(uiState.playlistDetailUIState.isAddTrackBottomSheetVisible){
            ModalBottomSheet(
                onDismissRequest = {
                    playlistViewModel.changeAddTrackBottomSheetState(false)
                },
                sheetState = sheetState,
                modifier = Modifier.statusBarsPadding()
            ) {
                AddTrackToPlaylist(
                    modifier = Modifier.fillMaxSize(),
                    playlistDetailUIState = uiState.playlistDetailUIState,
                    onTrackSelect = { recordingData ->
                       playlistViewModel.addTrackToPlaylist(
                            recordingData
                        )
                        playlistViewModel.changeAddTrackBottomSheetState(false)
                    },
                    onQueryChange = {
                        playlistViewModel.queryRecordings(it)
                    },
                    onDismiss = {
                        playlistViewModel.changeAddTrackBottomSheetState(false)
                        playlistViewModel.queryRecordings("")
                    }
                )
            }
        }

        ErrorBar(socialUiState.error, socialViewModel::clearErrorFlow)
        SuccessBar(socialUiState.successMsgId, socialViewModel::clearMsgFlow, snackbarState)
        ErrorBar(uiState.error, playlistViewModel::clearErrorFlow)
        SuccessBar(uiState.successMsg, playlistViewModel::clearMsgFlow, snackbarState)
    }
}

@Composable
private fun PlaylistDetailContent(
    playlistDetailUIState: PlaylistDetailUIState,
    goToArtistPage: (String) -> Unit,
    showsnackbar: (String) -> Unit,
    onAddTrackClick: () -> Unit,
    onTrackClick: (PlaylistTrack) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if(playlistDetailUIState.isUserPlaylistOwner) {
                item {
                    AddTrackCard(
                        modifier = Modifier.padding(
                            horizontal = ListenBrainzTheme.paddings.horizontal,
                            vertical = ListenBrainzTheme.paddings.lazyListAdjacent
                        ),
                        onClick = {
                            onAddTrackClick()
                        }
                    )
                }
            }
            items(playlistDetailUIState.playlistData?.track?.size ?: 0) { index ->
                val playlist = playlistDetailUIState.playlistData?.track?.get(index)
                if (playlist != null) {
                    ListenCardSmallDefault(
                        modifier = Modifier.padding(
                            horizontal = ListenBrainzTheme.paddings.horizontal,
                            vertical = ListenBrainzTheme.paddings.lazyListAdjacent
                        ),
                        metadata = (playlist.toMetadata()),
                        coverArtUrl = getCoverArtUrl(
                            caaReleaseMbid = playlist.extension.trackExtensionData.additionalMetadata.caaReleaseMbid,
                            caaId = playlist.extension.trackExtensionData.additionalMetadata.caaId
                        ),
                        onDropdownSuccess = { messsage ->
                            showsnackbar(messsage)
                        },
                        onDropdownError = { error ->
                            showsnackbar(error.toast)
                        },
                        goToArtistPage = goToArtistPage,
                        onClick = {
                            onTrackClick(playlist)
                        },
                        trailingContent = {
                            Text(
                                modifier = Modifier
                                    .padding(bottom = 4.dp),
                                text = formatDurationSeconds(playlist.duration?.div(1000) ?: 0),
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = ListenBrainzTheme.colorScheme.listenText.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        enableTrailingContent = true
                    )


                }
            }
            item {
                if (playlistDetailUIState.playlistData?.track?.size == 0) {
                    HelperText(
                        modifier = Modifier.padding(16.dp),
                        text = "No tracks found"
                    )
                }
            }
        }

    }
}

@Composable
fun HelperText(
    modifier: Modifier,
    text: String
) {
    Text(
        text = text,
        modifier = modifier,
        style = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        ),
        color = ListenBrainzTheme.colorScheme.listenText.copy(alpha = 0.8f)
    )
}