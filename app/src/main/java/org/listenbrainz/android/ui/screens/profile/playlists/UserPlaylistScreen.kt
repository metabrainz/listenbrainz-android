package org.listenbrainz.android.ui.screens.profile.playlists

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import org.listenbrainz.android.R
import org.listenbrainz.android.model.userPlaylist.UserPlaylist
import org.listenbrainz.android.ui.components.ToggleChips
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.UserViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UserPlaylistScreen(
    snackbarState: SnackbarHostState,
    userViewModel: UserViewModel
) {
    val uiState by userViewModel.uiState.collectAsState()
    val userPlaylistsData = uiState.playlistTabUIState.userPlaylists.collectAsLazyPagingItems()
    val collabPlaylistsData = uiState.playlistTabUIState.collabPlaylists.collectAsLazyPagingItems()
    var currentPlaylistView by remember {
        mutableStateOf(PlaylistView.LIST)
    }
    var isCurrentScreenCollab by remember { mutableStateOf(false) }
    val isRefreshing = remember(
        isCurrentScreenCollab,
        userPlaylistsData.loadState.refresh,
        collabPlaylistsData.loadState.refresh
    ) {
        if (isCurrentScreenCollab) {
            collabPlaylistsData.loadState.refresh is LoadState.Loading
        } else {
            userPlaylistsData.loadState.refresh is LoadState.Loading
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            if (isCurrentScreenCollab) {
                collabPlaylistsData.refresh()
            } else {
                userPlaylistsData.refresh()
            }
        }
    )

    UserPlaylistScreenBase(
        pullRefreshState = pullRefreshState,
        isRefreshing = isRefreshing,
        userPlaylistDataSize = userPlaylistsData.itemCount,
        collabPlaylistDataSize = collabPlaylistsData.itemCount,
        getUserPlaylist = { index ->
            userPlaylistsData[index]
        },
        getCollabPlaylist = { index ->
            collabPlaylistsData[index]
        },
        isCurrentScreenCollab = isCurrentScreenCollab,
        currentPlaylistView = currentPlaylistView,
        currentSortType = uiState.playlistTabUIState.currentSortType,
        onPlaylistSectionClick = {
            isCurrentScreenCollab = false
        },
        onCollabSectionClick = {
            isCurrentScreenCollab = true
        },
        onClickPlaylistViewChange = {
            if (currentPlaylistView == PlaylistView.LIST) {
                currentPlaylistView = PlaylistView.GRID
            } else {
                currentPlaylistView = PlaylistView.LIST
            }
        },
        onSortTypeChange = { sortType ->
            userViewModel.changeSortType(sortType)
        }
    )
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun UserPlaylistScreenBase(
    pullRefreshState: PullRefreshState,
    isRefreshing: Boolean,
    userPlaylistDataSize: Int,
    collabPlaylistDataSize: Int,
    isCurrentScreenCollab: Boolean,
    getUserPlaylist: (Int) -> UserPlaylist?,
    getCollabPlaylist: (Int) -> UserPlaylist?,
    currentPlaylistView: PlaylistView,
    currentSortType: PlaylistSortType,
    onPlaylistSectionClick: () -> Unit,
    onCollabSectionClick: () -> Unit,
    onClickPlaylistViewChange: () -> Unit,
    onSortTypeChange: (PlaylistSortType) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            PlaylistScreenTopSection(
                modifier = Modifier.fillMaxWidth(),
                playlistType = if (isCurrentScreenCollab) PlaylistType.COLLABORATIVE else PlaylistType.USER_PLAYLIST,
                playlistView = currentPlaylistView,
                currentSortType = currentSortType,
                onUserPlaylistClick = onPlaylistSectionClick,
                onCollabPlaylistClick = onCollabSectionClick,
                onClickPlaylistViewChange = onClickPlaylistViewChange,
                onSortTypeChange = onSortTypeChange
            )
            AnimatedContent(isCurrentScreenCollab) { isCurrentScreenCollab ->
                AnimatedContent(currentPlaylistView) { currentPlaylistView ->
                    when (currentPlaylistView) {
                        PlaylistView.LIST -> {
                            LazyColumn {
                                items(if (isCurrentScreenCollab) collabPlaylistDataSize else userPlaylistDataSize) { index ->
                                    val playlist =
                                        if (isCurrentScreenCollab) getCollabPlaylist(index) else getUserPlaylist(
                                            index
                                        )
                                    if (playlist != null) {

                                    }
                                }
                            }
                        }

                        PlaylistView.GRID -> {
                            LazyVerticalGrid(columns = GridCells.Fixed(2)) {
                                items(if (isCurrentScreenCollab) collabPlaylistDataSize else userPlaylistDataSize) { index ->
                                    val playlist =
                                        if (isCurrentScreenCollab) getCollabPlaylist(index) else getUserPlaylist(
                                            index
                                        )
                                    if (playlist != null) {
                                        PlaylistGridViewCard(
                                            modifier = Modifier,
                                            title = playlist.title ?: "",
                                            trackCount = 5,
                                            updatedDate = "5 days ago",
                                            onClickOptionsButton = {},
                                            onClickCard = { },
                                            coverArt = playlist.coverArt,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
        PullRefreshIndicator(
            modifier = Modifier.align(Alignment.TopCenter),
            refreshing = isRefreshing,
            contentColor = ListenBrainzTheme.colorScheme.lbSignatureInverse,
            backgroundColor = ListenBrainzTheme.colorScheme.level1,
            state = pullRefreshState
        )

    }
}

@Composable
private fun PlaylistScreenTopSection(
    modifier: Modifier,
    playlistType: PlaylistType,
    playlistView: PlaylistView,
    currentSortType: PlaylistSortType = PlaylistSortType.RANDOM,
    onUserPlaylistClick: () -> Unit,
    onCollabPlaylistClick: () -> Unit,
    onClickPlaylistViewChange: () -> Unit,
    onSortTypeChange: (PlaylistSortType) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .align(Alignment.CenterStart)
        ) {
            ToggleChips(
                modifier = Modifier,
                currentPageStateProvider = { if (playlistType == PlaylistType.USER_PLAYLIST) 0 else 1 },
                chips = listOf("User Playlist", "Collaborative"),
                onClick = {
                    when (it) {
                        0 -> onUserPlaylistClick()
                        1 -> onCollabPlaylistClick()
                    }
                },
                icons = listOf(null, R.drawable.playlist_collab)
            )
        }
        Row(
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            IconButton(
                onClick = {}
            ) { }
            IconButton(
                onClick = { onClickPlaylistViewChange() }
            ) {
                AnimatedContent(playlistView) {
                    when (it) {
                        PlaylistView.LIST -> {
                            Icon(
                                painter = painterResource(R.drawable.playlist_listview),
                                tint = ListenBrainzTheme.colorScheme.followerChipSelected,
                                contentDescription = "List View of Playlist Screen"
                            )
                        }

                        PlaylistView.GRID -> {
                            Icon(
                                painter = painterResource(R.drawable.playlist_gridview),
                                tint = ListenBrainzTheme.colorScheme.followerChipSelected,
                                contentDescription = "Grid View of Playlist Screen"
                            )
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun UserPlaylistScreenPreview() {
    ListenBrainzTheme {
        UserPlaylistScreenBase(
            pullRefreshState = rememberPullRefreshState(
                refreshing = false,
                onRefresh = {}
            ),
            isRefreshing = false,
            getUserPlaylist = { index ->
                UserPlaylist(
                    title = "Playlist $index"
                )
            },
            getCollabPlaylist = { index ->
                UserPlaylist(
                    title = "Collab Playlist $index"
                )
            },
            userPlaylistDataSize = 20,
            collabPlaylistDataSize = 20,
            isCurrentScreenCollab = true,
            currentPlaylistView = PlaylistView.GRID,
            currentSortType = PlaylistSortType.RANDOM,
            onPlaylistSectionClick = {},
            onCollabSectionClick = {},
            onClickPlaylistViewChange = { },
            onSortTypeChange = { }
        )
    }
}