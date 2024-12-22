package org.listenbrainz.android.ui.screens.profile.stats

import CategoryState
import android.text.TextUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.listenbrainz.android.R
import org.listenbrainz.android.model.Listen
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.model.SocialUiState
import org.listenbrainz.android.model.TrackMetadata
import org.listenbrainz.android.model.feed.FeedListenArtist
import org.listenbrainz.android.model.feed.ReviewEntityType
import org.listenbrainz.android.ui.components.ErrorBar
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.components.SuccessBar
import org.listenbrainz.android.ui.components.dialogs.Dialog
import org.listenbrainz.android.ui.components.dialogs.rememberDialogsState
import org.listenbrainz.android.ui.screens.artist.formatNumber
import org.listenbrainz.android.ui.screens.feed.FeedUiState
import org.listenbrainz.android.ui.screens.feed.SocialDropdown
import org.listenbrainz.android.ui.screens.profile.ProfileUiState
import org.listenbrainz.android.ui.screens.profile.listens.Dialogs
import org.listenbrainz.android.ui.screens.profile.listens.ListenDialogBundleKeys
import org.listenbrainz.android.ui.screens.profile.listens.LoadMoreButton
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.app_bg_secondary_dark
import org.listenbrainz.android.ui.theme.lb_purple_night
import org.listenbrainz.android.util.Utils.getCoverArtUrl
import org.listenbrainz.android.viewmodel.FeedViewModel
import org.listenbrainz.android.viewmodel.SocialViewModel
import org.listenbrainz.android.viewmodel.UserViewModel

@Composable
fun StatsScreen(
    username: String?,
    viewModel: UserViewModel,
    socialViewModel: SocialViewModel,
    feedViewModel : FeedViewModel,
    snackbarState : SnackbarHostState,
    goToArtistPage: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val socialUiState by socialViewModel.uiState.collectAsState()
    val feedUiState by feedViewModel.uiState.collectAsState()
    val dropdownItemIndex: MutableState<Int?> = rememberSaveable {
        mutableStateOf(null)
    }
    val statsRangeState: MutableState<StatsRange> = remember {
        mutableStateOf(StatsRange.THIS_WEEK)
    }
    val userGlobalState: MutableState<UserGlobal> = remember {
        mutableStateOf(UserGlobal.USER)
    }

    StatsScreen(
        username = username,
        uiState = uiState,
        statsRangeState = statsRangeState.value,
        setStatsRange = {
            range -> statsRangeState.value = range
        },
        userGlobalState = userGlobalState.value,
        setUserGlobal = {
            selection -> userGlobalState.value = selection
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
        dropdownItemIndex = dropdownItemIndex,
        feedUiState = feedUiState,
        playListen = {
            socialViewModel.playListen(it)
        },
        snackbarState = snackbarState,
        socialUiState = socialUiState,
        onRecommend = {metadata ->
            socialViewModel.recommend(metadata)
            dropdownItemIndex.value = null
        },
        onErrorShown = {
            socialViewModel.clearErrorFlow()
        },
        onMessageShown = {
            socialViewModel.clearMsgFlow()
        },
        onPin = {
                metadata, blurbContent -> socialViewModel.pin(metadata , blurbContent)
            dropdownItemIndex.value = null
        },
        searchUsers = {
                query -> feedViewModel.searchUser(query)
        },
        isCritiqueBrainzLinked = {
            feedViewModel.isCritiqueBrainzLinked()
        },
        onReview = {
                type, blurbContent, rating, locale, metadata ->  socialViewModel.review(metadata , type , blurbContent , rating , locale)
        },
        onPersonallyRecommend = {
                metadata, users, blurbContent ->  socialViewModel.personallyRecommend(metadata, users, blurbContent)
        },
        goToArtistPage = goToArtistPage
    )
}

@Composable
fun StatsScreen(
    username: String?,
    uiState: ProfileUiState,
    uriHandler: UriHandler = LocalUriHandler.current,
    statsRangeState: StatsRange,
    setStatsRange: (StatsRange) -> Unit,
    userGlobalState: UserGlobal,
    setUserGlobal: (UserGlobal) -> Unit,
    fetchTopArtists: suspend (String?) -> Unit,
    fetchTopAlbums: suspend (String?) -> Unit,
    fetchTopSongs: suspend (String?) -> Unit,
    dropdownItemIndex : MutableState<Int?>,
    feedUiState: FeedUiState,
    playListen: (TrackMetadata) -> Unit,
    snackbarState: SnackbarHostState,
    socialUiState: SocialUiState,
    onRecommend : (metadata : Metadata) -> Unit,
    onErrorShown : () -> Unit,
    onMessageShown : () -> Unit,
    onPin : (metadata : Metadata , blurbContent : String) -> Unit,
    searchUsers: (String) -> Unit,
    isCritiqueBrainzLinked: suspend () -> Boolean?,
    onReview: (type: ReviewEntityType, blurbContent: String, rating: Int?, locale: String, metadata: Metadata) -> Unit,
    onPersonallyRecommend: (metadata: Metadata, users: List<String>, blurbContent: String) -> Unit,
    goToArtistPage: (String) -> Unit,
) {
    val currentTabSelection: MutableState<CategoryState> = remember {
        mutableStateOf(CategoryState.ARTISTS)
    }
    val artistsCollapseState: MutableState<Boolean> = remember {
        mutableStateOf(true)
    }
    val albumsCollapseState: MutableState<Boolean> = remember {
        mutableStateOf(true)
    }
    val songsCollapseState: MutableState<Boolean> = remember {
        mutableStateOf(true)
    }

    val dialogsState = rememberDialogsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    when(currentTabSelection.value){
        CategoryState.ARTISTS -> {
            if(uiState.statsTabUIState.topArtists == null){
                LaunchedEffect(Unit) {
                    fetchTopArtists(username)
                }
            }

        }
        CategoryState.ALBUMS -> {
            if(uiState.statsTabUIState.topAlbums == null){
                LaunchedEffect(Unit) {
                    fetchTopAlbums(username)
                }
            }
        }
        CategoryState.SONGS -> {
            if(uiState.statsTabUIState.topSongs == null){
                LaunchedEffect(Unit) {
                    fetchTopSongs(username)
                }
            }
        }
    }

    val topArtists = when(artistsCollapseState.value){
        true -> uiState.statsTabUIState.topArtists?.get(statsRangeState)?.payload?.artists?.take(5) ?: listOf()
        false -> uiState.statsTabUIState.topArtists?.get(statsRangeState)?.payload?.artists ?: listOf()
    }

    val topAlbums = when(albumsCollapseState.value){
        true -> uiState.statsTabUIState.topAlbums?.get(statsRangeState)?.payload?.releases?.take(5) ?: listOf()
        false -> uiState.statsTabUIState.topAlbums?.get(statsRangeState)?.payload?.releases ?: listOf()
    }

    val topSongs = when(songsCollapseState.value){
        true -> uiState.statsTabUIState.topSongs?.get(statsRangeState)?.payload?.recordings?.take(5) ?: listOf()
        false -> uiState.statsTabUIState.topSongs?.get(statsRangeState)?.payload?.recordings ?: listOf()
    }

    LazyColumn (modifier = Modifier.testTag("statsScreenScrollableContainer")) {
        item {
            RangeBar(
                statsRangeState = statsRangeState,
                onClick = {
                    range ->
                    setStatsRange(range)
                }
            )
        }
        item {
            UserGlobalBar(
                userGlobalState = userGlobalState,
                onUserGlobalChange = setUserGlobal,
                username = username
            )
        }
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Box{
                Column {
                    Text(
                        text = "Listening activity",
                        color = ListenBrainzTheme.colorScheme.text,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp),
                        modifier = Modifier.padding(start = 10.dp)
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    val data = uiState.statsTabUIState.userListeningActivity[Pair(userGlobalState, statsRangeState)] ?: listOf()
                    if (data.isNotEmpty()) {
                        val modelProducer = remember {
                            CartesianChartModelProducer()
                        }

                        val splitIndex = when(statsRangeState){
                            StatsRange.THIS_WEEK -> 7
                                StatsRange.LAST_WEEK -> 7
                                StatsRange.THIS_MONTH -> 30
                                StatsRange.LAST_MONTH -> 30
                                StatsRange.LAST_YEAR -> 12
                                StatsRange.THIS_YEAR -> 12
                            StatsRange.ALL_TIME -> 0
                        }
                        val listenCountsFirstPart = data.subList(0, minOf(splitIndex, data.size)).mapNotNull { it?.listenCount }
                        val listenCountsSecondPart = if (data.size > splitIndex) {
                            data.subList(splitIndex, data.size).mapNotNull { it?.listenCount }
                        } else {
                            emptyList()
                        }

                        LaunchedEffect(data) {
                            withContext(Dispatchers.Default) {
                                while(isActive){
                                    modelProducer.runTransaction { columnSeries {
                                        if(listenCountsFirstPart.isNotEmpty()){
                                            series(y = listenCountsFirstPart)
                                        }
                                        if(listenCountsSecondPart.isNotEmpty()){
                                            series(y = listenCountsSecondPart)
                                        }
                                    } }
                                }
                            }
                        }

                        val columnProvider =
                            ColumnCartesianLayer.ColumnProvider.series(
                                listOf(
                                    rememberLineComponent(
                                        color = Color(0xFF353070),
                                        thickness = 25.dp,
                                    ),
                                    rememberLineComponent(
                                        color = Color(0xFFEB743B),
                                        thickness = 25.dp,
                                    )
                                )
                            )


                        CartesianChartHost(
                            modifier = Modifier
                                .padding(start = 11.dp, end = 11.dp)
                                .height(250.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFe0e5de))
                                .testTag("listeningActivityChart"),
                            chart = rememberCartesianChart(
                                rememberColumnCartesianLayer(
                                    columnProvider = columnProvider,
                                    spacing = 25.dp,
                                    mergeMode = { ColumnCartesianLayer.MergeMode.Grouped },
                                ),
                                startAxis = rememberStartAxis(
                                    label = rememberTextComponent(
                                        color = Color.Black,
                                        textSize = 11.sp,
                                        padding = Dimensions.of(ListenBrainzTheme.paddings.tinyPadding)
                                    )
                                ),
                                bottomAxis = rememberBottomAxis(
                                    label = rememberTextComponent (
                                        ellipsize = TextUtils.TruncateAt.MARQUEE,
                                        textSize = 11.sp,
                                        color = Color.Black,
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

                    }
                    else{
                        Text("There are no statistics available for this user for this period", color = ListenBrainzTheme.colorScheme.text, modifier = Modifier.padding(start = 10.dp))
                    }

                }
            }
        }

        item {
           Column (modifier = Modifier
               .padding(start = 10.dp, top = 30.dp)
               ) {
                    Text("Top ...", color = ListenBrainzTheme.colorScheme.text, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp))
               Box(modifier = Modifier.height(10.dp))
                    Row {
                        repeat(3) {
                            position ->
                            val reqdState = when(position){
                                0 -> currentTabSelection.value == CategoryState.ARTISTS
                                1 -> currentTabSelection.value == CategoryState.ALBUMS
                                2 -> currentTabSelection.value == CategoryState.SONGS
                                else -> true
                            }
                            val label = when(position){
                                0 -> "Artists"
                                1 -> "Albums"
                                2 -> "Songs"
                                else -> ""
                            }
                            ElevatedSuggestionChip(
                                onClick = {
                                    when(position){
                                        0 -> currentTabSelection.value = CategoryState.ARTISTS
                                        1 -> currentTabSelection.value = CategoryState.ALBUMS
                                        2 -> currentTabSelection.value = CategoryState.SONGS
                                    }
                                },
                                label = {
                                    Text(label, color = when(reqdState){
                                        true -> ListenBrainzTheme.colorScheme.followerChipUnselected
                                        false -> ListenBrainzTheme.colorScheme.followerChipSelected
                                    }, style = ListenBrainzTheme.textStyles.chips)
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = when(reqdState){
                                    true -> null
                                    false -> BorderStroke(1.dp, lb_purple_night)
                                },
                                colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                                    if (reqdState) {
                                        ListenBrainzTheme.colorScheme.followerChipSelected
                                    } else {
                                        ListenBrainzTheme.colorScheme.followerChipUnselected
                                    }
                                ),
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                    }
               when(currentTabSelection.value){
                   CategoryState.ARTISTS ->
                       if(uiState.statsTabUIState.isLoading){
                           CircularProgressIndicator()
                       }
                       else{
                           Column (horizontalAlignment = Alignment.CenterHorizontally) {
                               topArtists.map {
                                       topArtist ->
                                   ArtistCard(artistName = topArtist.artistName ?: "", listenCountLabel = formatNumber(topArtist.listenCount ?: 0)){
                                       if(topArtist.artistMbid != null){
                                           goToArtistPage(topArtist.artistMbid)
                                       }

                                   }
                               }
                               Spacer(modifier = Modifier.height(10.dp))
                               if((uiState.statsTabUIState.topArtists?.size ?: 0) > 5){
                                   LoadMoreButton(state = artistsCollapseState.value) {
                                       artistsCollapseState.value = !artistsCollapseState.value
                                   }
                               }

                           }
                       }

                   CategoryState.ALBUMS ->
                       if(uiState.statsTabUIState.isLoading){
                           CircularProgressIndicator(
                               color = lb_purple_night
                           )
                       }
                       else{
                           Column (horizontalAlignment = Alignment.CenterHorizontally) {
                               topAlbums.mapIndexed {
                                       index, topAlbum ->
                                   ListenCardSmall(
                                       trackName = topAlbum.releaseName ?: "",
                                       artists = topAlbum.artists ?: listOf(FeedListenArtist(topAlbum.artistName ?: "", null, "")),
                                       coverArtUrl = getCoverArtUrl(topAlbum.caaReleaseMbid, topAlbum.caaId),
                                       modifier = Modifier.padding(top = 10.dp, bottom = 10.dp, end = 10.dp),
                                       color = app_bg_secondary_dark,
                                       titleColor = ListenBrainzTheme.colorScheme.followerChipSelected,
                                       subtitleColor = ListenBrainzTheme.colorScheme.listenText.copy(alpha = 0.7f),
                                       enableTrailingContent = true,
                                       listenCount = topAlbum.listenCount,
                                       enableDropdownIcon = true,
                                       goToArtistPage = goToArtistPage
                                       )
                                   {

                                   }
                               }
                               Spacer(modifier = Modifier.height(10.dp))
                               if((uiState.statsTabUIState.topAlbums?.size ?: 0) > 5) {
                                   LoadMoreButton(state = albumsCollapseState.value) {
                                       albumsCollapseState.value = !albumsCollapseState.value
                                   }
                               }
                           }
                       }

                   CategoryState.SONGS -> {
                       if(uiState.statsTabUIState.isLoading){
                           CircularProgressIndicator()
                       }
                       else{
                           Column(horizontalAlignment = Alignment.CenterHorizontally) {
                               topSongs.mapIndexed { index, topSong ->
                                   val metadata = Metadata(trackMetadata = topSong.toTrackMetadata())
                                   ListenCardSmall(
                                       trackName = topSong.trackName ?: "",
                                       artists = topSong.artists ?:listOf(FeedListenArtist(topSong.artistName ?: "", null, "")),
                                       coverArtUrl = getCoverArtUrl(topSong.caaReleaseMbid, topSong.caaId),
                                       modifier = Modifier.padding(top = 10.dp, bottom = 10.dp, end = 10.dp),
                                       color = app_bg_secondary_dark,
                                       titleColor = ListenBrainzTheme.colorScheme.followerChipSelected,
                                       subtitleColor = ListenBrainzTheme.colorScheme.listenText.copy(alpha = 0.7f),
                                       enableDropdownIcon = true,
                                       onDropdownIconClick = {
                                           dropdownItemIndex.value = index
                                       },
                                       dropDown = {
                                           SocialDropdown(
                                               isExpanded = dropdownItemIndex.value == index,
                                               onDismiss = {
                                                   dropdownItemIndex.value = null
                                               },
                                               metadata = metadata,
                                               onRecommend = { onRecommend(metadata) },
                                               onPersonallyRecommend = {
                                                   dialogsState.activateDialog(Dialog.PERSONAL_RECOMMENDATION , ListenDialogBundleKeys.listenDialogBundle(0, index))
                                                   dropdownItemIndex.value = null
                                               },
                                               onReview = {
                                                   dialogsState.activateDialog(Dialog.REVIEW , ListenDialogBundleKeys.listenDialogBundle(0, index))
                                                   dropdownItemIndex.value = null
                                               },
                                               onPin = {
                                                   dialogsState.activateDialog(Dialog.PIN , ListenDialogBundleKeys.listenDialogBundle(0, index))
                                                   dropdownItemIndex.value = null
                                               },
                                               onOpenInMusicBrainz = {
                                                   try {
                                                       uriHandler.openUri("https://musicbrainz.org/recording/${metadata.trackMetadata?.mbidMapping?.recordingMbid}")
                                                   }
                                                   catch(e : Error) {
                                                       scope.launch {
                                                           snackbarState.showSnackbar(context.getString(R.string.err_generic_toast))
                                                       }
                                                   }
                                                   dropdownItemIndex.value = null
                                               }
                                           )
                                       },
                                       enableTrailingContent = true,
                                       listenCount = topSong.listenCount,
                                       goToArtistPage = goToArtistPage
                                   ) {
                                       val trackMetadata = metadata.trackMetadata
                                       if(trackMetadata != null){
                                           playListen(trackMetadata)
                                       }
                                   }
                               }
                               Spacer(modifier = Modifier.height(10.dp))
                               if((uiState.statsTabUIState.topSongs?.size ?: 0) > 5) {
                                   LoadMoreButton(state = songsCollapseState.value) {
                                       songsCollapseState.value = !songsCollapseState.value
                                   }
                               }
                           }
                       }
                   }
               }
           }
        }
    }

    ErrorBar(error = socialUiState.error, onErrorShown = onErrorShown )
    SuccessBar(resId = socialUiState.successMsgId, onMessageShown = onMessageShown, snackbarState = snackbarState)

    Dialogs(
        deactivateDialog = {
            dialogsState.deactivateDialog()
        },
        currentDialog = dialogsState.currentDialog,
        currentIndex = dialogsState.metadata?.getInt(ListenDialogBundleKeys.EVENT_INDEX.name),
        listens = uiState.listensTabUiState.recentListens ?: listOf(),
        onPin = {metadata, blurbContent ->  onPin(metadata, blurbContent)},
        searchUsers = { query -> searchUsers(query) },
        feedUiState = feedUiState,
        isCritiqueBrainzLinked = isCritiqueBrainzLinked,
        onReview = {type, blurbContent, rating, locale, metadata -> onReview(type, blurbContent, rating, locale, metadata) },
        onPersonallyRecommend = {metadata, users, blurbContent -> onPersonallyRecommend(metadata, users, blurbContent)},
        snackbarState = snackbarState,
        socialUiState = socialUiState)
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
            .padding(end = 10.dp, top = 10.dp)
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
                    Text(artistName, color = ListenBrainzTheme.colorScheme.followerChipSelected, style = ListenBrainzTheme.textStyles.listenTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                }
                if(listenCountLabel != null){
                    Box(
                        modifier = modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(ListenBrainzTheme.colorScheme.followerChipSelected)
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = listenCountLabel,
                                color = ListenBrainzTheme.colorScheme.followerChipUnselected
                            )
                        }
                    }
                }

            }
        }
    }
}

@Composable
private fun RangeBar(
    statsRangeState: StatsRange,
    onClick: (StatsRange) -> Unit
){
    LazyRow {
        repeat(7){
                position ->
            val rangeAtIndex  = when (position){
                0 -> StatsRange.THIS_WEEK
                1 -> StatsRange.THIS_MONTH
                2 -> StatsRange.THIS_YEAR
                3 -> StatsRange.LAST_WEEK
                4 -> StatsRange.LAST_MONTH
                5 -> StatsRange.LAST_YEAR
                6 -> StatsRange.ALL_TIME
                else -> StatsRange.ALL_TIME
            }
            item {
                if(position == 0){
                    Spacer(modifier = Modifier.width(10.dp))
                }
                ElevatedSuggestionChip(
                    onClick = {
                        onClick(rangeAtIndex)
                    },
                    label = {
                        Text(rangeAtIndex.rangeString, color = when(statsRangeState == rangeAtIndex){
                            true -> ListenBrainzTheme.colorScheme.followerChipUnselected
                            false -> ListenBrainzTheme.colorScheme.followerChipSelected
                        }, style = ListenBrainzTheme.textStyles.chips)
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = when(statsRangeState == rangeAtIndex){
                        true -> null
                        false -> BorderStroke(1.dp, lb_purple_night)
                    },
                    colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                        if (statsRangeState == rangeAtIndex) {
                            ListenBrainzTheme.colorScheme.followerChipSelected
                        } else {
                            ListenBrainzTheme.colorScheme.followerChipUnselected
                        }
                    ),
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
        }
    }
}

@Composable
private fun UserGlobalBar(
    userGlobalState: UserGlobal,
    onUserGlobalChange: (UserGlobal) -> Unit,
    username: String?
){
    LazyRow {
            item {
                val reqdState = userGlobalState == UserGlobal.USER
                Spacer(modifier = Modifier.width(10.dp))
                ElevatedSuggestionChip(
                    onClick = {
                        onUserGlobalChange(UserGlobal.USER)
                    },
                    label = {
                        Text((username ?: "").toString(), color = when(reqdState){
                            true -> ListenBrainzTheme.colorScheme.followerChipUnselected
                            false -> ListenBrainzTheme.colorScheme.followerChipSelected
                        }, style = ListenBrainzTheme.textStyles.chips)
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = when(reqdState){
                        true -> null
                        false -> BorderStroke(1.dp, lb_purple_night)
                    },
                    colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                        if (reqdState) {
                            ListenBrainzTheme.colorScheme.followerChipSelected
                        } else {
                            ListenBrainzTheme.colorScheme.followerChipUnselected
                        }
                    ),
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            item {
                val reqdState = userGlobalState == UserGlobal.GLOBAL
                ElevatedSuggestionChip(
                    onClick = {
                        onUserGlobalChange(UserGlobal.GLOBAL)
                    },
                    label = {
                        Row (verticalAlignment = Alignment.CenterVertically) {
                            Text("Global", color = when(reqdState){
                                true -> ListenBrainzTheme.colorScheme.followerChipUnselected
                                false -> ListenBrainzTheme.colorScheme.followerChipSelected
                            }, style = ListenBrainzTheme.textStyles.chips)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(painter = painterResource(id = R.drawable.globe), contentDescription = "", modifier = Modifier.height(25.dp), tint = when(reqdState){
                                true -> ListenBrainzTheme.colorScheme.followerChipUnselected
                                false -> ListenBrainzTheme.colorScheme.followerChipSelected
                            })
                        }

                    },
                    shape = RoundedCornerShape(10.dp),
                    border = when(reqdState){
                        true -> null
                        false -> BorderStroke(1.dp, lb_purple_night)
                    },
                    colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                        if (reqdState) {
                            ListenBrainzTheme.colorScheme.followerChipSelected
                        } else {
                            ListenBrainzTheme.colorScheme.followerChipUnselected
                        }
                    ),
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
    }
}

private fun valueFormatter(value: Int, statsRange: StatsRange) : String {
    val label : String = when(statsRange){
        StatsRange.THIS_WEEK, StatsRange.LAST_WEEK -> when(value % 7){
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
        StatsRange.THIS_YEAR, StatsRange.LAST_YEAR -> when(value % 12){
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