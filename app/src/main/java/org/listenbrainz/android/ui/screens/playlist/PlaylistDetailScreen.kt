package org.listenbrainz.android.ui.screens.playlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.listenbrainz.android.model.playlist.PlaylistTrack
import org.listenbrainz.android.ui.components.ListenCardSmallDefault
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Utils.formatDurationSeconds
import org.listenbrainz.android.util.Utils.getCoverArtUrl
import org.listenbrainz.android.viewmodel.PlaylistDataViewModel
import org.listenbrainz.android.viewmodel.SocialViewModel

@Composable
fun PlaylistDetailScreen(
    playlistMBID: String,
    playlistViewModel: PlaylistDataViewModel = hiltViewModel(),
    socialViewModel: SocialViewModel = hiltViewModel(),
    snackbarState: SnackbarHostState,
    goToArtistPage: (String) -> Unit
) {
    val uiState by playlistViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        playlistViewModel.getInitialDataInPlaylistScreen(playlistMBID)
    }

    PlaylistDetailContent(
        playlistDetailUIState = uiState.playlistDetailUIState,
        goToArtistPage = goToArtistPage,
        onTrackClick = { it.toMetadata().trackMetadata?.let { it1 -> socialViewModel.playListen(it1) } },
        showsnackbar = {
            scope.launch {
                snackbarState.showSnackbar(it)
            }
        }
    )
}

@Composable
private fun PlaylistDetailContent(
    playlistDetailUIState: PlaylistDetailUIState,
    goToArtistPage: (String) -> Unit,
    showsnackbar: (String) -> Unit,
    onTrackClick: (PlaylistTrack) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
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
                if(playlistDetailUIState.playlistData?.track?.size == 0) {
                    Text(
                        text = "No tracks found",
                        modifier = Modifier.padding(16.dp),
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = ListenBrainzTheme.colorScheme.listenText.copy(alpha = 0.8f)
                    )
                }
            }
        }

    }
}