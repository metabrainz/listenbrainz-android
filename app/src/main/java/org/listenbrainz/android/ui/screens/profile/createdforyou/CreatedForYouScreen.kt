package org.listenbrainz.android.ui.screens.profile.createdforyou

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.listenbrainz.android.model.SocialUiState
import org.listenbrainz.android.model.createdForYou.CreatedForYouPlaylist
import org.listenbrainz.android.model.playlist.PlaylistTrack
import org.listenbrainz.android.ui.components.ErrorBar
import org.listenbrainz.android.ui.components.ListenCardSmallDefault
import org.listenbrainz.android.ui.components.SuccessBar
import org.listenbrainz.android.ui.screens.profile.ProfileUiState
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Utils.formatDurationSeconds
import org.listenbrainz.android.util.Utils.getCoverArtUrl
import org.listenbrainz.android.util.Utils.shareLink
import org.listenbrainz.android.viewmodel.SocialViewModel
import org.listenbrainz.android.viewmodel.UserViewModel

@Composable
fun CreatedForYouScreen(
    snackbarState: SnackbarHostState,
    socialViewModel: SocialViewModel,
    userViewModel: UserViewModel,
    goToArtistPage: (String) -> Unit,
) {
    val uiState by userViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val socialUiState by socialViewModel.uiState.collectAsState()
    CreatedForYouScreen(uiState = uiState,
        onPlaylistSaveClick = { playlist ->
            userViewModel.saveCreatedForPlaylist(playlist?.getPlaylistMBID()) {
                scope.launch {
                    snackbarState.showSnackbar(it)
                }
            }
        }, onPlayAllClick = {
            //TODO: Implement this
        }, onShareClick = {
            if (it?.identifier != null) {
                shareLink(context, it.identifier)
            } else {
                scope.launch {
                    snackbarState.showSnackbar("Link not found")
                }
            }
        }, onTrackClick = {
            it.toMetadata().trackMetadata?.let { it1 -> socialViewModel.playListen(it1) }
        }, goToArtistPage = goToArtistPage,
        snackbarState = snackbarState,
        socialUiState = socialUiState,
        onErrorShown = {
            socialViewModel.clearErrorFlow()
        },
        onMessageShown = {
            socialViewModel.clearMsgFlow()
        },
        onRetryDataFetch = {
            userViewModel.retryFetchAPlaylist(it.getPlaylistMBID())
        }
    )
}

@Composable
private fun CreatedForYouScreen(
    uiState: ProfileUiState,
    snackbarState: SnackbarHostState,
    socialUiState: SocialUiState,
    onPlaylistSaveClick: (CreatedForYouPlaylist?) -> Unit,
    onPlayAllClick: () -> Unit,
    onShareClick: (CreatedForYouPlaylist?) -> Unit,
    onTrackClick: (PlaylistTrack) -> Unit,
    goToArtistPage: (String) -> Unit,
    onErrorShown: () -> Unit,
    onMessageShown: () -> Unit,
    onRetryDataFetch: (CreatedForYouPlaylist) -> Unit
) {
    var selectedPlaylist by remember {
        mutableStateOf<CreatedForYouPlaylist?>(
            if (uiState.createdForTabUIState.createdForYouPlaylists.isNullOrEmpty()) null
            else uiState.createdForTabUIState.createdForYouPlaylists[0].playlist
        )
    }
    var dropdownItemIndex by remember {
        mutableStateOf<Int?>(null)
    }
    val playlistData =
        uiState.createdForTabUIState.createdForYouPlaylistData?.get(selectedPlaylist?.getPlaylistMBID())

    if (uiState.createdForTabUIState.createdForYouPlaylists.isNullOrEmpty()) {
        Column(modifier = Modifier.padding(ListenBrainzTheme.paddings.horizontal)) {
            Text(
                text = "No playlists found", color = ListenBrainzTheme.colorScheme.onBackground
            )
        }
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
                        PlaylistSelectionCardRow(
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
                            Column(modifier = Modifier.padding(ListenBrainzTheme.paddings.horizontal)) {
                                Text(
                                    text = "Playlist data could not be loaded",
                                    color = ListenBrainzTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = {
                                    onRetryDataFetch(playlist)
                                }) {
                                    Text(
                                        text = "Retry",
                                        color = ListenBrainzTheme.colorScheme.onBackground
                                    )
                                }
                            }
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
                    if (playlistData != null) {
                        val playlist = playlistData.track[trackIndex]
                        ListenCardSmallDefault(
                            modifier = Modifier.padding(
                                horizontal = ListenBrainzTheme.paddings.horizontal
                            ),
                            metadata = (playlist.toMetadata()),
                            coverArtUrl = getCoverArtUrl(
                                caaReleaseMbid = playlist.extension.trackExtensionData.additionalMetadata.caaReleaseMbid,
                                caaId = playlist.extension.trackExtensionData.additionalMetadata.caaId
                            ),
                            onDropdownSuccess = { messsage ->
                                snackbarState.showSnackbar(messsage)
                            },
                            onDropdownError = { error ->
                                snackbarState.showSnackbar(error.toast)
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
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    ErrorBar(error = socialUiState.error, onErrorShown = onErrorShown)

    SuccessBar(
        resId = socialUiState.successMsgId,
        onMessageShown = onMessageShown,
        snackbarState = snackbarState
    )
}

