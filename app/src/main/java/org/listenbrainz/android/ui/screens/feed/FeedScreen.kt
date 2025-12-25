package org.listenbrainz.android.ui.screens.feed

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ReusableContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.ShimmerTheme
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import com.valentinilk.shimmer.shimmerSpec
import kotlinx.coroutines.flow.flow
import org.listenbrainz.android.model.AppNavigationItem
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.model.feed.FeedCallbacks
import org.listenbrainz.android.model.feed.FeedEvent
import org.listenbrainz.android.model.feed.FeedEventType
import org.listenbrainz.android.model.feed.FeedListenArtist
import org.listenbrainz.android.model.feed.ReviewEntityType
import org.listenbrainz.android.ui.components.ErrorBar
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.components.NavigationChips
import org.listenbrainz.android.ui.components.TitleAndSubtitle
import org.listenbrainz.android.ui.components.dialogs.Dialog
import org.listenbrainz.android.ui.components.dialogs.PersonalRecommendationDialog
import org.listenbrainz.android.ui.components.dialogs.PinDialog
import org.listenbrainz.android.ui.components.dialogs.ReviewDialog
import org.listenbrainz.android.ui.components.dialogs.rememberDialogsState
import org.listenbrainz.android.ui.navigation.TopBar
import org.listenbrainz.android.ui.navigation.TopBarActions
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.PreviewSurface
import org.listenbrainz.android.util.Utils
import org.listenbrainz.android.viewmodel.FeedViewModel
import org.listenbrainz.android.viewmodel.SocialViewModel

@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel(),
    socialViewModel: SocialViewModel = hiltViewModel(),
    scrollToTopState: Boolean,
    topAppBarActions: TopBarActions,
    onScrollToTop: (suspend () -> Unit) -> Unit,
    goToUserPage: (String) -> Unit,
    goToArtistPage: (String) -> Unit
) {

    val uiState by viewModel.uiState.collectAsState()

    FeedScreen(
        uiState = uiState,
        scrollToTopState = scrollToTopState,
        topBarActions = topAppBarActions,
        callbacks = remember {
            FeedCallbacks(
                onScrollToTop = onScrollToTop,
                onDeleteOrHide = { event, eventType, parentUser ->
                    viewModel.hideOrDeleteEvent(event, eventType, parentUser)
                },
                onErrorShown = { viewModel.clearErrorFlow() },
                onRecommend = { event ->
                    socialViewModel.recommend(event.metadata)
                },
                onPersonallyRecommend = { event, users, blurbContent ->
                    socialViewModel.personallyRecommend(event.metadata, users, blurbContent)
                },
                onReview = { event, type, blurbContent, rating, locale ->
                    socialViewModel.review(event.metadata, type, blurbContent, rating, locale)
                },
                onPin = { event, blurbContent ->
                    socialViewModel.pin(event.metadata, blurbContent)
                },
                searchFollower = { query ->
                    viewModel.searchUser(query)
                },
                isCritiqueBrainzLinked = viewModel::isCritiqueBrainzLinked,
                onPlay = { event ->
                    viewModel.play(event)
                },
                goToUserPage = goToUserPage,
                goToArtistPage = goToArtistPage
            )
        }
    )
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FeedScreen(
    uiState: FeedUiState,
    scrollToTopState: Boolean,
    topBarActions: TopBarActions,
    callbacks: FeedCallbacks,
) {
    val myFeedPagingData = uiState.myFeedState.eventList.collectAsLazyPagingItems()
    val myFeedListState = rememberLazyListState()

    val followListensPagingData =
        uiState.followListensFeedState.eventList.collectAsLazyPagingItems()
    val followListensListState = rememberLazyListState()

    val similarListensPagingData =
        uiState.similarListensFeedState.eventList.collectAsLazyPagingItems()
    val similarListensListState = rememberLazyListState()

    val pagerState = rememberPagerState { 3 }
    val isRefreshing = remember(
        pagerState.currentPage,
        myFeedPagingData.loadState.refresh
    ) {
        when (pagerState.currentPage) {
            0 -> myFeedPagingData.loadState.refresh is LoadState.Loading
            1 -> followListensPagingData.loadState.refresh is LoadState.Loading
            2 -> similarListensPagingData.loadState.refresh is LoadState.Loading
            else -> false
        }
    }

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

    val dialogsState = rememberDialogsState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            when (pagerState.currentPage) {
                0 -> myFeedPagingData.refresh()
                1 -> followListensPagingData.refresh()
                2 -> similarListensPagingData.refresh()
            }
        }
    )

    LaunchedEffect(scrollToTopState) {
        callbacks.onScrollToTop {
            when (pagerState.currentPage) {
                0 -> {
                    myFeedListState.scrollToItem(0)
                    myFeedPagingData.refresh()
                }

                1 -> {
                    followListensListState.scrollToItem(0)
                    followListensPagingData.refresh()
                }

                2 -> {
                    similarListensListState.scrollToItem(0)
                    similarListensPagingData.refresh()
                }
            }
        }
    }

    /** CONTENT */
    Column {
        TopBar(
            modifier = Modifier.statusBarsPadding(),
            topBarActions = topBarActions,
            title = AppNavigationItem.Feed.title
        )
        Box(
            Modifier
                .fillMaxSize()
                .pullRefresh(state = pullRefreshState)
        ) {
            if (myFeedPagingData.itemCount == 0 && myFeedPagingData.loadState.refresh is LoadState.Error) {
                RetryButton(
                    modifier = Modifier.align(Alignment.Center),
                ) {
                    myFeedPagingData.retry()
                    similarListensPagingData.retry()
                    followListensPagingData.retry()
                }
            }

            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1
            ) { position ->
                when (position) {
                    0 -> MyFeed(
                        listState = myFeedListState,
                        pagingData = myFeedPagingData,
                        uiState = uiState.myFeedState,
                        onDeleteOrHide = callbacks.onDeleteOrHide,
                        recommendTrack = callbacks.onRecommend,
                        personallyRecommendTrack = { index ->
                            dialogsState.activateDialog(
                                Dialog.PERSONAL_RECOMMENDATION,
                                FeedDialogBundleKeys.feedDialogBundle(0, index),
                            )
                        },
                        review = { index ->
                            dialogsState.activateDialog(
                                Dialog.REVIEW,
                                FeedDialogBundleKeys.feedDialogBundle(0, index)
                            )
                        },
                        pin = { index ->
                            dialogsState.activateDialog(
                                Dialog.PIN,
                                FeedDialogBundleKeys.feedDialogBundle(0, index)
                            )
                        },
                        onPlay = callbacks.onPlay,
                        goToUserPage = callbacks.goToUserPage,
                        goToArtistPage = callbacks.goToArtistPage,
                        shimmerInstance = shimmerInstance
                    )

                    1 -> FollowListens(
                        listState = followListensListState,
                        pagingData = followListensPagingData,
                        recommendTrack = callbacks.onRecommend,
                        personallyRecommendTrack = { index ->
                            dialogsState.activateDialog(
                                Dialog.PERSONAL_RECOMMENDATION,
                                FeedDialogBundleKeys.feedDialogBundle(1, index)
                            )
                        },
                        review = { index ->
                            dialogsState.activateDialog(
                                Dialog.REVIEW,
                                FeedDialogBundleKeys.feedDialogBundle(1, index)
                            )
                        },
                        pin = { index ->
                            dialogsState.activateDialog(
                                Dialog.PIN,
                                FeedDialogBundleKeys.feedDialogBundle(1, index)
                            )
                        },
                        onPlay = callbacks.onPlay,
                        goToArtistPage = callbacks.goToArtistPage,
                        shimmerInstance = shimmerInstance
                    )

                    2 -> SimilarListens(
                        listState = similarListensListState,
                        pagingData = similarListensPagingData,
                        recommendTrack = callbacks.onRecommend,
                        personallyRecommendTrack = { index ->
                            dialogsState.activateDialog(
                                Dialog.PERSONAL_RECOMMENDATION,
                                FeedDialogBundleKeys.feedDialogBundle(2, index)
                            )
                        },
                        review = { index ->
                            dialogsState.activateDialog(
                                Dialog.REVIEW,
                                FeedDialogBundleKeys.feedDialogBundle(2, index)
                            )
                        },
                        pin = { index ->
                            dialogsState.activateDialog(
                                Dialog.PIN,
                                FeedDialogBundleKeys.feedDialogBundle(2, index)
                            )
                        },
                        onPlay = callbacks.onPlay,
                        goToArtistPage = callbacks.goToArtistPage,
                        shimmerInstance = shimmerInstance
                    )
                }
            }

            Column(Modifier.fillMaxWidth()) {
                ErrorBar(error = uiState.error, onErrorShown = callbacks.onErrorShown)
                NavigationChips(
                    chips = remember {
                        listOf(
                            "My Feed",
                            "Follow Listens",
                            "Similar Listens"
                        )
                    },
                    currentPageStateProvider = { pagerState.currentPage }
                ) { position ->
                    pagerState.animateScrollToPage(position)
                }
                PullRefreshIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    refreshing = isRefreshing,
                    contentColor = ListenBrainzTheme.colorScheme.lbSignatureInverse,
                    backgroundColor = ListenBrainzTheme.colorScheme.level1,
                    state = pullRefreshState
                )
            }
        }
    }

    Dialogs(
        deactivateDialog = {
            dialogsState.deactivateDialog()
        },
        currentDialog = dialogsState.currentDialog,
        currentEventIndex = dialogsState.metadata?.getInt(FeedDialogBundleKeys.EVENT_INDEX.name),
        pagingSource = remember(dialogsState.metadata) {
            when (dialogsState.metadata?.getInt(FeedDialogBundleKeys.PAGE.name)) {
                0 -> myFeedPagingData
                1 -> followListensPagingData
                else -> similarListensPagingData
            }
        },
        callbacks = callbacks,
        searchUserResult = uiState.searchResult,
    )

}

@Composable
private fun Dialogs(
    deactivateDialog: () -> Unit,
    currentDialog: Dialog,
    currentEventIndex: Int?,
    pagingSource: LazyPagingItems<FeedUiEventItem>,
    searchUserResult: List<String>,
    callbacks: FeedCallbacks
) {
    when (currentDialog) {
        Dialog.NONE -> Unit
        Dialog.PIN -> {
            val metadata = pagingSource[currentEventIndex!!]?.event?.metadata
            PinDialog(
                trackName = metadata?.trackMetadata?.trackName
                    ?: metadata?.entityName ?: return,
                artistName = metadata?.trackMetadata?.artistName ?: return,
                onDismiss = deactivateDialog,
                onSubmit = { blurbContent ->
                    callbacks.onPin(pagingSource[currentEventIndex]?.event!!, blurbContent)
                }
            )
        }

        Dialog.PERSONAL_RECOMMENDATION -> {
            val metadata = pagingSource[currentEventIndex!!]?.event?.metadata
            PersonalRecommendationDialog(
                trackName = metadata?.trackMetadata?.trackName
                    ?: metadata?.entityName ?: return,
                onDismiss = deactivateDialog,
                searchResult = searchUserResult,
                searchUsers = callbacks.searchFollower,
                onSubmit = { users, blurbContent ->
                    callbacks.onPersonallyRecommend(
                        pagingSource[currentEventIndex]?.event!!,
                        users,
                        blurbContent
                    )
                }
            )
        }

        Dialog.REVIEW -> {
            val metadata = pagingSource[currentEventIndex!!]?.event?.metadata
            if (metadata != null) {
                ReviewDialog(
                    trackName = metadata.trackMetadata?.trackName
                        ?: if (metadata.entityType == ReviewEntityType.RECORDING.code) metadata.entityName else return,
                    artistName = metadata.trackMetadata?.artistName
                        ?: if (metadata.entityType == ReviewEntityType.ARTIST.code) metadata.entityName else null,
                    releaseName = metadata.trackMetadata?.releaseName
                        ?: if (metadata.entityType == ReviewEntityType.RELEASE_GROUP.code) metadata.entityName else null,
                    onDismiss = deactivateDialog,
                    isCritiqueBrainzLinked = callbacks.isCritiqueBrainzLinked,
                    onSubmit = { type, blurbContent, rating, locale ->
                        callbacks.onReview(
                            pagingSource[currentEventIndex]?.event!!,
                            type,
                            blurbContent,
                            rating,
                            locale
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun MyFeed(
    listState: LazyListState,
    pagingData: LazyPagingItems<FeedUiEventItem>,
    uiState: FeedUiEventData,
    onDeleteOrHide: (event: FeedEvent, eventType: FeedEventType, parentUser: String) -> Unit,
    recommendTrack: (event: FeedEvent) -> Unit,
    personallyRecommendTrack: (index: Int) -> Unit,
    review: (index: Int) -> Unit,
    pin: (index: Int) -> Unit,
    onPlay: (FeedEvent) -> Unit,
    goToUserPage: (String) -> Unit,
    goToArtistPage: (String) -> Unit,
    shimmerInstance: Shimmer,
    uriHandler: UriHandler = LocalUriHandler.current
) {
    val dropdownItemIndex = rememberSaveable { mutableStateOf<Int?>(null) }

    val inRefreshingState = pagingData.loadState.refresh is LoadState.Loading

    var height by remember { mutableIntStateOf(0) }
    val count by remember {
        derivedStateOf {
            if (height == 0)
                return@derivedStateOf 0
            listState.layoutInfo.viewportSize.height / height
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = LocalConfiguration.current.screenWidthDp.dp),
        state = listState
    ) {
        item { StartingSpacer() }

        if (inRefreshingState) {
            item(contentType = "shimmer") {
                ShimmerMyFeedItem(
                    shimmer = shimmerInstance,
                    modifier = Modifier.onSizeChanged {
                        height = it.height
                    }
                )
            }

            items(count, contentType = { "shimmer" }) {
                ShimmerMyFeedItem(shimmerInstance)
            }
        } else {
            items(pagingData.itemCount) { index ->
                pagingData[index]?.apply {
                    AnimatedVisibility(
                        visible = uiState.isDeletedMap[event.id] != true,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        eventType.Content(
                            event = event,
                            parentUser = parentUser,
                            isHidden = uiState.isHiddenMap[event.id] == true,
                            onDeleteOrHide = {
                                onDeleteOrHide(event, eventType, parentUser)
                            },
                            dropDownState = dropdownItemIndex.value,
                            index = index,
                            onDropdownClick = {
                                dropdownItemIndex.value =
                                    if (dropdownItemIndex.value == null) index else null
                            },
                            onRecommend = {
                                recommendTrack(event)
                                dropdownItemIndex.value = null
                            },
                            onPersonallyRecommend = {
                                personallyRecommendTrack(index)
                                dropdownItemIndex.value = null
                            },
                            onReview = {
                                review(index)
                                dropdownItemIndex.value = null
                            },
                            onPin = {
                                pin(index)
                                dropdownItemIndex.value = null
                            },
                            onOpenInMusicBrainz = {
                                uriHandler.openUri(
                                    "https://musicbrainz.org/recording/${
                                        event.metadata.trackMetadata?.mbidMapping?.recordingMbid
                                            ?: return@Content
                                    }"
                                )
                                dropdownItemIndex.value = null
                            },
                            onClick = {
                                onPlay(event)
                                dropdownItemIndex.value = null
                            },
                            goToUserPage = goToUserPage,
                            goToArtistPage = goToArtistPage
                        )
                    }
                }
            }

            item { PagerRearLoadingIndicator(pagingData) }
        }
    }

}


@Composable
fun FollowListens(
    listState: LazyListState,
    pagingData: LazyPagingItems<FeedUiEventItem>,
    recommendTrack: (event: FeedEvent) -> Unit,
    personallyRecommendTrack: (index: Int) -> Unit,
    review: (index: Int) -> Unit,
    pin: (index: Int) -> Unit,
    onPlay: (FeedEvent) -> Unit,
    uriHandler: UriHandler = LocalUriHandler.current,
    shimmerInstance: Shimmer,
    goToArtistPage: (String) -> Unit,
) {
    // Since, at most one drop down will be active at a time, then we only need to maintain one state variable.
    val dropdownItemIndex: MutableState<Int?> = rememberSaveable {
        mutableStateOf(null)
    }

    val inRefreshingState = pagingData.loadState.refresh is LoadState.Loading

    var height by remember { mutableIntStateOf(0) }
    val count by remember {
        derivedStateOf {
            if (height == 0)
                return@derivedStateOf 0
            listState.layoutInfo.viewportSize.height / height
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
    ) {

        item { StartingSpacer() }

        if (inRefreshingState) {
            item(contentType = "shimmer") {
                ShimmerListensItem(
                    shimmer = shimmerInstance,
                    modifier = Modifier.onSizeChanged {
                        height = it.height
                    }
                )
            }

            items(count, contentType = { "shimmer" }) {
                ShimmerListensItem(shimmerInstance)
            }
        } else {

            items(count = pagingData.itemCount) { index: Int ->

                pagingData[index]?.apply {

                    ListenCardSmall(
                        modifier = Modifier.padding(
                            horizontal = ListenBrainzTheme.paddings.horizontal,
                            vertical = ListenBrainzTheme.paddings.lazyListAdjacent
                        ),
                        trackName = event.metadata.trackMetadata?.trackName ?: "Unknown",
                        artists = event.metadata.trackMetadata?.mbidMapping?.artists ?: listOf(
                            FeedListenArtist(
                                event.metadata.trackMetadata?.artistName ?: "",
                                null,
                                ""
                            )
                        ),
                        coverArtUrl =
                            Utils.getCoverArtUrl(
                                caaReleaseMbid = event.metadata.trackMetadata?.mbidMapping?.caaReleaseMbid,
                                caaId = event.metadata.trackMetadata?.mbidMapping?.caaId
                            ),
                        onDropdownIconClick = {
                            dropdownItemIndex.value =
                                if (dropdownItemIndex.value == null) index else null
                        },
                        dropDown = {
                            SocialDropdown(
                                isExpanded = dropdownItemIndex.value == index,
                                metadata = event.metadata,
                                onDismiss = {
                                    dropdownItemIndex.value = null
                                },
                                onRecommend = {
                                    recommendTrack(event)
                                    dropdownItemIndex.value = null
                                },
                                onPersonallyRecommend = {
                                    personallyRecommendTrack(index)
                                    dropdownItemIndex.value = null
                                },
                                onReview = {
                                    review(index)
                                    dropdownItemIndex.value = null
                                },
                                onPin = {
                                    pin(index)
                                    dropdownItemIndex.value = null
                                },
                                onOpenInMusicBrainz = {
                                    uriHandler.openUri("https://musicbrainz.org/recording/${event.metadata.trackMetadata?.mbidMapping?.recordingMbid ?: return@SocialDropdown}")
                                }
                            )
                        },
                        trailingContent = { modifier ->
                            Column(modifier, horizontalAlignment = Alignment.End) {
                                TitleAndSubtitle(
                                    title = event.username ?: "Unknown",
                                    artists = listOf(),
                                    titleColor = ListenBrainzTheme.colorScheme.lbSignature,
                                    goToArtistPage = goToArtistPage
                                )
                                Date(
                                    event = event,
                                    parentUser = parentUser,
                                    eventType = eventType
                                )
                            }
                        },
                        goToArtistPage = goToArtistPage
                    ) {
                        onPlay(event)
                    }

                }
            }

            item {
                PagerRearLoadingIndicator(pagingData)
            }
        }
    }
}


@Composable
fun SimilarListens(
    listState: LazyListState,
    pagingData: LazyPagingItems<FeedUiEventItem>,
    recommendTrack: (event: FeedEvent) -> Unit,
    personallyRecommendTrack: (index: Int) -> Unit,
    review: (index: Int) -> Unit,
    pin: (index: Int) -> Unit,
    onPlay: (FeedEvent) -> Unit,
    uriHandler: UriHandler = LocalUriHandler.current,
    shimmerInstance: Shimmer,
    goToArtistPage: (String) -> Unit
) {
    // Since, at most one drop down will be active at a time, then we only need to maintain one state variable.
    val dropdownItemIndex: MutableState<Int?> = rememberSaveable {
        mutableStateOf(null)
    }

    val inRefreshingState = pagingData.loadState.refresh is LoadState.Loading

    var height by remember { mutableIntStateOf(0) }
    val count by remember {
        derivedStateOf {
            if (height == 0)
                return@derivedStateOf 0
            listState.layoutInfo.viewportSize.height / height
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState
    ) {
        item { StartingSpacer() }

        if (inRefreshingState) {
            item(contentType = "shimmer") {
                ShimmerListensItem(
                    shimmer = shimmerInstance,
                    modifier = Modifier.onSizeChanged {
                        height = it.height
                    }
                )
            }

            items(count, contentType = { "shimmer" }) {
                ShimmerListensItem(shimmerInstance)
            }
        } else {

            items(count = pagingData.itemCount) { index: Int ->

                pagingData[index]?.run {

                    ListenCardSmall(
                        modifier = Modifier.padding(
                            horizontal = ListenBrainzTheme.paddings.horizontal,
                            vertical = ListenBrainzTheme.paddings.lazyListAdjacent
                        ),
                        trackName = event.metadata.trackMetadata?.trackName ?: "Unknown",
                        artists = event.metadata.trackMetadata?.mbidMapping?.artists ?: listOf(
                            FeedListenArtist(
                                event.metadata.trackMetadata?.artistName ?: "",
                                null,
                                ""
                            )
                        ),
                        coverArtUrl =
                            Utils.getCoverArtUrl(
                                caaReleaseMbid = event.metadata.trackMetadata?.mbidMapping?.caaReleaseMbid,
                                caaId = event.metadata.trackMetadata?.mbidMapping?.caaId
                            ),
                        onDropdownIconClick = {
                            dropdownItemIndex.value = if (dropdownItemIndex.value == null) {
                                index
                            } else {
                                null
                            }
                        },
                        dropDown = {
                            SocialDropdown(
                                isExpanded = dropdownItemIndex.value == index,
                                metadata = event.metadata,
                                onDismiss = { dropdownItemIndex.value = null },
                                onRecommend = {
                                    recommendTrack(event)
                                    dropdownItemIndex.value = null
                                },
                                onPersonallyRecommend = {
                                    personallyRecommendTrack(index)
                                    dropdownItemIndex.value = null
                                },
                                onReview = {
                                    review(index)
                                    dropdownItemIndex.value = null
                                },
                                onPin = {
                                    pin(index)
                                    dropdownItemIndex.value = null
                                },
                                onOpenInMusicBrainz = {
                                    uriHandler.openUri("https://musicbrainz.org/recording/${event.metadata.trackMetadata?.mbidMapping?.recordingMbid ?: return@SocialDropdown}")
                                }
                            )
                        },
                        trailingContent = { modifier ->
                            /*TitleAndSubtitle(
                        modifier = modifier,
                        title = event.username ?: "Unknown",
                        subtitle = similarityToPercent(event.similarity),
                        alignment = Alignment.End,
                        titleColor = ListenBrainzTheme.colorScheme.lbSignature,
                        subtitleColor = ListenBrainzTheme.colorScheme.lbSignatureInverse
                    )*/
                            Column(modifier, horizontalAlignment = Alignment.End) {
                                TitleAndSubtitle(
                                    title = event.username ?: "Unknown",
                                    artists = listOf(),
                                    titleColor = ListenBrainzTheme.colorScheme.lbSignature,
                                    goToArtistPage = goToArtistPage
                                )
                                Date(
                                    event = event,
                                    parentUser = parentUser,
                                    eventType = eventType
                                )
                            }
                        },
                        goToArtistPage = goToArtistPage
                    ) {
                        onPlay(event)
                    }

                }
            }


            item {
                PagerRearLoadingIndicator(pagingData)
            }
        }
    }
}

@Composable
fun ShimmerMyFeedItem(
    shimmer: Shimmer,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(top = 10.dp)
            .padding(end = 8.dp, start = 10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(17.dp)
                    .shimmer(shimmer)
                    .background(
                        Color.Gray.copy(alpha = 0.8f),
                        CircleShape
                    )
            )
            Spacer(modifier = Modifier.height(6.dp))
            VerticalDivider(
                thickness = 2.dp,
                modifier = Modifier.height(90.dp),
                color = Color.Gray.copy(0.8f)
            )
        }
        Spacer(modifier = Modifier.padding(start = 12.dp))
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(12.dp)
                    .shimmer(shimmer)
                    .background(
                        Color.Gray.copy(alpha = 0.8f),
                        RoundedCornerShape(2.dp)
                    )
            )
            Spacer(modifier = Modifier.padding(3.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(vertical = 8.dp)
                    .background(
                        Color.Gray.copy(alpha = 0.1f),
                        RoundedCornerShape(6.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .height(72.dp)
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
            Spacer(modifier = Modifier.padding(3.dp))
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.End)
                    .width(80.dp)
                    .height(12.dp)
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
fun ShimmerListensItem(
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
                .padding(end = 30.dp)
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
fun StartingSpacer() {
    Spacer(modifier = Modifier.height(60.dp))   // 6 + 6 + 48
}

@Composable
fun RetryButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        modifier = modifier,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = ListenBrainzTheme.colorScheme.lbSignature)
    ) {
        Text(
            text = "Retry",
            color = ListenBrainzTheme.colorScheme.onLbSignature,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PagerRearLoadingIndicator(pagingData: LazyPagingItems<FeedUiEventItem>) {

    Box(
        modifier = if (pagingData.itemCount == 0) Modifier.fillMaxSize() else Modifier.fillMaxWidth()
    ) {
        if (
            pagingData.itemCount != 0 &&
            pagingData.loadState.append is LoadState.Loading
        ) {

            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(vertical = 32.dp) // Default height of m3 button is 40.dp
                    .size(24.dp),
                strokeCap = StrokeCap.Round,
                color = ListenBrainzTheme.colorScheme.lbSignature
            )

        } else if (pagingData.loadState.append is LoadState.Error) {

            RetryButton(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(vertical = 24.dp),
                onClick = {
                    pagingData.retry()
                }
            )

        } else if (
            pagingData.loadState.refresh is LoadState.NotLoading &&
            pagingData.loadState.append is LoadState.NotLoading
        ) {
            // No more data to page.
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(vertical = 32.dp), // Default height of m3 button is 40.dp)
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Rounded.Done,
                    contentDescription = "End of feed.",
                    tint = ListenBrainzTheme.colorScheme.lbSignature
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "You are all caught up!",
                    fontWeight = FontWeight.Medium,
                    color = ListenBrainzTheme.colorScheme.lbSignature
                )
            }


        }
    }
}


private enum class FeedDialogBundleKeys {
    PAGE,
    EVENT_INDEX;

    companion object {
        fun feedDialogBundle(page: Int, eventIndex: Int): Bundle {
            return Bundle().apply {
                putInt(PAGE.name, page)
                putInt(EVENT_INDEX.name, eventIndex)
            }
        }
    }
}


@Preview(uiMode = UI_MODE_NIGHT_NO)
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun FeedScreenPreview() {
    PreviewSurface {
        FeedScreen(
            uiState = FeedUiState(
                FeedUiEventData(eventList = flow {
                    emit(
                        PagingData.from(
                            List(30) {
                                FeedUiEventItem(
                                    eventType = FeedEventType.LISTEN,
                                    parentUser = "Jasjeet",
                                    event = FeedEvent(
                                        0,
                                        0,
                                        FeedEventType.LISTEN.type,
                                        metadata = Metadata(),
                                        username = "Jasjeet"
                                    )
                                )
                            }
                        ))
                })
            ),
            scrollToTopState = false,
            callbacks = FeedCallbacks(
                onScrollToTop = {},
                onDeleteOrHide = { _, _, _ -> },
                onErrorShown = {},
                onRecommend = {},
                onPersonallyRecommend = { _, _, _ -> },
                onReview = { _, _, _, _, _ -> },
                onPin = { _, _ -> },
                searchFollower = {},
                isCritiqueBrainzLinked = { true },
                onPlay = {},
                goToUserPage = {},
                goToArtistPage = {}
            ),
            topBarActions = TopBarActions()
        )
    }

}