package org.listenbrainz.android.ui.screens.profile.playlists

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.ShimmerTheme
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import com.valentinilk.shimmer.shimmerSpec
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.model.userPlaylist.UserPlaylist
import org.listenbrainz.android.ui.components.ToggleChips
import org.listenbrainz.android.ui.screens.feed.RetryButton
import org.listenbrainz.android.ui.screens.playlist.CreateEditPlaylistScreen
import org.listenbrainz.android.ui.screens.profile.createdforyou.formatDateLegacy
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_purple_night
import org.listenbrainz.android.util.Utils.shareLink
import org.listenbrainz.android.viewmodel.PlaylistDataViewModel
import org.listenbrainz.android.viewmodel.UserViewModel

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UserPlaylistScreen(
    snackbarState: SnackbarHostState,
    userViewModel: UserViewModel,
    playlistViewModel: PlaylistDataViewModel,
    goToPlaylist: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by userViewModel.uiState.collectAsState()
    val userPlaylistsData = uiState.playlistTabUIState.userPlaylists.collectAsLazyPagingItems()
    val collabPlaylistsData = uiState.playlistTabUIState.collabPlaylists.collectAsLazyPagingItems()
    var currentPlaylistView by remember {
        mutableStateOf(PlaylistView.GRID)
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
    var isBottomSheetVisible by rememberSaveable { mutableStateOf(false) }

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
    var currentMBID by remember {
        mutableStateOf<String?>(null)
    }

    UserPlaylistScreenBase(
        pullRefreshState = pullRefreshState,
        isRefreshing = isRefreshing,
        userPlaylistDataSize = userPlaylistsData.itemCount,
        collabPlaylistDataSize = collabPlaylistsData.itemCount,
        getUserPlaylist = { index ->
            userPlaylistsData[index]
        },
        getPlaylistCoverArt = userViewModel::fetchCoverArt,
        getCollabPlaylist = { index ->
            collabPlaylistsData[index]
        },
        isCurrentScreenCollab = isCurrentScreenCollab,
        currentPlaylistView = currentPlaylistView,
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
        onClickPlaylist = { playlist ->
            playlist.getPlaylistMBID()?.let { goToPlaylist(it) }
        },
        onRetry = {
            collabPlaylistsData.refresh()
            userPlaylistsData.refresh()
        },
        isUserSelf = uiState.isSelf,
        onCreatePlaylistClick = {
            isBottomSheetVisible = true
        },
        onDropdownItemClick = { menuItem, playlist ->
            when (menuItem) {
                PlaylistDropdownItems.DUPLICATE -> {
                    userViewModel.saveCreatedForPlaylist(
                        playlist.getPlaylistMBID(),
                        onCompletion = {
                            scope.launch {
                                snackbarState.showSnackbar(it)
                            }
                            collabPlaylistsData.refresh()
                            userPlaylistsData.refresh()
                        }
                    )
                }

                PlaylistDropdownItems.DELETE -> {
                    userViewModel.deletePlaylist(
                        playlist.getPlaylistMBID(),
                        onCompletion = {
                            scope.launch {
                                snackbarState.showSnackbar(it)
                            }
                            collabPlaylistsData.refresh()
                            userPlaylistsData.refresh()
                        }
                    )
                }

                PlaylistDropdownItems.SHARE -> {
                    if (playlist.identifier != null) {
                        shareLink(context, playlist.identifier)
                    } else {
                        scope.launch {
                            snackbarState.showSnackbar("Link not found")
                        }
                    }
                }

                PlaylistDropdownItems.EDIT -> {
                    currentMBID = playlist.getPlaylistMBID()
                    isBottomSheetVisible = true
                }
            }
        }
    )

    CreateEditPlaylistScreen(
        viewModel = playlistViewModel,
        isVisible = isBottomSheetVisible,
        mbid = currentMBID
    ) {
        currentMBID = null
        isBottomSheetVisible = false
    }

}


@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun UserPlaylistScreenBase(
    pullRefreshState: PullRefreshState,
    isRefreshing: Boolean,
    isUserSelf: Boolean,
    userPlaylistDataSize: Int,
    collabPlaylistDataSize: Int,
    isCurrentScreenCollab: Boolean,
    getPlaylistCoverArt: suspend (UserPlaylist) -> String?,
    getUserPlaylist: (Int) -> UserPlaylist?,
    getCollabPlaylist: (Int) -> UserPlaylist?,
    currentPlaylistView: PlaylistView,
    onPlaylistSectionClick: () -> Unit,
    onCollabSectionClick: () -> Unit,
    onClickPlaylistViewChange: () -> Unit,
    onClickPlaylist: (UserPlaylist) -> Unit,
    onRetry: () -> Unit,
    onDropdownItemClick: (PlaylistDropdownItems, UserPlaylist) -> Unit,
    onCreatePlaylistClick: () -> Unit
) {

    val shimmerInstance = rememberShimmer(
        shimmerBounds = ShimmerBounds.View,
        theme = ShimmerTheme(
            animationSpec = infiniteRepeatable(
                animation = shimmerSpec(
                    durationMillis = 300,
                    delayMillis = 800,
                ),
                repeatMode = RepeatMode.Restart,
            ),
            blendMode = BlendMode.DstIn,
            rotation = 6.0f,
            shaderColors = listOf(
                Color.White.copy(alpha = 0.25f),
                Color.White.copy(alpha = 1.00f),
                Color.White.copy(alpha = 0.25f),
            ),
            shaderColorStops = listOf(
                0.0f,
                0.5f,
                1.0f,
            ),
            shimmerWidth = 350.dp,
        )
    )

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
                onUserPlaylistClick = onPlaylistSectionClick,
                onCollabPlaylistClick = onCollabSectionClick,
                onClickPlaylistViewChange = onClickPlaylistViewChange,
            )
            if (isRefreshing) {
                AnimatedContent(currentPlaylistView) { currentPlaylistView ->
                    when (currentPlaylistView) {
                        PlaylistView.LIST -> {
                            val listState = rememberLazyListState()
                            var height by remember { mutableIntStateOf(0) }
                            val count by remember {
                                derivedStateOf {
                                    if (height == 0) {
                                        return@derivedStateOf 0
                                    }
                                    listState.layoutInfo.viewportSize.height / height
                                }
                            }
                            LazyColumn(
                                modifier = Modifier
                                    .padding(
                                        ListenBrainzTheme.paddings.horizontal
                                    )
                                    .fillMaxSize(),
                                state = listState
                            ) {
                                item(contentType = "shimmer") {
                                    ShimmerListItem(
                                        shimmerInstance,
                                        modifier = Modifier.onSizeChanged {
                                            height = it.height
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                items(count, contentType = { "shimmer" }) {
                                    ShimmerListItem(shimmerInstance)
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }

                        PlaylistView.GRID -> {
                            val listState = rememberLazyGridState()
                            var height by remember { mutableIntStateOf(0) }
                            var width by remember { mutableIntStateOf(0) }
                            val count by remember {
                                derivedStateOf {
                                    if (width == 0 || height == 0) {
                                        return@derivedStateOf 0
                                    }
                                    val row = listState.layoutInfo.viewportSize.height / height
                                    val column = listState.layoutInfo.viewportSize.width / width
                                    (row * column * 2) - 1
                                }
                            }

                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(150.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalArrangement = Arrangement.Center,
                                state = listState
                            ) {
                                item(contentType = "shimmer") {
                                    ShimmerGridItem(
                                        shimmerInstance,
                                        modifier = Modifier.onSizeChanged {
                                            width = it.width
                                            height = it.height
                                        }
                                    )
                                }
                                items(count, contentType = { "shimmer" }) {
                                    ShimmerGridItem(shimmerInstance)
                                }
                            }
                        }
                    }
                }

            } else {
                AnimatedContent(isCurrentScreenCollab) { isCurrentScreenCollab ->
                    if ((isCurrentScreenCollab && collabPlaylistDataSize != 0) || (!isCurrentScreenCollab && userPlaylistDataSize != 0)) {
                        AnimatedContent(currentPlaylistView) { currentPlaylistView ->
                            when (currentPlaylistView) {
                                PlaylistView.LIST -> {
                                    LazyColumn(
                                        modifier = Modifier.padding(
                                            ListenBrainzTheme.paddings.horizontal
                                        )
                                    ) {
                                        items(if (isCurrentScreenCollab) collabPlaylistDataSize else userPlaylistDataSize) { index ->
                                            val playlist =
                                                if (isCurrentScreenCollab) getCollabPlaylist(
                                                    index
                                                ) else getUserPlaylist(
                                                    index
                                                )
                                            if (playlist != null) {
                                                val coverArt by produceState<String?>(
                                                    initialValue = null,
                                                    key1 = playlist
                                                ) {
                                                    value = getPlaylistCoverArt(playlist)
                                                }
                                                PlaylistListViewCard(
                                                    modifier = Modifier,
                                                    title = playlist.title ?: "",
                                                    updatedDate = formatDateLegacy(
                                                        playlist.extension.createdForYouExtensionData.lastModifiedAt
                                                            ?: "",
                                                        showTime = false
                                                    ),
                                                    onClickCard = { onClickPlaylist(playlist) },
                                                    coverArt = coverArt,
                                                    onDropdownClick = {
                                                        onDropdownItemClick(it, playlist)
                                                    },
                                                    canUserEdit = isUserSelf && !isCurrentScreenCollab
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                            }
                                        }
                                    }
                                }

                                PlaylistView.GRID -> {

                                    LazyVerticalGrid(
                                        columns = GridCells.Adaptive(150.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        items(if (isCurrentScreenCollab) collabPlaylistDataSize else userPlaylistDataSize) { index ->
                                            val playlist =
                                                if (isCurrentScreenCollab) getCollabPlaylist(
                                                    index
                                                ) else getUserPlaylist(
                                                    index
                                                )
                                            if (playlist != null) {
                                                val coverArt by produceState<String?>(
                                                    initialValue = null,
                                                    key1 = playlist
                                                ) {
                                                    value = getPlaylistCoverArt(playlist)
                                                }
                                                PlaylistGridViewCard(
                                                    modifier = Modifier,
                                                    title = playlist.title ?: "",
                                                    updatedDate = formatDateLegacy(
                                                        playlist.extension.createdForYouExtensionData.lastModifiedAt
                                                            ?: "",
                                                        showTime = false
                                                    ),
                                                    onClickCard = { onClickPlaylist(playlist) },
                                                    coverArt = coverArt,
                                                    onDropdownClick = {
                                                        onDropdownItemClick(it, playlist)
                                                    },
                                                    canUserEdit = isUserSelf && !isCurrentScreenCollab
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!isRefreshing) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No Playlists Found",
                                        color = ListenBrainzTheme.colorScheme.listenText,
                                        style = ListenBrainzTheme.textStyles.listenTitle
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    RetryButton {
                                        onRetry()
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

        FloatingActionButton(
            onClick = {
                onCreatePlaylistClick()
            },
            backgroundColor = lb_purple_night,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(ListenBrainzTheme.paddings.defaultPadding)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Floating action button to create playlist",
                tint = ListenBrainzTheme.colorScheme.onLbSignature
            )
        }

    }
}

@Composable
fun ShimmerListItem(
    shimmer: Shimmer,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(ListenBrainzTheme.sizes.listenCardHeight)
            .background(
                Color.Gray.copy(alpha = 0.1f),
                RoundedCornerShape(6.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .height(ListenBrainzTheme.sizes.listenCardHeight)
                .width(60.dp)
                .shimmer(shimmer)
                .background(
                    Color.Gray.copy(alpha = 0.8f),
                    RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)
                )
        )
        Spacer(modifier = Modifier.padding(6.dp))
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(10.dp)
                    .shimmer(shimmer)
                    .background(
                        Color.Gray.copy(alpha = 0.8f),
                        RoundedCornerShape(2.dp)
                    )
            )
            Spacer(modifier = Modifier.height(5.dp))
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(8.dp)
                    .shimmer(shimmer)
                    .background(
                        Color.Gray.copy(alpha = 0.8f),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    }

}

@Composable
fun ShimmerGridItem(
    shimmer: Shimmer, modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(8.dp)
            .width(150.dp)
            .clip(ListenBrainzTheme.shapes.listenCardSmall)
            .background(
                Color.Gray.copy(alpha = 0.1f),
                shape = ListenBrainzTheme.shapes.listenCardSmall,
            )
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .shimmer(shimmer)
                .background(
                    Color.Gray.copy(alpha = 0.8f),
                    RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                )
        )
        Spacer(modifier = Modifier.padding(6.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(10.dp)
                    .shimmer(shimmer)
                    .background(
                        Color.Gray.copy(alpha = 0.8f),
                        RoundedCornerShape(2.dp)
                    )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(8.dp)
                    .shimmer(shimmer)
                    .background(
                        Color.Gray.copy(alpha = 0.8f),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
        Spacer(modifier = Modifier.padding(20.dp))
    }

}

@Composable
private fun PlaylistScreenTopSection(
    modifier: Modifier,
    playlistType: PlaylistType,
    playlistView: PlaylistView,
    onUserPlaylistClick: () -> Unit,
    onCollabPlaylistClick: () -> Unit,
    onClickPlaylistViewChange: () -> Unit,
) {
    Row(modifier = modifier) {
        ToggleChips(
            modifier = Modifier.weight(1f),
            currentPageStateProvider = { if (playlistType == PlaylistType.USER_PLAYLIST) 0 else 1 },
            chips = remember {
                listOf("User Playlist", "Collaborative")
            },
            onClick = {
                when (it) {
                    0 -> onUserPlaylistClick()
                    1 -> onCollabPlaylistClick()
                }
            },
            icons = remember {
                listOf(null, R.drawable.playlist_collab)
            }
        )

        IconButton(
            modifier = Modifier,
            onClick = onClickPlaylistViewChange
        ) {
            AnimatedContent(playlistView) {
                when (it) {
                    PlaylistView.GRID -> {
                        Icon(
                            painter = painterResource(R.drawable.playlist_listview),
                            tint = ListenBrainzTheme.colorScheme.followerChipSelected,
                            contentDescription = "List View of Playlist Screen"
                        )
                    }

                    PlaylistView.LIST -> {
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
            onPlaylistSectionClick = {},
            getPlaylistCoverArt = { null },
            onCollabSectionClick = {},
            onClickPlaylistViewChange = { },
            onClickPlaylist = { },
            onRetry = {},
            onDropdownItemClick = { it, it2 ->
            },
            isUserSelf = true,
            onCreatePlaylistClick = { },
        )
    }
}