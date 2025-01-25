package org.listenbrainz.android.ui.screens.profile.createdforyou

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.model.createdForYou.CreatedForYouPlaylist
import org.listenbrainz.android.model.playlist.PlaylistTrack
import org.listenbrainz.android.ui.screens.profile.ProfileUiState
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Utils.getCoverArtUrl
import org.listenbrainz.android.viewmodel.UserViewModel

@Composable
fun CreatedForYouScreen(
    snackbarState: SnackbarHostState,
    userViewModel: UserViewModel,
    goToArtistPage: (String) -> Unit,
) {
    val uiState by userViewModel.uiState.collectAsState()
    CreatedForYouScreen(uiState = uiState, onPlaylistSaveClick = {
        //TODO: Implement this
    }, onPlayAllClick = {

    }, onShareClick = {

    }, onTrackClick = {

    }, goToArtistPage = goToArtistPage
    )
}

@Composable
private fun CreatedForYouScreen(
    uiState: ProfileUiState,
    onPlaylistSaveClick: (CreatedForYouPlaylist?) -> Unit,
    onPlayAllClick: () -> Unit,
    onShareClick: (CreatedForYouPlaylist?) -> Unit,
    onTrackClick: (PlaylistTrack) -> Unit,
    goToArtistPage: (String) -> Unit
) {
    var selectedPlaylist by remember {
        mutableStateOf<CreatedForYouPlaylist?>(
            if (uiState.createdForTabUIState.createdForYouPlaylists.isNullOrEmpty()) null
            else uiState.createdForTabUIState.createdForYouPlaylists[0].playlist
        )
    }
    val playlistData =
        uiState.createdForTabUIState.createdForYouPlaylistData?.get(selectedPlaylist?.getPlaylistMBID())

    if (uiState.createdForTabUIState.createdForYouPlaylists.isNullOrEmpty()) {
        Text(
            text = "No playlists found", color = ListenBrainzTheme.colorScheme.onBackground
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = ListenBrainzTheme.colorScheme.gradientBrush)
        ) {
            LazyColumn {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp))
                            .background(ListenBrainzTheme.colorScheme.background)
                    ) {
                        Spacer(modifier = Modifier.height(32.dp))
                        PlaylistTitleCardRow(
                            modifier = Modifier.padding(
                                horizontal = 8.dp, vertical = 8.dp
                            ),
                            playlists = uiState.createdForTabUIState.createdForYouPlaylists.map { it.playlist },
                            selectedPlaylist = selectedPlaylist,
                            onPlaylistSelect = {
                                selectedPlaylist = it
                            },
                            onSaveClick = onPlaylistSaveClick
                        )
                    }
                }
                item {
                    AnimatedContent(
                        selectedPlaylist
                    ) { playlist ->
                        if (playlist == null) {
                            Text(
                                text = "No playlist selected",
                                color = ListenBrainzTheme.colorScheme.onBackground
                            )
                        } else if (playlistData == null) {
                            Text(
                                text = "Playlist data could not be loaded",
                                color = ListenBrainzTheme.colorScheme.onBackground
                            )
                        } else {
                            PlaylistHeadingAndDescription(title = playlistData.title ?: "No title",
                                tracksCount = playlistData.track.size ?: 0,
                                lastUpdatedDate = playlistData.date ?: "No date",
                                description = playlistData.annotation ?: "No description",
                                onPlayAllClick = {
                                    onPlayAllClick()
                                },
                                onShareClick = {
                                    onShareClick(playlist)
                                })
                        }
                    }
                }
                items(playlistData?.track?.size ?: 0) { trackIndex ->
                    if (playlistData != null) PlaylistRowComposable(
                        modifier = Modifier.padding(horizontal = ListenBrainzTheme.paddings.horizontal),
                        trackName = playlistData.track[trackIndex].title ?: "No title",

                        artists = playlistData.track[trackIndex].extension.trackExtensionData.additionalMetadata.artists,
                        goToArtistPage = goToArtistPage,
                        coverArtUrl = getCoverArtUrl(
                            caaReleaseMbid = playlistData.track[trackIndex].extension.trackExtensionData.additionalMetadata.caaReleaseMbid,
                            caaId = playlistData.track[trackIndex].extension.trackExtensionData.additionalMetadata.caaId
                        ),
                        isReorderButtonVisible = false,
                        onClick = { onTrackClick(playlistData.track[trackIndex]) },
                        onPlayClick = { onTrackClick(playlistData.track[trackIndex]) },
                        durationInSeconds = (playlistData.track[trackIndex].duration?.div(1000))
                            ?: 0
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }


    }
}
