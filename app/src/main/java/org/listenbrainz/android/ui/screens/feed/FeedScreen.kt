package org.listenbrainz.android.ui.screens.feed

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.flow
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
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Utils
import org.listenbrainz.android.viewmodel.FeedViewModel
import org.listenbrainz.android.viewmodel.SocialViewModel

@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel(),
    socialViewModel: SocialViewModel = hiltViewModel(),
    scrollToTopState: Boolean,
    onScrollToTop: (suspend () -> Unit) -> Unit,
    goToUserPage: (String) -> Unit,
    goToArtistPage: (String) -> Unit
) {
    
    val uiState by viewModel.uiState.collectAsState()
    
    FeedScreen(
        uiState = uiState,
        scrollToTopState = scrollToTopState,
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
    callbacks: FeedCallbacks,
) {
    val myFeedPagingData = uiState.myFeedState.eventList.collectAsLazyPagingItems()
    val myFeedListState = rememberLazyListState()
    
    val followListensPagingData = uiState.followListensFeedState.eventList.collectAsLazyPagingItems()
    val followListensListState = rememberLazyListState()
    
    val similarListensPagingData = uiState.similarListensFeedState.eventList.collectAsLazyPagingItems()
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
    
    LaunchedEffect(scrollToTopState){
        callbacks.onScrollToTop {
            when (pagerState.currentPage){
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
    Box(
        Modifier
            .fillMaxSize()
            .pullRefresh(state = pullRefreshState)
    ) {
    
        RetryButton(
            modifier = Modifier.align(Alignment.Center),
            show = myFeedPagingData.itemCount == 0 && myFeedPagingData.loadState.refresh is LoadState.Error,
        ) {
            myFeedPagingData.retry()
            similarListensPagingData.retry()
            followListensPagingData.retry()
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
                    goToArtistPage = callbacks.goToArtistPage
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
                    goToArtistPage = callbacks.goToArtistPage
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
                    goToArtistPage = callbacks.goToArtistPage
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
    when (currentDialog){
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
                    callbacks.onPersonallyRecommend(pagingSource[currentEventIndex]?.event!!, users, blurbContent)
                }
            )
        }
        Dialog.REVIEW -> {
            val metadata = pagingSource[currentEventIndex!!]?.event?.metadata
            ReviewDialog(
                trackName = metadata?.trackMetadata?.trackName
                    ?: if (metadata?.entityType == ReviewEntityType.RECORDING.code) metadata.entityName else return,
                artistName = metadata.trackMetadata?.artistName
                    ?: if (metadata.entityType == ReviewEntityType.ARTIST.code) metadata.entityName else null,
                releaseName = metadata.trackMetadata?.releaseName
                    ?: if (metadata.entityType == ReviewEntityType.RELEASE_GROUP.code) metadata.entityName else null,
                onDismiss = deactivateDialog,
                isCritiqueBrainzLinked = callbacks.isCritiqueBrainzLinked,
                onSubmit = { type, blurbContent, rating, locale ->
                    callbacks.onReview(pagingSource[currentEventIndex]?.event!!, type, blurbContent, rating, locale)
                }
            )
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
    uriHandler: UriHandler = LocalUriHandler.current
) {
    // Since, at most one drop down will be active at a time, then we only need to maintain one state variable.
    val dropdownItemIndex: MutableState<Int?> = rememberSaveable {
        mutableStateOf(null)
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = LocalConfiguration.current.screenWidthDp.dp),
        state = listState
    ) {
        
        item { StartingSpacer() }
        
        items(count = pagingData.itemCount) { index: Int ->
            
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
                            onDeleteOrHide(
                                event,
                                eventType,
                                parentUser
                            )
                        },
                        dropDownState = dropdownItemIndex.value,
                        index = index,
                        onDropdownClick = {
                            dropdownItemIndex.value = if (dropdownItemIndex.value == null){
                                index
                            } else {
                                null
                            }
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
                            uriHandler.openUri("https://musicbrainz.org/recording/${event.metadata.trackMetadata?.mbidMapping?.recordingMbid ?: return@Content}")
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
        
        item {
            PagerRearLoadingIndicator(pagingData)
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
    goToArtistPage: (String) -> Unit,
) {
    // Since, at most one drop down will be active at a time, then we only need to maintain one state variable.
    val dropdownItemIndex: MutableState<Int?> = rememberSaveable {
        mutableStateOf(null)
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        state = listState,
    ) {
        
        item { StartingSpacer() }
        
        items(count = pagingData.itemCount) { index: Int ->
            
            pagingData[index]?.apply {
                
                ListenCardSmall(
                    modifier = Modifier.padding(
                        horizontal = ListenBrainzTheme.paddings.horizontal,
                        vertical = ListenBrainzTheme.paddings.lazyListAdjacent
                    ),
                    trackName = event.metadata.trackMetadata?.trackName ?: "Unknown",
                    artists = event.metadata.trackMetadata?.mbidMapping?.artists ?: listOf(
                        FeedListenArtist(event.metadata.trackMetadata?.artistName ?: "" , null, "")
                    ),
                    coverArtUrl =
                        Utils.getCoverArtUrl(
                            caaReleaseMbid = event.metadata.trackMetadata?.mbidMapping?.caaReleaseMbid,
                            caaId = event.metadata.trackMetadata?.mbidMapping?.caaId
                        ),
                    enableDropdownIcon = true,
                    onDropdownIconClick = {
                        dropdownItemIndex.value = if (dropdownItemIndex.value == null) index else null
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
                    enableTrailingContent = true,
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
    goToArtistPage: (String) -> Unit
) {
    // Since, at most one drop down will be active at a time, then we only need to maintain one state variable.
    val dropdownItemIndex: MutableState<Int?> = rememberSaveable {
        mutableStateOf(null)
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        state = listState
    ) {
        
        item { StartingSpacer() }
        
        items(count = pagingData.itemCount) { index: Int ->
            
            pagingData[index]?.apply {
                
                ListenCardSmall(
                    modifier = Modifier.padding(
                        horizontal = ListenBrainzTheme.paddings.horizontal,
                        vertical = ListenBrainzTheme.paddings.lazyListAdjacent
                    ),
                    trackName = event.metadata.trackMetadata?.trackName ?: "Unknown",
                    artists = event.metadata.trackMetadata?.mbidMapping?.artists ?: listOf(FeedListenArtist(event.metadata.trackMetadata?.artistName ?: "" , null, "")),
                    coverArtUrl =
                        Utils.getCoverArtUrl(
                            caaReleaseMbid = event.metadata.trackMetadata?.mbidMapping?.caaReleaseMbid,
                            caaId = event.metadata.trackMetadata?.mbidMapping?.caaId
                        ),
                    enableDropdownIcon = true,
                    onDropdownIconClick = {
                        dropdownItemIndex.value = if (dropdownItemIndex.value == null){
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
                    enableTrailingContent = true,
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

@Composable
fun StartingSpacer() {
    Spacer(modifier = Modifier.height(60.dp))   // 6 + 6 + 48
}

@Composable
fun RetryButton(modifier: Modifier = Modifier, show: Boolean, onClick: () -> Unit) {
    if (show) {
        Box(
            modifier = modifier
                .width(150.dp)
                .height(50.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color.Black,
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable(onClick = onClick)
        ) {
            Text(
                text = "Retry",
                color = Color.Black,
                fontWeight = FontWeight.Medium,
                style = TextStyle(fontSize = 23.sp),
                modifier = Modifier.align(Alignment.Center)
            )
        }
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
                show = true,
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
    ListenBrainzTheme {
        Surface (color = ListenBrainzTheme.colorScheme.background) {
            FeedScreen(
                uiState = FeedUiState(
                    FeedUiEventData(eventList = flow {
                        emit(PagingData.from(
                            List(30){
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
                    onDeleteOrHide = {_,_,_ -> },
                    onErrorShown = {},
                    onRecommend = {},
                    onPersonallyRecommend = {_,_,_ -> },
                    onReview = {_,_,_,_,_ ->},
                    onPin = {_,_ ->},
                    searchFollower = {},
                    isCritiqueBrainzLinked = {true},
                    onPlay = {},
                    goToUserPage = {},
                    goToArtistPage = {}
                ),
            )
        }
    }
}