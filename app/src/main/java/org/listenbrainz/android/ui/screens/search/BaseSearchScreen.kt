package org.listenbrainz.android.ui.screens.search

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import org.koin.androidx.compose.koinViewModel
import org.listenbrainz.android.model.User
import org.listenbrainz.android.model.feed.FeedListenArtist
import org.listenbrainz.android.model.playlist.PlaylistTrack
import org.listenbrainz.android.model.search.SearchData
import org.listenbrainz.android.model.search.SearchType
import org.listenbrainz.android.model.search.albumSearch.AlbumSearchUiState
import org.listenbrainz.android.model.search.artistSearch.ArtistSearchUiState
import org.listenbrainz.android.model.search.playlistSearch.PlayListSearchUiState
import org.listenbrainz.android.model.search.trackSearch.TrackSearchUiState
import org.listenbrainz.android.model.search.userSearch.UserListUiState
import org.listenbrainz.android.ui.components.FollowButton
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.components.ListenCardSmallDefault
import org.listenbrainz.android.ui.components.NavigationChips
import org.listenbrainz.android.ui.components.TitleAndSubtitle
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Utils.formatDurationSeconds
import org.listenbrainz.android.util.Utils.getCoverArtUrl
import org.listenbrainz.android.util.Utils.removeHtmlTags
import org.listenbrainz.android.viewmodel.SearchViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BaseSearchScreen(
    viewModel: SearchViewModel = koinViewModel(),
    goToUserPage: (String) -> Unit,
    goToPlaylist: (String) -> Unit,
    goToArtist: (String) -> Unit,
    goToAlbum: (String) -> Unit,
    deactivate: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoadingFlow.collectAsState()
    val searchOptions = remember {
        listOf(
            SearchType.USER,
            SearchType.PLAYLIST,
            SearchType.ARTIST,
            SearchType.ALBUM,
            SearchType.TRACK
        )
    }
    val pagerState = rememberPagerState(initialPage = 0) { searchOptions.size }

    LaunchedEffect(pagerState.currentPage) {
        val selectedType = searchOptions[pagerState.currentPage]
        if (uiState.selectedSearchType != selectedType) {
            viewModel.updateSearchOption(selectedType)
        }
    }

    LaunchedEffect(uiState.selectedSearchType) {
        val index = searchOptions.indexOf(uiState.selectedSearchType)
        if (index != pagerState.currentPage) {
            pagerState.scrollToPage(index)
        }
    }

    SearchScreen(
        uiState = uiState,
        onDismiss = {
            deactivate()
            viewModel.clearUi()
        },
        onQueryChange = viewModel::updateQueryFlow,
        onClear = viewModel::clearUi,
        onErrorShown = viewModel::clearErrorFlow,
        placeholderText = uiState.selectedSearchType.placeholder,
        onChangeSearchOption = viewModel::updateSearchOption,
        isBrainzPlayerSearch = false,
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            NavigationChips(
                chips = remember { searchOptions.map { it.title } },
                currentPageStateProvider = {
                    pagerState.currentPage
                }
            ) { position ->
                viewModel.updateSearchOption(searchOptions[position])
            }
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = ListenBrainzTheme.colorScheme.text
            )
            Spacer(modifier = Modifier.padding(3.dp))
            HorizontalPager(
                state = pagerState,
            ) { position ->
                val searchType = searchOptions[position]
                val result = uiState.result

                if (isLoading) {
                    when (searchType) {
                        SearchType.USER -> {
                            UserList(
                                uiState = UserListUiState(emptyList(), emptyList()),
                                onFollowClick = { _, _ -> },
                                goToUserPage = {},
                                isLoading = true
                            )
                        }

                        SearchType.PLAYLIST -> {
                            Playlists(
                                uiState = PlayListSearchUiState(emptyList()),
                                goToPlaylist = {},
                                isLoading = true
                            )
                        }

                        SearchType.ALBUM -> {
                            Albums(
                                uiState = AlbumSearchUiState(emptyList()),
                                goToAlbum = {},
                                isLoading = true
                            )
                        }

                        SearchType.TRACK -> {
                            Tracks(
                                uiState = TrackSearchUiState(emptyList()),
                                goToTrack = {},
                                goToArtistPage = {},
                                isLoading = true
                            )
                        }

                        SearchType.ARTIST -> {
                            Artists(
                                uiState = ArtistSearchUiState(emptyList()),
                                goToArtist = {},
                                isLoading = true
                            )
                        }
                    }
                } else {
                    when (result) {
                        is SearchData.Users -> {
                            UserList(
                                uiState = result.data,
                                onFollowClick = viewModel::toggleFollowStatus,
                                goToUserPage = goToUserPage,
                                isLoading = false
                            )
                        }

                        is SearchData.Playlists -> {
                            Playlists(
                                uiState = result.data,
                                goToPlaylist = goToPlaylist,
                                isLoading = false
                            )
                        }

                        is SearchData.Artists -> {
                            Artists(
                                uiState = result.data,
                                goToArtist = goToArtist,
                                isLoading = false
                            )
                        }

                        is SearchData.Tracks -> {
                            Tracks(
                                uiState = result.data,
                                goToTrack = {
                                    it.toMetadata().trackMetadata?.let { it1 ->
                                        viewModel.playListen(
                                            it1
                                        )
                                    }
                                },
                                goToArtistPage = goToArtist,
                                isLoading = false
                            )
                        }

                        is SearchData.Albums -> {
                            Albums(
                                uiState = result.data,
                                goToAlbum = goToAlbum,
                                isLoading = false
                            )
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun UserList(
    uiState: UserListUiState,
    /** Must return if the operation was successful.*/
    onFollowClick: (User, Int) -> Unit,
    goToUserPage: (String) -> Unit,
    isLoading: Boolean = false
) {
    val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.View)
    val listState = rememberLazyListState()
    var height by remember { mutableIntStateOf(0) }

    val count by remember {
        derivedStateOf {
            if (height == 0) return@derivedStateOf 0
            listState.layoutInfo.viewportSize.height / height
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        if (isLoading) {
            item {
                ShimmerUserItem(
                    shimmerInstance,
                    modifier = Modifier.onSizeChanged { height = it.height }
                )
            }
            items(count) {
                ShimmerUserItem(shimmerInstance)
            }
        } else {
                itemsIndexed(uiState.userList) { index, user ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { goToUserPage(user.username) }
                            .padding(
                                horizontal = ListenBrainzTheme.paddings.horizontal,
                                vertical = 4.dp
                            ),
                        shape = ListenBrainzTheme.shapes.listenCardSmall,
                        color = ListenBrainzTheme.colorScheme.level1,
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ListenBrainzTheme.sizes.listenCardHeight)
                                .padding(horizontal = ListenBrainzTheme.paddings.coverArtAndTextGap),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = "Profile",
                                tint = ListenBrainzTheme.colorScheme.hint
                            )

                            Spacer(modifier = Modifier.width(ListenBrainzTheme.paddings.coverArtAndTextGap))

                            Text(
                                text = user.username,
                                color = ListenBrainzTheme.colorScheme.text,
                                fontWeight = FontWeight.Bold,
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            FollowButton(
                                isFollowedState = uiState.isFollowedList.getOrNull(index) == true
                            ) {
                                uiState.userList.getOrNull(index)?.let {
                                    onFollowClick(it, index)
                                }
                            }
                            Spacer(modifier = Modifier.width(5.dp))
                        }
                    }
                }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun Playlists(
    uiState: PlayListSearchUiState,
    goToPlaylist: (String) -> Unit,
    isLoading: Boolean = false
) {
    val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.View)
    val listState = rememberLazyListState()
    var height by remember { mutableIntStateOf(0) }

    val count by remember {
        derivedStateOf {
            if (height == 0) return@derivedStateOf 0
            listState.layoutInfo.viewportSize.height / height
        }
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        if (isLoading) {
            item {
                ShimmerPlaylistAndArtistItem(
                    shimmerInstance,
                    modifier = Modifier.onSizeChanged { height = it.height }
                )
            }
            items(count) {
                ShimmerPlaylistAndArtistItem(shimmerInstance)
            }
        } else {
            items(uiState.playlists) { playlist ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { goToPlaylist(playlist.mbid) }
                        .padding(ListenBrainzTheme.paddings.lazyListAdjacent),
                    shape = ListenBrainzTheme.shapes.listenCardSmall,
                    color = ListenBrainzTheme.colorScheme.level1,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ListenBrainzTheme.sizes.listenCardHeight)
                            .padding(horizontal = 15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            TitleAndSubtitle(
                                title = playlist.title,
                                artists = listOf(),
                                titleColor = ListenBrainzTheme.colorScheme.lbSignature,
                                goToArtistPage = {}
                            )
                            Text(
                                text = removeHtmlTags(playlist.description),
                                style = ListenBrainzTheme.textStyles.listenSubtitle,
                                color = ListenBrainzTheme.colorScheme.listenText.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.Center
                        ) {
                            TitleAndSubtitle(
                                title = playlist.creator,
                                artists = listOf(),
                                titleColor = ListenBrainzTheme.colorScheme.lbSignature,
                                goToArtistPage = {}
                            )
                            Text(
                                text = formatSearchDate(playlist.date),
                                style = ListenBrainzTheme.textStyles.listenSubtitle,
                                color = ListenBrainzTheme.colorScheme.listenText.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Artists(
    uiState: ArtistSearchUiState,
    goToArtist: (String) -> Unit,
    isLoading: Boolean = false
) {
    val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.View)
    val listState = rememberLazyListState()
    var height by remember { mutableIntStateOf(0) }

    val count by remember {
        derivedStateOf {
            if (height == 0) return@derivedStateOf 0
            listState.layoutInfo.viewportSize.height / height
        }
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        if (isLoading) {
            item {
                ShimmerPlaylistAndArtistItem(
                    shimmerInstance,
                    modifier = Modifier.onSizeChanged { height = it.height }
                )
            }
            items(count) {
                ShimmerPlaylistAndArtistItem(shimmerInstance)
            }
        } else {
            items(uiState.artists) { artist ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { goToArtist(artist.id) }
                        .padding(ListenBrainzTheme.paddings.lazyListAdjacent),
                    shape = ListenBrainzTheme.shapes.listenCardSmall,
                    color = ListenBrainzTheme.colorScheme.level1,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ListenBrainzTheme.sizes.listenCardHeight)
                            .padding(horizontal = 15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            TitleAndSubtitle(
                                title = artist.name,
                                artists = listOf(),
                                titleColor = ListenBrainzTheme.colorScheme.lbSignature,
                                goToArtistPage = {}
                            )
                            Text(
                                text = artist.gender,
                                style = ListenBrainzTheme.textStyles.listenSubtitle,
                                color = ListenBrainzTheme.colorScheme.listenText.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.Center
                        ) {
                            TitleAndSubtitle(
                                title = artist.type,
                                artists = listOf(),
                                titleColor = ListenBrainzTheme.colorScheme.lbSignature,
                                goToArtistPage = {}
                            )
                            Text(
                                text = artist.area,
                                style = ListenBrainzTheme.textStyles.listenSubtitle,
                                color = ListenBrainzTheme.colorScheme.listenText.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Tracks(
    uiState: TrackSearchUiState,
    goToTrack: (PlaylistTrack) -> Unit,
    goToArtistPage: (String) -> Unit,
    isLoading: Boolean = false
) {
    val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.View)
    val listState = rememberLazyListState()
    var height by remember { mutableIntStateOf(0) }

    val count by remember {
        derivedStateOf {
            if (height == 0) return@derivedStateOf 0
            listState.layoutInfo.viewportSize.height / height
        }
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        if (isLoading) {
            item {
                ShimmerTrackItem(
                    shimmerInstance,
                    modifier = Modifier.onSizeChanged { height = it.height }
                )
            }
            items(count) {
                ShimmerTrackItem(shimmerInstance)
            }
        } else {
            items(uiState.tracks) { trackData ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    trackData.toMetadata().trackMetadata?.let { track->
                        ListenCardSmall(
                            modifier = Modifier.padding(
                                horizontal = ListenBrainzTheme.paddings.horizontal,
                                vertical = ListenBrainzTheme.paddings.lazyListAdjacent
                            ),
                            onClick = { goToTrack(trackData) },
                            trackName = track.trackName ?: "Unknown Title",
                            artists = track.mbidMapping?.artists ?: listOf(
                                FeedListenArtist(track.artistName ?: "--", null, "")
                            ),
                            coverArtUrl = getCoverArtUrl(
                                caaReleaseMbid = trackData.extension.trackExtensionData.additionalMetadata.caaReleaseMbid,
                                caaId = trackData.extension.trackExtensionData.additionalMetadata.caaId
                            ),
                            goToArtistPage = goToArtistPage,
                            trailingContent = {
                                Text(
                                    modifier = Modifier
                                        .padding(bottom = 4.dp),
                                    text = formatDurationSeconds(
                                        trackData.duration?.div(1000) ?: 0
                                    ),
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = ListenBrainzTheme.colorScheme.listenText.copy(alpha = 0.8f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Albums(
    uiState: AlbumSearchUiState,
    goToAlbum: (String) -> Unit,
    isLoading: Boolean = false
) {
    val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.View)
    val listState = rememberLazyListState()
    var height by remember { mutableIntStateOf(0) }

    val count by remember {
        derivedStateOf {
            if (height == 0) return@derivedStateOf 0
            listState.layoutInfo.viewportSize.height / height
        }
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        if (isLoading) {
            item {
                ShimmerAlbumItem(
                    shimmerInstance,
                    modifier = Modifier.onSizeChanged { height = it.height }
                )
            }
            items(count) {
                ShimmerAlbumItem(shimmerInstance)
            }
        } else {
            items(uiState.albums) { album ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    ListenCardSmall(
                        modifier = Modifier.padding(
                            horizontal = ListenBrainzTheme.paddings.horizontal,
                            vertical = ListenBrainzTheme.paddings.lazyListAdjacent
                        ),
                        trackName = album.title,
                        onClick = { goToAlbum(album.id) },
                        coverArtUrl = album.coverArtUrl,
                        artists = album.artistCredit.map {
                            FeedListenArtist(
                                artistMbid = it.artist?.id,
                                artistCreditName = it.name ?: "",
                                joinPhrase = it.joinphrase
                            )
                        },
                        goToArtistPage = {},
                        trailingContent = {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.End
                            ) {
                                TitleAndSubtitle(
                                    title = album.type,
                                    artists = listOf(),
                                    titleColor = ListenBrainzTheme.colorScheme.lbSignature,
                                    goToArtistPage = {}
                                )
                                Text(
                                    text = album.firstReleaseDate,
                                    style = ListenBrainzTheme.textStyles.listenSubtitle,
                                    color = ListenBrainzTheme.colorScheme.listenText.copy(alpha = 0.7f)
                                )
                            }
                            Spacer(modifier = Modifier.padding(end = 10.dp))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ShimmerUserItem(
    shimmer: Shimmer,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = ListenBrainzTheme.paddings.horizontal,
                vertical = 4.dp
            )
            .height(ListenBrainzTheme.sizes.listenCardHeight)
            .background(
                Color.Gray.copy(alpha = 0.1f),
                RoundedCornerShape(6.dp)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.padding(horizontal = ListenBrainzTheme.paddings.coverArtAndTextGap)
        ) {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = "Profile",
                tint = ListenBrainzTheme.colorScheme.hint
            )
        }

        Spacer(modifier = Modifier.width(ListenBrainzTheme.paddings.coverArtAndTextGap))
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(20.dp)
                    .shimmer(shimmer)
                    .background(Color.Gray.copy(alpha = 0.8f), RoundedCornerShape(2.dp))
            )
        }
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(end = 13.dp)
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.End
        ) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(20.dp)
                    .shimmer(shimmer)
                    .background(Color.Gray.copy(alpha = 0.8f), RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
fun ShimmerPlaylistAndArtistItem(
    shimmer: Shimmer,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = ListenBrainzTheme.paddings.horizontal,
                vertical = ListenBrainzTheme.paddings.lazyListAdjacent
            )
            .height(ListenBrainzTheme.sizes.listenCardHeight)
            .background(
                Color.Gray.copy(alpha = 0.1f),
                RoundedCornerShape(6.dp)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.fillMaxHeight()
                .padding(start = 15.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(12.dp)
                    .shimmer(shimmer)
                    .background(Color.Gray.copy(alpha = 0.8f), RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.height(5.dp))
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(10.dp)
                    .shimmer(shimmer)
                    .background(Color.Gray.copy(alpha = 0.8f), RoundedCornerShape(2.dp))
            )
        }
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(end = 15.dp)
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.End
        ) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(10.dp)
                    .shimmer(shimmer)
                    .background(Color.Gray.copy(alpha = 0.8f), RoundedCornerShape(3.dp))
            )
            Spacer(modifier = Modifier.height(5.dp))
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(8.dp)
                    .shimmer(shimmer)
                    .background(Color.Gray.copy(alpha = 0.8f), RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
fun ShimmerAlbumItem(
    shimmer: Shimmer,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = ListenBrainzTheme.paddings.horizontal,
                vertical = ListenBrainzTheme.paddings.lazyListAdjacent
            )
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
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(end = 10.dp)
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.End
        ) {
            Box(
                modifier = Modifier
                    .width(60.dp)
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
fun ShimmerTrackItem(
    shimmer: Shimmer,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = ListenBrainzTheme.paddings.horizontal,
                vertical = ListenBrainzTheme.paddings.lazyListAdjacent
            )
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
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(end = 10.dp)
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.End
        ) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(10.dp)
                    .shimmer(shimmer)
                    .background(
                        Color.Gray.copy(alpha = 0.8f),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatSearchDate(date: String): String {
    return try {
        val inputFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        val parsedDate = java.time.OffsetDateTime.parse(date, inputFormatter)
        val outputFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
        parsedDate.format(outputFormatter)
    } catch (e: Exception) {
        date
    }
}