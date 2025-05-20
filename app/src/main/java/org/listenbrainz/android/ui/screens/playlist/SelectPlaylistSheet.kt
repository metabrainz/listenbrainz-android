package org.listenbrainz.android.ui.screens.playlist

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.model.playlist.PlaylistTrack
import org.listenbrainz.android.model.userPlaylist.UserPlaylist
import org.listenbrainz.android.ui.components.ErrorBar
import org.listenbrainz.android.ui.components.LoadingAnimation
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_purple
import org.listenbrainz.android.viewmodel.PlaylistDataViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun SelectPlaylist(
    modifier: Modifier = Modifier,
    isSheetVisible: Boolean,
    viewModel: PlaylistDataViewModel = hiltViewModel(),
    trackMetadata: Metadata,
    onCreateNewPlaylist: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userPlaylistData = viewModel.userPlaylistPager.collectAsLazyPagingItems()
    val uiState by viewModel.uiState.collectAsState()
    val collabPlaylistData = viewModel.collabPlaylistPager.collectAsLazyPagingItems()
    val isRefreshing = remember(
        userPlaylistData.loadState.refresh,
        collabPlaylistData.loadState.refresh
    ) {
        userPlaylistData.loadState.refresh is LoadState.Loading ||
                collabPlaylistData.loadState.refresh is LoadState.Loading
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            userPlaylistData.refresh()
            collabPlaylistData.refresh()
        }
    )
    val isAddingTrack by viewModel.isLoadingWhileAddingTrack.collectAsState()

    val dismissWithAnimation: () -> Unit = {
        scope.launch {
            sheetState.hide()
            onDismiss()
        }
    }

    LaunchedEffect(isSheetVisible) {
        userPlaylistData.refresh()
        collabPlaylistData.refresh()
    }

    if (isSheetVisible) {
        ModalBottomSheet(
            modifier = Modifier.statusBarsPadding(),
            onDismissRequest = dismissWithAnimation,
            sheetState = sheetState,
            containerColor = ListenBrainzTheme.colorScheme.background
        ) {
            Box(modifier = modifier.fillMaxSize()) {
                SelectPlaylistBase(
                    modifier = Modifier,
                    songName = trackMetadata.trackMetadata?.trackName ?: "",
                    onSelect = { playlist ->
                        viewModel.addTrackToPlaylistFromSelectPlaylist(
                            trackMetadata,
                            playlist.getPlaylistMBID()
                        ) {
                            dismissWithAnimation()
                            Toast.makeText(context, "Track added to playlist", Toast.LENGTH_SHORT)
                                .show()
                        }
                    },
                    onCreateNewPlaylist = onCreateNewPlaylist,
                    getUserPlaylist = userPlaylistData::get,
                    getCollabPlaylist = collabPlaylistData::get,
                    userPlaylistDataSize = userPlaylistData.itemCount,
                    collabPlaylistDataSize = collabPlaylistData.itemCount,
                    isRefreshing = isRefreshing,
                    pullRefreshState = pullRefreshState,
                )

                Column {
                    ErrorBar(uiState.error) {
                        viewModel.clearErrorFlow()
                    }

                    if (isAddingTrack) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(ListenBrainzTheme.colorScheme.background.copy(alpha = 0.75f)),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingAnimation()
                        }
                    }
                }
            }

        }
    }
}


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterialApi::class)
@Composable
private fun SelectPlaylistBase(
    songName: String,
    modifier: Modifier = Modifier,
    onSelect: (UserPlaylist) -> Unit,
    onCreateNewPlaylist: () -> Unit,
    getUserPlaylist: (Int) -> UserPlaylist?,
    getCollabPlaylist: (Int) -> UserPlaylist?,
    userPlaylistDataSize: Int,
    collabPlaylistDataSize: Int,
    isRefreshing: Boolean,
    pullRefreshState: PullRefreshState,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(ListenBrainzTheme.colorScheme.background)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.add_track_to_playlist),
                    contentDescription = "Add to playlist icon",
                    tint = ListenBrainzTheme.colorScheme.listenText
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add to playlist",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = ListenBrainzTheme.colorScheme.listenText
                )
            }
            Spacer(Modifier.height(16.dp))

            HorizontalDivider()


            Text(
                modifier = Modifier.padding(16.dp),
                text = buildAnnotatedString {
                    append("Add the track ")
                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(songName)
                    }
                    append(" to one or more of your playlists below:")
                },
                color = ListenBrainzTheme.colorScheme.text
            )

            LazyColumn(modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, ListenBrainzTheme.colorScheme.level2)) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(lb_purple.copy(alpha = 0.2f))
                            .clickable { onCreateNewPlaylist() }
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CreateNewFolder,
                                contentDescription = null,
                                tint = ListenBrainzTheme.colorScheme.text
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create new playlist", color = ListenBrainzTheme.colorScheme.text)
                        }
                    }
                }

                items(userPlaylistDataSize) { index ->
                    getUserPlaylist(index)?.let { playlist ->
                        PlaylistItem(playlist = playlist, onClick = { onSelect(playlist) })
                    }
                }

                if (collabPlaylistDataSize > 0) {
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = "Collaborative Playlists",
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            color = ListenBrainzTheme.colorScheme.text,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                items(collabPlaylistDataSize) { index ->
                    getCollabPlaylist(index)?.let { playlist ->
                        PlaylistItem(playlist = playlist, onClick = { onSelect(playlist) })
                    }
                }
            }
        }

        PullRefreshIndicator(
            modifier = Modifier.align(Alignment.TopCenter),
            refreshing = isRefreshing,
            state = pullRefreshState,
            contentColor = ListenBrainzTheme.colorScheme.lbSignatureInverse,
            backgroundColor = ListenBrainzTheme.colorScheme.level1
        )
    }
}


@Composable
fun PlaylistItem(playlist: UserPlaylist, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Text(
            text = playlist.title ?: "Title not available",
            style = MaterialTheme.typography.bodyLarge,
            color = ListenBrainzTheme.colorScheme.text,
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp)
        )
        HorizontalDivider(color = ListenBrainzTheme.colorScheme.text.copy(0.2f))
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SelectPlaylistBasePreview() {
    val userPlaylists = List(3) { UserPlaylist("User Playlist ${it + 1}") }
    val collabPlaylists = List(2) { UserPlaylist("Collab Playlist ${it + 1}") }
    val isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(refreshing = isRefreshing, onRefresh = {})

    ListenBrainzTheme {
        SelectPlaylistBase(
            songName = "Mannat",
            onSelect = {},
            onCreateNewPlaylist = {},
            getUserPlaylist = { index -> userPlaylists.getOrNull(index) },
            getCollabPlaylist = { index -> collabPlaylists.getOrNull(index) },
            userPlaylistDataSize = userPlaylists.size,
            collabPlaylistDataSize = collabPlaylists.size,
            isRefreshing = isRefreshing,
            pullRefreshState = pullRefreshState,
        )
    }
}
