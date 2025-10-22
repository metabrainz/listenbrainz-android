package org.listenbrainz.android.ui.screens.profile.stats

import android.text.TextUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.of
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.Dimensions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.listenbrainz.android.R
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.model.SocialUiState
import org.listenbrainz.android.model.TrackMetadata
import org.listenbrainz.android.model.user.Artist
import org.listenbrainz.android.model.user.ListeningActivity
import org.listenbrainz.android.model.user.TopArtists
import org.listenbrainz.android.model.user.TopArtistsPayload
import org.listenbrainz.android.ui.components.ChipItem
import org.listenbrainz.android.ui.components.ErrorBar
import org.listenbrainz.android.ui.components.ListenCardSmallDefault
import org.listenbrainz.android.ui.components.SelectionChipBar
import org.listenbrainz.android.ui.components.SuccessBar
import org.listenbrainz.android.ui.screens.artist.formatNumber
import org.listenbrainz.android.ui.screens.profile.ProfileUiState
import org.listenbrainz.android.ui.screens.profile.StatsTabUIState
import org.listenbrainz.android.ui.screens.profile.listens.LoadMoreButton
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_purple_night
import org.listenbrainz.android.util.PreviewSurface
import org.listenbrainz.android.util.Utils.LaunchedEffectUnit
import org.listenbrainz.android.util.Utils.Spacer
import org.listenbrainz.android.util.Utils.getCoverArtUrl
import org.listenbrainz.android.util.getStringResource
import org.listenbrainz.android.viewmodel.SocialViewModel
import org.listenbrainz.android.viewmodel.UserViewModel

@Composable
fun StatsScreen(
    username: String?,
    viewModel: UserViewModel,
    socialViewModel: SocialViewModel,
    snackbarState: SnackbarHostState,
    goToArtistPage: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val socialUiState by socialViewModel.uiState.collectAsState()

    StatsScreen(
        username = username,
        uiState = uiState,
        fetchListeningActivity = { range, scope ->
            viewModel.getListeningActivity(username, range, scope)
        },
        fetchTopArtists = {
            viewModel.getUserTopArtists(it)
        },
        fetchTopAlbums = {
            viewModel.getUserTopAlbums(it)
        },
        fetchTopSongs = {
            viewModel.getUserTopSongs(it)
        },
        playListen = {
            socialViewModel.playListen(it)
        },
        snackbarState = snackbarState,
        socialUiState = socialUiState,
        onErrorShown = {
            socialViewModel.clearErrorFlow()
        },
        onMessageShown = {
            socialViewModel.clearMsgFlow()
        },
        goToArtistPage = goToArtistPage
    )
}

@Composable
fun StatsScreen(
    username: String?,
    uiState: ProfileUiState,
    fetchListeningActivity: suspend (StatsRange, DataScope) -> Unit,
    fetchTopArtists: suspend (String?) -> Unit,
    fetchTopAlbums: suspend (String?) -> Unit,
    fetchTopSongs: suspend (String?) -> Unit,
    playListen: (TrackMetadata) -> Unit,
    snackbarState: SnackbarHostState,
    socialUiState: SocialUiState,
    onErrorShown: () -> Unit,
    onMessageShown: () -> Unit,
    goToArtistPage: (String) -> Unit,
) {
    var statsRangeState by remember {
        mutableStateOf(StatsRange.THIS_WEEK)
    }
    var dataScopeState by remember {
        mutableStateOf(DataScope.USER)
    }
    var currentTabSelection by remember {
        mutableStateOf(CategoryState.ARTISTS)
    }
    var artistsCollapseState by remember {
        mutableStateOf(true)
    }
    var albumsCollapseState by remember {
        mutableStateOf(true)
    }
    var songsCollapseState by remember {
        mutableStateOf(true)
    }

    // Fetch listening activity when range or scope changes
    LaunchedEffectUnit {
        snapshotFlow {
            statsRangeState to dataScopeState
        }.collectLatest { (range, scope) ->
            fetchListeningActivity(range, scope)
        }
    }

    // Fetch category data (artists/albums/songs) when tab selection changes
    LaunchedEffectUnit {
        snapshotFlow { currentTabSelection }.collectLatest {
            when (it) {
                CategoryState.ARTISTS -> {
                    if (uiState.statsTabUIState.topArtists == null) {
                        fetchTopArtists(username)
                    }
                }

                CategoryState.ALBUMS -> {
                    if (uiState.statsTabUIState.topAlbums == null) {
                        fetchTopAlbums(username)
                    }
                }

                CategoryState.SONGS -> {
                    if (uiState.statsTabUIState.topSongs == null) {
                        fetchTopSongs(username)
                    }
                }
            }
        }
    }

    val topArtists = when (artistsCollapseState) {
        true -> uiState.statsTabUIState.topArtists?.get(statsRangeState)?.payload?.artists?.take(5)
            ?: listOf()

        false -> uiState.statsTabUIState.topArtists?.get(statsRangeState)?.payload?.artists
            ?: listOf()
    }

    val topAlbums = when (albumsCollapseState) {
        true -> uiState.statsTabUIState.topAlbums?.get(statsRangeState)?.payload?.releases?.take(5)
            ?: listOf()

        false -> uiState.statsTabUIState.topAlbums?.get(statsRangeState)?.payload?.releases
            ?: listOf()
    }

    val topSongs = when (songsCollapseState) {
        true -> uiState.statsTabUIState.topSongs?.get(statsRangeState)?.payload?.recordings?.take(5)
            ?: listOf()

        false -> uiState.statsTabUIState.topSongs?.get(statsRangeState)?.payload?.recordings
            ?: listOf()
    }

    LazyColumn(modifier = Modifier.testTag("statsScreenScrollableContainer")) {
        item {
            val chipItems = listOf(
                ChipItem(id = "THIS_WEEK", label = StatsRange.THIS_WEEK.rangeString),
                ChipItem(id = "THIS_MONTH", label = StatsRange.THIS_MONTH.rangeString),
                ChipItem(id = "THIS_YEAR", label = StatsRange.THIS_YEAR.rangeString),
                ChipItem(id = "LAST_WEEK", label = StatsRange.LAST_WEEK.rangeString),
                ChipItem(id = "LAST_MONTH", label = StatsRange.LAST_MONTH.rangeString),
                ChipItem(id = "LAST_YEAR", label = StatsRange.LAST_YEAR.rangeString),
                ChipItem(id = "ALL_TIME", label = StatsRange.ALL_TIME.rangeString)
            )
            SelectionChipBar(
                items = chipItems,
                selectedItemId = statsRangeState.name,
                onItemSelected = { data ->
                    statsRangeState = StatsRange.valueOf(data.id)
                }
            )
        }
        item {
            val userGlobalChips = listOf(
                ChipItem(id = "USER", label = username.orEmpty()),
                ChipItem(
                    id = "GLOBAL",
                    label = "Global",
                    icon = painterResource(id = R.drawable.globe)
                )
            )
            SelectionChipBar(
                items = userGlobalChips,
                selectedItemId = dataScopeState.name,
                onItemSelected = { data ->
                    dataScopeState = DataScope.valueOf(data.id)
                }
            )
        }
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Box {
                Column {
                    Text(
                        text = "Listening activity",
                        color = ListenBrainzTheme.colorScheme.text,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp),
                        modifier = Modifier.padding(start = 10.dp)
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    val data = uiState.statsTabUIState.userListeningActivity[Pair(
                        dataScopeState,
                        statsRangeState
                    )] ?: listOf()
                    if (data.isNotEmpty()) {
                        val modelProducer = remember {
                            CartesianChartModelProducer()
                        }

                        val splitIndex = when (statsRangeState) {
                            StatsRange.THIS_WEEK -> 7
                            StatsRange.LAST_WEEK -> 7
                            StatsRange.THIS_MONTH -> 30
                            StatsRange.LAST_MONTH -> 30
                            StatsRange.LAST_YEAR -> 12
                            StatsRange.THIS_YEAR -> 12
                            StatsRange.ALL_TIME -> 0
                        }
                        val listenCountsFirstPart = data.subList(0, minOf(splitIndex, data.size))
                            .mapNotNull { it?.listenCount }
                        val listenCountsSecondPart = if (data.size > splitIndex) {
                            data.subList(splitIndex, data.size).mapNotNull { it?.listenCount }
                        } else {
                            emptyList()
                        }

                        LaunchedEffect(data) {
                            withContext(Dispatchers.Default) {
                                while (isActive) {
                                    modelProducer.runTransaction {
                                        columnSeries {
                                            if (listenCountsFirstPart.isNotEmpty()) {
                                                series(y = listenCountsFirstPart)
                                            }
                                            if (listenCountsSecondPart.isNotEmpty()) {
                                                series(y = listenCountsSecondPart)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        val columnProvider =
                            ColumnCartesianLayer.ColumnProvider.series(
                                listOf(
                                    rememberLineComponent(
                                        color = ListenBrainzTheme.colorScheme.lbSignature,
                                        thickness = 25.dp,
                                    ),
                                    rememberLineComponent(
                                        color = ListenBrainzTheme.colorScheme.lbSignatureInverse,
                                        thickness = 25.dp,
                                    )
                                )
                            )

                        CartesianChartHost(
                            modifier = Modifier
                                .padding(start = 11.dp, end = 11.dp)
                                .height(250.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .testTag("listeningActivityChart"),
                            chart = rememberCartesianChart(
                                rememberColumnCartesianLayer(
                                    columnProvider = columnProvider,
                                    spacing = 25.dp,
                                    mergeMode = { ColumnCartesianLayer.MergeMode.Grouped },
                                ),
                                startAxis = rememberStartAxis(
                                    label = rememberTextComponent(
                                        color = ListenBrainzTheme.colorScheme.text,
                                        textSize = 11.sp,
                                        padding = Dimensions.of(ListenBrainzTheme.paddings.tinyPadding)
                                    )
                                ),
                                bottomAxis = rememberBottomAxis(
                                    label = rememberTextComponent(
                                        ellipsize = TextUtils.TruncateAt.MARQUEE,
                                        textSize = 11.sp,
                                        color = ListenBrainzTheme.colorScheme.text,
                                        padding = Dimensions.of(ListenBrainzTheme.paddings.tinyPadding)
                                    ),
                                    guideline = null,
                                    valueFormatter = { value, chartValues, verticalAxisPosition ->
                                        valueFormatter(value.toInt(), statsRangeState)
                                    },
                                ),
                            ),
                            modelProducer = modelProducer,
                        )

                    } else {
                        Text(
                            "There are no statistics available for this user for this period",
                            color = ListenBrainzTheme.colorScheme.text,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }

                }
            }
        }

        item {
            Column(
                modifier = Modifier.padding(top = 30.dp)
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = ListenBrainzTheme.paddings.horizontal),
                    text = "Top ...",
                    color = ListenBrainzTheme.colorScheme.text,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp)
                )

                Spacer(10.dp)

                val context = LocalContext.current
                SelectionChipBar(
                    items = remember {
                        CategoryState.entries.map {
                            ChipItem(it.text.getStringResource(context), it.ordinal)
                        }
                    },
                    selectedItemId = currentTabSelection.ordinal,
                ) { chip ->
                    currentTabSelection = CategoryState.entries[chip.id]
                }

                when (currentTabSelection) {
                    CategoryState.ARTISTS ->
                        if (uiState.statsTabUIState.isLoading) {
                            CircularProgressIndicator()
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                topArtists.map { topArtist ->
                                    ArtistCard(
                                        modifier = Modifier.padding(
                                            vertical = ListenBrainzTheme.paddings.lazyListAdjacent,
                                            horizontal = ListenBrainzTheme.paddings.horizontal
                                        ),
                                        artistName = topArtist.artistName ?: "",
                                        listenCountLabel = formatNumber(topArtist.listenCount ?: 0)
                                    ) {
                                        if (topArtist.artistMbid != null) {
                                            goToArtistPage(topArtist.artistMbid)
                                        }
                                    }
                                }

                                if ((uiState.statsTabUIState.topArtists?.size ?: 0) > 5) {
                                    LoadMoreButton(
                                        modifier = Modifier.padding(
                                            vertical = 16.dp,
                                            horizontal = ListenBrainzTheme.paddings.horizontal
                                        ),
                                        state = artistsCollapseState
                                    ) {
                                        artistsCollapseState = !artistsCollapseState
                                    }
                                }
                            }
                        }

                    CategoryState.ALBUMS ->
                        if (uiState.statsTabUIState.isLoading) {
                            CircularProgressIndicator(
                                color = lb_purple_night
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                topAlbums.mapIndexed { index, topAlbum ->
                                    val metadata = topAlbum.toMetadata()
                                    ListenCardSmallDefault(
                                        metadata = metadata,
                                        coverArtUrl = getCoverArtUrl(
                                            topAlbum.caaReleaseMbid,
                                            topAlbum.caaId
                                        ),
                                        modifier = Modifier
                                            .padding(
                                                vertical = ListenBrainzTheme.paddings.lazyListAdjacent,
                                                horizontal = ListenBrainzTheme.paddings.horizontal
                                            ),
                                        titleColor = ListenBrainzTheme.colorScheme.followerChipSelected,
                                        subtitleColor = ListenBrainzTheme.colorScheme.listenText.copy(
                                            alpha = 0.7f
                                        ),
                                        trailingContent = {
                                            if (topAlbum.listenCount != null) {
                                                ListenCountChip(formatNumber(topAlbum.listenCount))
                                            }
                                        },
                                        goToArtistPage = goToArtistPage,
                                        onDropdownError = {
                                            snackbarState.showSnackbar(it.toast)
                                        },
                                        onDropdownSuccess = {
                                            snackbarState.showSnackbar(it)
                                        },
                                        onClick = {
                                            metadata.trackMetadata?.let { playListen(it) }
                                        }
                                    )
                                }

                                if ((uiState.statsTabUIState.topAlbums?.size ?: 0) > 5) {
                                    LoadMoreButton(
                                        modifier = Modifier.padding(
                                            vertical = 16.dp,
                                            horizontal = ListenBrainzTheme.paddings.horizontal
                                        ),
                                        state = albumsCollapseState
                                    ) {
                                        albumsCollapseState = !albumsCollapseState
                                    }
                                }
                            }
                        }

                    CategoryState.SONGS -> {
                        if (uiState.statsTabUIState.isLoading) {
                            CircularProgressIndicator()
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                topSongs.mapIndexed { index, topSong ->
                                    val metadata =
                                        Metadata(trackMetadata = topSong.toTrackMetadata())
                                    ListenCardSmallDefault(
                                        metadata = metadata,
                                        coverArtUrl = getCoverArtUrl(
                                            topSong.caaReleaseMbid,
                                            topSong.caaId
                                        ),
                                        modifier = Modifier
                                            .padding(
                                                vertical = ListenBrainzTheme.paddings.lazyListAdjacent,
                                                horizontal = ListenBrainzTheme.paddings.horizontal
                                            ),
                                        titleColor = ListenBrainzTheme.colorScheme.followerChipSelected,
                                        subtitleColor = ListenBrainzTheme.colorScheme.listenText.copy(
                                            alpha = 0.7f
                                        ),
                                        trailingContent = {
                                            if (topSong.listenCount != null) {
                                                ListenCountChip(formatNumber(topSong.listenCount))
                                            }
                                        },
                                        goToArtistPage = goToArtistPage,
                                        onDropdownError = {
                                            snackbarState.showSnackbar(it.toast)
                                        },
                                        onDropdownSuccess = {
                                            snackbarState.showSnackbar(it)
                                        }
                                    ) {
                                        val trackMetadata = metadata.trackMetadata
                                        if (trackMetadata != null) {
                                            playListen(trackMetadata)
                                        }
                                    }
                                }

                                if ((uiState.statsTabUIState.topSongs?.size ?: 0) > 5) {
                                    LoadMoreButton(
                                        modifier = Modifier.padding(
                                            vertical = 16.dp,
                                            horizontal = ListenBrainzTheme.paddings.horizontal
                                        ),
                                        state = songsCollapseState
                                    ) {
                                        songsCollapseState = !songsCollapseState
                                    }
                                }
                            }
                        }
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

@Composable
fun ArtistCard(
    modifier: Modifier = Modifier,
    artistName: String,
    listenCountLabel: String? = null,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = true) { onClick() },
        shape = ListenBrainzTheme.shapes.listenCardSmall,
        shadowElevation = 4.dp,
        color = ListenBrainzTheme.colorScheme.followerCardColor
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ListenBrainzTheme.sizes.listenCardHeight),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        artistName,
                        color = ListenBrainzTheme.colorScheme.followerChipSelected,
                        style = ListenBrainzTheme.textStyles.listenTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (listenCountLabel != null) {
                    Box(
                        modifier = modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ListenCountChip(listenCountLabel)
                    }
                }
            }
        }
    }
}

@Composable
fun ListenCountChip(
    listenCountLabel: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(ListenBrainzTheme.colorScheme.followerChipSelected)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = listenCountLabel,
            fontWeight = FontWeight.Bold,
            color = ListenBrainzTheme.colorScheme.followerChipUnselected,
            fontSize = 14.sp
        )
    }
}

private fun valueFormatter(value: Int, statsRange: StatsRange): String {
    val label: String = when (statsRange) {
        StatsRange.THIS_WEEK, StatsRange.LAST_WEEK -> when (value % 7) {
            0 -> "Mon"
            1 -> "Tue"
            2 -> "Wed"
            3 -> "Thu"
            4 -> "Fri"
            5 -> "Sat"
            6 -> "Sun"
            else -> ""
        }

        StatsRange.THIS_MONTH, StatsRange.LAST_MONTH -> value.toString()
        StatsRange.THIS_YEAR, StatsRange.LAST_YEAR -> when (value % 12) {
            0 -> "Jan"
            1 -> "Feb"
            2 -> "Mar"
            3 -> "Apr"
            4 -> "May"
            5 -> "Jun"
            6 -> "Jul"
            7 -> "Aug"
            8 -> "Sep"
            9 -> "Oct"
            10 -> "Nov"
            11 -> "Dec"
            else -> ""
        }

        StatsRange.ALL_TIME -> (value + 2002).toString()
    }
    return label
}

@PreviewLightDark
@Composable
private fun StatsScreenPreview() {
    // Create mock data for artists
    val mockArtists = listOf(
        Artist(
            artistMbid = "mbid-1",
            artistName = "Radiohead",
            listenCount = 1234
        ),
        Artist(
            artistMbid = "mbid-2",
            artistName = "Pink Floyd",
            listenCount = 987
        ),
        Artist(
            artistMbid = "mbid-3",
            artistName = "The Beatles",
            listenCount = 876
        ),
        Artist(
            artistMbid = "mbid-4",
            artistName = "Led Zeppelin",
            listenCount = 654
        ),
        Artist(
            artistMbid = "mbid-5",
            artistName = "Queen",
            listenCount = 543
        )
    )

    val mockTopArtists = TopArtists(
        payload = TopArtistsPayload(
            artists = mockArtists,
            count = mockArtists.size,
            fromTs = 0,
            lastUpdated = 0,
            offset = 0,
            range = "week",
            toTs = 0,
            totalArtistCount = mockArtists.size,
            userId = "test_user"
        )
    )

    // Create realistic mock listening activity data for THIS_WEEK (7 days)
    val mockWeeklyActivity = listOf(
        ListeningActivity(
            fromTs = 1704672000,
            toTs = 1704758399,
            listenCount = 23,
            timeRange = "Monday"
        ),
        ListeningActivity(
            fromTs = 1704758400,
            toTs = 1704844799,
            listenCount = 45,
            timeRange = "Tuesday"
        ),
        ListeningActivity(
            fromTs = 1704844800,
            toTs = 1704931199,
            listenCount = 67,
            timeRange = "Wednesday"
        ),
        ListeningActivity(
            fromTs = 1704931200,
            toTs = 1705017599,
            listenCount = 89,
            timeRange = "Thursday"
        ),
        ListeningActivity(
            fromTs = 1705017600,
            toTs = 1705103999,
            listenCount = 112,
            timeRange = "Friday"
        ),
        ListeningActivity(
            fromTs = 1705104000,
            toTs = 1705190399,
            listenCount = 134,
            timeRange = "Saturday"
        ),
        ListeningActivity(
            fromTs = 1705190400,
            toTs = 1705276799,
            listenCount = 78,
            timeRange = "Sunday"
        )
    )

    // Create mock listening activity data for THIS_MONTH (30 days)
    val mockMonthlyActivity = (1..30).map { day ->
        // Simulate varying listening patterns throughout the month
        val listenCount = when {
            day % 7 == 6 || day % 7 == 0 -> (80..150).random() // Weekends - more listening
            day % 7 in 1..5 -> (40..90).random() // Weekdays - moderate listening
            else -> (30..70).random()
        }
        ListeningActivity(
            fromTs = 1704067200 + (day - 1) * 86400,
            toTs = 1704067200 + day * 86400 - 1,
            listenCount = listenCount,
            timeRange = "Day $day"
        )
    }

    // Create mock listening activity data for THIS_YEAR (12 months)
    val mockYearlyActivity = listOf(
        ListeningActivity(
            fromTs = 1704067200,
            toTs = 1706745599,
            listenCount = 1245,
            timeRange = "January"
        ),
        ListeningActivity(
            fromTs = 1706745600,
            toTs = 1709251199,
            listenCount = 1098,
            timeRange = "February"
        ),
        ListeningActivity(
            fromTs = 1709251200,
            toTs = 1711929599,
            listenCount = 1567,
            timeRange = "March"
        ),
        ListeningActivity(
            fromTs = 1711929600,
            toTs = 1714521599,
            listenCount = 1432,
            timeRange = "April"
        ),
        ListeningActivity(
            fromTs = 1714521600,
            toTs = 1717199999,
            listenCount = 1789,
            timeRange = "May"
        ),
        ListeningActivity(
            fromTs = 1717200000,
            toTs = 1719791999,
            listenCount = 1654,
            timeRange = "June"
        ),
        ListeningActivity(
            fromTs = 1719792000,
            toTs = 1722470399,
            listenCount = 1876,
            timeRange = "July"
        ),
        ListeningActivity(
            fromTs = 1722470400,
            toTs = 1725148799,
            listenCount = 2012,
            timeRange = "August"
        ),
        ListeningActivity(
            fromTs = 1725148800,
            toTs = 1727740799,
            listenCount = 1723,
            timeRange = "September"
        ),
        ListeningActivity(
            fromTs = 1727740800,
            toTs = 1730419199,
            listenCount = 1598,
            timeRange = "October"
        ),
        ListeningActivity(
            fromTs = 1730419200,
            toTs = 1733011199,
            listenCount = 1834,
            timeRange = "November"
        ),
        ListeningActivity(
            fromTs = 1733011200,
            toTs = 1735689599,
            listenCount = 1456,
            timeRange = "December"
        )
    )

    // Create mock UI state with multiple time ranges
    val mockUiState = ProfileUiState(
        statsTabUIState = StatsTabUIState(
            isLoading = false,
            topArtists = mapOf(
                StatsRange.THIS_WEEK to mockTopArtists,
                StatsRange.THIS_MONTH to mockTopArtists,
                StatsRange.THIS_YEAR to mockTopArtists,
                StatsRange.LAST_WEEK to mockTopArtists,
                StatsRange.LAST_MONTH to mockTopArtists,
                StatsRange.LAST_YEAR to mockTopArtists,
                StatsRange.ALL_TIME to mockTopArtists
            ),
            userListeningActivity = mapOf(
                // User scope data
                Pair(DataScope.USER, StatsRange.THIS_WEEK) to mockWeeklyActivity,
                Pair(DataScope.USER, StatsRange.THIS_MONTH) to mockMonthlyActivity,
                Pair(DataScope.USER, StatsRange.THIS_YEAR) to mockYearlyActivity,
                Pair(DataScope.USER, StatsRange.LAST_WEEK) to mockWeeklyActivity,
                Pair(DataScope.USER, StatsRange.LAST_MONTH) to mockMonthlyActivity,
                Pair(DataScope.USER, StatsRange.LAST_YEAR) to mockYearlyActivity,
                // Global scope data (slightly different to show variation)
                Pair(
                    DataScope.GLOBAL,
                    StatsRange.THIS_WEEK
                ) to mockWeeklyActivity.mapIndexed { index, activity ->
                    activity.copy(listenCount = (activity.listenCount ?: 0) + (10..30).random())
                },
                Pair(
                    DataScope.GLOBAL,
                    StatsRange.THIS_MONTH
                ) to mockMonthlyActivity.map { activity ->
                    activity.copy(listenCount = (activity.listenCount ?: 0) + (5..15).random())
                },
                Pair(DataScope.GLOBAL, StatsRange.THIS_YEAR) to mockYearlyActivity.map { activity ->
                    activity.copy(listenCount = (activity.listenCount ?: 0) + (100..300).random())
                }
            )
        )
    )

    val mockSocialUiState = SocialUiState(
        searchResult = emptyList(),
        error = null,
        successMsgId = null
    )

    PreviewSurface {
        StatsScreen(
            username = "test_user",
            uiState = mockUiState,
            fetchListeningActivity = { _, _ -> },
            fetchTopArtists = {},
            fetchTopAlbums = {},
            fetchTopSongs = {},
            playListen = {},
            snackbarState = remember { SnackbarHostState() },
            socialUiState = mockSocialUiState,
            onErrorShown = {},
            onMessageShown = {},
            goToArtistPage = {}
        )
    }
}