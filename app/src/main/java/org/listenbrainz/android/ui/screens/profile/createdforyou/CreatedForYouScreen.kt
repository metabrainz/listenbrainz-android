package org.listenbrainz.android.ui.screens.profile.createdforyou

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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.listenbrainz.android.model.SocialUiState
import org.listenbrainz.android.model.userPlaylist.UserPlaylist
import org.listenbrainz.android.model.userPlaylist.UserPlaylists
import org.listenbrainz.android.model.playlist.AdditionalMetadataTrack
import org.listenbrainz.android.model.playlist.PlaylistData
import org.listenbrainz.android.model.playlist.PlaylistTrack
import org.listenbrainz.android.model.playlist.TrackExtension
import org.listenbrainz.android.model.playlist.TrackExtensionData
import org.listenbrainz.android.ui.components.ErrorBar
import org.listenbrainz.android.ui.components.ListenCardSmallDefault
import org.listenbrainz.android.ui.components.SuccessBar
import org.listenbrainz.android.ui.screens.feed.RetryButton
import org.listenbrainz.android.ui.screens.profile.CreatedForTabUIState
import org.listenbrainz.android.ui.screens.profile.ProfileUiState
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Utils.VerticalSpacer
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
    onPlaylistSaveClick: (UserPlaylist?) -> Unit,
    onPlayAllClick: () -> Unit,
    onShareClick: (UserPlaylist?) -> Unit,
    onTrackClick: (PlaylistTrack) -> Unit,
    goToArtistPage: (String) -> Unit,
    onErrorShown: () -> Unit,
    onMessageShown: () -> Unit,
    onRetryDataFetch: (UserPlaylist) -> Unit
) {
    var selectedPlaylist by remember {
        mutableStateOf<UserPlaylist?>(
            if (uiState.createdForTabUIState.createdForYouPlaylists.isNullOrEmpty()) null
            else uiState.createdForTabUIState.createdForYouPlaylists[0].playlist
        )
    }
    val playlistData =
        uiState.createdForTabUIState.createdForYouPlaylistData?.get(selectedPlaylist?.getPlaylistMBID())

    if (uiState.createdForTabUIState.createdForYouPlaylists.isNullOrEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(ListenBrainzTheme.paddings.horizontal),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No playlists found",
                fontWeight = FontWeight.Medium,
                color = ListenBrainzTheme.colorScheme.onBackground
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
                            modifier = Modifier.padding(vertical = 8.dp),
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
                            Box(
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .padding(
                                        horizontal = ListenBrainzTheme.paddings.horizontal,
                                        vertical = 40.dp
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No playlist selected",
                                    fontWeight = FontWeight.Medium,
                                    color = ListenBrainzTheme.colorScheme.onBackground
                                )
                            }
                        } else if (playlistData == null) {
                            Column(
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .padding(
                                        horizontal = ListenBrainzTheme.paddings.horizontal,
                                        vertical = 40.dp
                                    ),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Playlist data could not be loaded :(",
                                    fontWeight = FontWeight.Medium,
                                    color = ListenBrainzTheme.colorScheme.onBackground
                                )

                                VerticalSpacer(8.dp)

                                RetryButton {
                                    onRetryDataFetch(playlist)
                                }
                            }
                        } else {
                            PlaylistHeadingAndDescription(
                                title = playlistData.title ?: "No title",
                                tracksCount = playlistData.track.size,
                                lastUpdatedDate = playlistData.date ?: "No date",
                                description = playlistData.annotation ?: "No description",
                                onPlayAllClick = {
                                    onPlayAllClick()
                                },
                                onShareClick = {
                                    onShareClick(playlist)
                                }
                            )
                        }
                    }
                }

                items(playlistData?.track?.size ?: 0) { trackIndex ->
                    if (playlistData != null) {
                        val playlist = playlistData.track[trackIndex]
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


@Preview(showBackground = true)
@Composable
fun CreatedForScreenPreview() {
    ListenBrainzTheme {
        val playlistTrack = PlaylistTrack(
            title = "Goodbyes",
            identifier = listOf("https://musicbrainz.org/recording/c8162486-f1a6-4673-88a2-e70c2112c221"),
            duration = 174853,
            extension = TrackExtension(
                trackExtensionData = TrackExtensionData(
                    additionalMetadata = AdditionalMetadataTrack(
                        caaId = 24593316652,
                        caaReleaseMbid = "0b751b1b-f420-46e9-b2b5-108615b8427f"
                    )
                )
            )
        )
        CreatedForYouScreen(
            uiState = ProfileUiState(
                createdForTabUIState = CreatedForTabUIState(
                    createdForYouPlaylists = listOf(
                        UserPlaylists(
                            playlist = UserPlaylist(
                                annotation = "\"<p>The ListenBrainz Weekly Exploration playlist helps you discover new music! \\n    It may require active listening and skips. The playlist features tracks you haven't heard before, \\n    selected by a collaborative filtering algorithm.</p>\\n\\n    <p>Updated every Monday morning based on your timezone.</p>\\n\"",
                                creator = "listenbrainz",
                                date = "2025-01-20T00:09:28.012627+00:00",
                                identifier = "\"https://listenbrainz.org/playlist/66d1b8fb-f5ed-4f82-b8e2-eac48f2fd278\"",
                                title = "Weekly Exploration for hemang-mishra, week of 2025-01-20 Mon"
                            )
                        ),
                        UserPlaylists(
                            playlist = UserPlaylist(
                                annotation = "\"<p>The ListenBrainz Weekly Exploration playlist helps you discover new music! \\n    It may require active listening and skips. The playlist features tracks you haven't heard before, \\n    selected by a collaborative filtering algorithm.</p>\\n\\n    <p>Updated every Monday morning based on your timezone.</p>\\n\"",
                                creator = "listenbrainz",
                                date = "2025-01-20T00:09:28.012627+00:00",
                                identifier = "\"https://listenbrainz.org/playlist/66d1b8fb-f5ed-4f82-b8e2-eac48f2fd279\"",
                                title = "Weekly Exploration for hemang-mishra, week of 2025-01-20 Mon"
                            )
                        ),
                    ),
                    createdForYouPlaylistData = mapOf(
                        "66d1b8fb-f5ed-4f82-b8e2-eac48f2fd278" to PlaylistData(
                            title = "Playlist 1",
                            track = listOf(
                                playlistTrack,
                                playlistTrack,
                                playlistTrack
                            ),
                            date = "2025-01-13T00:07:44.741098+00:00",
                            annotation = "<p>The ListenBrainz Weekly Exploration playlist helps you discover new music! \\n    It may require active listening and skips. The playlist features tracks you haven't heard before, \\n    selected by a collaborative filtering algorithm.</p>\\n\\n    <p>Updated every Monday morning based on your timezone.</p>"
                        )
                    )
                )
            ),
            snackbarState = SnackbarHostState(),
            socialUiState = SocialUiState(),
            onPlaylistSaveClick = {},
            onPlayAllClick = {},
            onShareClick = {},
            onTrackClick = {},
            goToArtistPage = {},
            onErrorShown = {},
            onMessageShown = {},
            onRetryDataFetch = {}
        )
    }
}