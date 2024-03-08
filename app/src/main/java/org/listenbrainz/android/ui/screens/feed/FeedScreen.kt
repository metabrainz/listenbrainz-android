package org.listenbrainz.android.ui.screens.feed

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.Icon
import androidx.compose.material3.SuggestionChipDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.model.feed.FeedEvent
import org.listenbrainz.android.model.feed.FeedEventType
import org.listenbrainz.android.model.feed.ReviewEntityType
import org.listenbrainz.android.ui.components.ErrorBar
import org.listenbrainz.android.ui.components.ListenCardSmall
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
    onScrollToTop: (suspend () -> Unit) -> Unit
) {
    
    val uiState by viewModel.uiState.collectAsState()
    
    FeedScreen(
        uiState = uiState,
        scrollToTopState = scrollToTopState,
        onScrollToTop = onScrollToTop,
        onDeleteOrHide = { event, eventType, parentUser ->
            viewModel.hideOrDeleteEvent(event, eventType, parentUser)
        },
        onErrorShown = { viewModel.clearErrorFlow() },
        recommendTrack = { event ->
            socialViewModel.recommend(event.metadata)
        },
        personallyRecommendTrack = { event, users, blurbContent ->
            socialViewModel.personallyRecommend(event.metadata, users, blurbContent)
        },
        review = { event, type, blurbContent, rating, locale ->
            socialViewModel.review(event.metadata, type, blurbContent, rating, locale)
        },
        pin = { event, blurbContent ->
            socialViewModel.pin(event.metadata, blurbContent)
        },
        searchFollower = { query ->
            viewModel.searchUser(query)
        },
        isCritiqueBrainzLinked = { viewModel.isCritiqueBrainzLinked() },
        onPlay = { event ->
            viewModel.play(event)
        }
    )
}


@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun FeedScreen(
    uiState: FeedUiState,
    scrollToTopState: Boolean,
    onScrollToTop: (suspend () -> Unit) -> Unit,
    onDeleteOrHide: (event: FeedEvent, eventType: FeedEventType, parentUser: String) -> Unit,
    onErrorShown: () -> Unit,
    recommendTrack: (event: FeedEvent) -> Unit,
    personallyRecommendTrack: (event: FeedEvent, users: List<String>, blurbContent: String) -> Unit,
    review: (event: FeedEvent, entityType: ReviewEntityType, blurbContent: String, rating: Int?, locale: String) -> Unit,
    pin: (event: FeedEvent, blurbContent: String?) -> Unit,
    searchFollower: (String) -> Unit,
    isCritiqueBrainzLinked: suspend () -> Boolean?,
    onPlay: (event: FeedEvent) -> Unit,
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
        onScrollToTop {
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
            beyondBoundsPageCount = 1
        ) { position ->
            when (position) {
                0 -> MyFeed(
                    listState = myFeedListState,
                    pagingData = myFeedPagingData,
                    uiState = uiState.myFeedState,
                    onDeleteOrHide = onDeleteOrHide,
                    recommendTrack = recommendTrack,
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
                    onPlay = onPlay
                )
            
                1 -> FollowListens(
                    listState = followListensListState,
                    pagingData = followListensPagingData,
                    recommendTrack = recommendTrack,
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
                    onPlay = onPlay
                )
            
                2 -> SimilarListens(
                    listState = similarListensListState,
                    pagingData = similarListensPagingData,
                    recommendTrack = recommendTrack,
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
                    onPlay = onPlay
                )
            }
        }
        
        Column(Modifier.fillMaxWidth()) {
            ErrorBar(error = uiState.error, onErrorShown = onErrorShown)
            NavigationChips(currentPageStateProvider = { pagerState.currentPage }) { position ->
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
        onPin = pin,
        searchUserResult = uiState.searchResult,
        searchUsers = searchFollower,
        onPersonallyRecommend = personallyRecommendTrack,
        isCritiqueBrainzLinked = isCritiqueBrainzLinked,
        onReview = review
    )
    
}

@Composable
private fun Dialogs(
    deactivateDialog: () -> Unit,
    currentDialog: Dialog,
    currentEventIndex: Int?,
    pagingSource: LazyPagingItems<FeedUiEventItem>,
    searchUserResult: List<String>,
    onPin: (event: FeedEvent, blurbContent: String) -> Unit,
    searchUsers: (String) -> Unit,
    onPersonallyRecommend: (event: FeedEvent, users: List<String>, blurbContent: String) -> Unit,
    isCritiqueBrainzLinked: suspend () -> Boolean?,
    onReview: (event: FeedEvent, type: ReviewEntityType, blurbContent: String, rating: Int?, locale: String) -> Unit
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
                    onPin(pagingSource[currentEventIndex]?.event!!, blurbContent)
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
                searchUsers = searchUsers,
                onSubmit = { users, blurbContent ->
                    onPersonallyRecommend(pagingSource[currentEventIndex]?.event!!, users, blurbContent)
                }
            )
        }
        Dialog.REVIEW -> {
            val metadata = pagingSource[currentEventIndex!!]?.event?.metadata
            ReviewDialog(
                trackName = metadata?.trackMetadata?.trackName
                    ?: if (metadata?.entityType == ReviewEntityType.RECORDING.code) metadata.entityName else return,
                artistName = metadata?.trackMetadata?.artistName
                    ?: if (metadata?.entityType == ReviewEntityType.ARTIST.code) metadata.entityName else null,
                releaseName = metadata?.trackMetadata?.releaseName
                    ?: if (metadata?.entityType == ReviewEntityType.RELEASE_GROUP.code) metadata.entityName else null,
                onDismiss = deactivateDialog,
                isCritiqueBrainzLinked = isCritiqueBrainzLinked,
                onSubmit = { type, blurbContent, rating, locale ->
                    onReview(pagingSource[currentEventIndex]?.event!!, type, blurbContent, rating, locale)
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
                        }
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
    uriHandler: UriHandler = LocalUriHandler.current
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
                    artistName = event.metadata.trackMetadata?.artistName ?: "Unknown",
                    coverArtUrl =
                        Utils.getCoverArtUrl(
                            caaReleaseMbid = event.metadata.trackMetadata?.mbidMapping?.caaReleaseMbid,
                            caaId = event.metadata.trackMetadata?.mbidMapping?.caaId
                        ),
                    enableDropdownIcon = true,
                    enableTrailingContent = true,
                    trailingContent = { modifier ->
                        Column(modifier, horizontalAlignment = Alignment.End) {
                            TitleAndSubtitle(
                                title = event.username ?: "Unknown",
                                titleColor = ListenBrainzTheme.colorScheme.lbSignature
                            )
                            Date(
                                event = event,
                                parentUser = parentUser,
                                eventType = eventType
                            )
                        }
                    },
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
                    }
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
    uriHandler: UriHandler = LocalUriHandler.current
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
                    artistName = event.metadata.trackMetadata?.artistName ?: "Unknown",
                    coverArtUrl =
                        Utils.getCoverArtUrl(
                            caaReleaseMbid = event.metadata.trackMetadata?.mbidMapping?.caaReleaseMbid,
                            caaId = event.metadata.trackMetadata?.mbidMapping?.caaId
                        ),
                    enableDropdownIcon = true,
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
                                titleColor = ListenBrainzTheme.colorScheme.lbSignature
                            )
                            Date(
                                event = event,
                                parentUser = parentUser,
                                eventType = eventType
                            )
                        }
                    },
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
                    }
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
fun NavigationChips(
    currentPageStateProvider: () -> Int,
    scope: CoroutineScope = rememberCoroutineScope(),
    onClick: suspend (Int) -> Unit
){
    Row(modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .background(
            Brush.verticalGradient(
                listOf(
                    ListenBrainzTheme.colorScheme.background,
                    Color.Transparent
                )
            )
        )
    ) {
        Spacer(modifier = Modifier.width(ListenBrainzTheme.paddings.chipsHorizontal/2))
        repeat(3){ position ->
            ElevatedSuggestionChip(
                modifier = Modifier.padding(ListenBrainzTheme.paddings.chipsHorizontal),
                colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                    if (currentPageStateProvider() == position) {
                        ListenBrainzTheme.colorScheme.chipSelected
                    } else {
                        ListenBrainzTheme.colorScheme.chipUnselected
                    }
                ),
                shape = ListenBrainzTheme.shapes.chips,
                elevation = SuggestionChipDefaults.elevatedSuggestionChipElevation(elevation = 4.dp),
                label = {
                    Text(
                        text = when(position) {
                            0 ->"My Feed"
                            1 -> "Follow Listens"
                            else -> "Similar Listens"
                        },
                        style = ListenBrainzTheme.textStyles.chips,
                        color = ListenBrainzTheme.colorScheme.text,
                    )
                },
                onClick = { scope.launch { onClick(position) } }
            )
        }
    }
    
}

@Composable
private fun RetryButton(modifier: Modifier = Modifier, show: Boolean, onClick: () -> Unit) {
    if (show) {
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
                onScrollToTop = {},
                onDeleteOrHide = {_,_,_ -> },
                onErrorShown = {},
                recommendTrack = {},
                personallyRecommendTrack = {_,_,_ -> },
                review = {_,_,_,_,_ ->},
                pin = {_,_ ->},
                searchFollower = {},
                isCritiqueBrainzLinked = {true},
                onPlay = {}
            )
        }
        
    }
}