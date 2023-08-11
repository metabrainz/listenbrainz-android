package org.listenbrainz.android.ui.screens.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
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
import org.listenbrainz.android.model.FeedEvent
import org.listenbrainz.android.model.FeedEventType
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.ui.components.ErrorBar
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.components.TitleAndSubtitle
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Utils
import org.listenbrainz.android.viewmodel.FeedViewModel

@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel(),
    scrollToTopState: Boolean,
    onScrollToTop: (suspend () -> Unit) -> Unit
) {
    val uiState = viewModel.uiState.collectAsState().value
    
    FeedScreen(
        uiState = uiState,
        scrollToTopState = scrollToTopState,
        onScrollToTop = onScrollToTop,
        onDeleteOrHide = { event, eventType, parentUser ->
            viewModel.hideOrDeleteEvent(event, eventType, parentUser)
        },
        onErrorShown = { viewModel.clearError() },
        onDropDownClick = {
        
        },
        onPlay = {
        
        }
    )
}


@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
private fun FeedScreen(
    uiState: FeedUiState,
    scrollToTopState: Boolean,
    onScrollToTop: (suspend () -> Unit) -> Unit,
    onDeleteOrHide: (event: FeedEvent, eventType: FeedEventType, parentUser: String) -> Unit,
    onDropDownClick: () -> Unit,
    onErrorShown: () -> Unit,
    onPlay: () -> Unit,
) {
    val myFeedPagingData = uiState.myFeedState.data.eventList.collectAsLazyPagingItems()
    val myFeedListState = rememberLazyListState()
    
    val followListensPagingData = uiState.followListensFeedState.data.eventList.collectAsLazyPagingItems()
    val followListensListState = rememberLazyListState()
    
    val similarListensPagingData = uiState.similarListensFeedState.data.eventList.collectAsLazyPagingItems()
    val similarListensListState = rememberLazyListState()
    
    val pagerState = rememberPagerState()
    val isRefreshing = {
        when (pagerState.currentPage) {
            0 -> myFeedPagingData.itemCount == 0 && myFeedPagingData.loadState.refresh is LoadState.Loading
            1 -> followListensPagingData.itemCount == 0 && followListensPagingData.loadState.refresh is LoadState.Loading
            2 -> similarListensPagingData.itemCount == 0 && similarListensPagingData.loadState.refresh is LoadState.Loading
            else -> false
        }
    }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing(),
        onRefresh = {
            when (pagerState.currentPage){
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
        
        RetryButton(Modifier.align(Alignment.Center), myFeedPagingData)
    
        Column {
            NavigationChips(currentPageStateProvider = { pagerState.currentPage }) { position ->
                pagerState.animateScrollToPage(position)
            }
            HorizontalPager(
                pageCount = 3,
                state = pagerState,
            ) { position ->
                when (position){
                    0 -> MyFeed(myFeedListState, myFeedPagingData, uiState.myFeedState, onDeleteOrHide, onDropDownClick, onPlay)
                    1 -> FollowListens(followListensListState, followListensPagingData, onDropDownClick, onPlay)
                    2 -> SimilarListens(similarListensListState, similarListensPagingData, onDropDownClick, onPlay)
                }
            }
        }
        
        ErrorBar(error = uiState.error, onErrorShown = onErrorShown)
    
        PullRefreshIndicator(
            modifier = Modifier.align(Alignment.TopCenter),
            refreshing = isRefreshing(),
            state = pullRefreshState
        )
    }
    
}


@Composable
private fun MyFeed(
    listState: LazyListState,
    pagingData: LazyPagingItems<FeedUiEventItem>,
    uiState: FeedScreenUiState,
    onDeleteOrHide: (event: FeedEvent, eventType: FeedEventType, parentUser: String) -> Unit,
    onDropDownClick: () -> Unit,
    onPlay: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = LocalConfiguration.current.screenWidthDp.dp),
        state = listState
    ) {
        
        items(count = pagingData.itemCount) { index: Int ->
            
            pagingData[index]?.apply {
                AnimatedVisibility(
                    visible = uiState.data.isDeletedMap[event.id] != true,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    eventType.Content(
                        event = event,
                        parentUser = parentUser,
                        isHidden = uiState.data.isHiddenMap[event.id] == true,
                        onDeleteOrHide = {
                            onDeleteOrHide(
                                event,
                                eventType,
                                parentUser
                            )
                        },
                        onDropdownClick = { onDropDownClick() },
                        onClick = { onPlay() }
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
    onDropDownClick: () -> Unit,
    onPlay: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = LocalConfiguration.current.screenWidthDp.dp),
        state = listState
    ) {
        
        items(count = pagingData.itemCount) { index: Int ->
            
            pagingData[index]?.apply {
                
                // Main Card
                Column(
                    modifier = Modifier.padding(horizontal = ListenBrainzTheme.paddings.horizontal)
                ) {
                    ListenCardSmall(
                        releaseName = event.metadata.trackMetadata?.releaseName ?: "Unknown",
                        artistName = event.metadata.trackMetadata?.artistName ?: "Unknown",
                        coverArtUrl =
                        Utils.getCoverArtUrl(
                            caaReleaseMbid = event.metadata.trackMetadata?.mbidMapping?.caa_release_mbid,
                            caaId = event.metadata.trackMetadata?.mbidMapping?.caa_id
                        ),
                        enableDropdownIcon = true,
                        enableTrailingContent = true,
                        trailingContent = { modifier ->
                            TitleAndSubtitle(modifier = modifier, title = event.username ?: "Unknown")
                        },
                        onDropdownIconClick = onDropDownClick,
                    ) {
                        onPlay()
                    }
                    
                    // Date
                    Date()
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
    onDropDownClick: () -> Unit,
    onPlay: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = LocalConfiguration.current.screenWidthDp.dp),
        state = listState
    ) {
        
        items(count = pagingData.itemCount) { index: Int ->
            
            pagingData[index]?.apply {
                // Main Card
                Column(
                    modifier = Modifier.padding(horizontal = ListenBrainzTheme.paddings.horizontal)
                ) {
                    ListenCardSmall(
                        releaseName = event.metadata.trackMetadata?.releaseName ?: "Unknown",
                        artistName = event.metadata.trackMetadata?.artistName ?: "Unknown",
                        coverArtUrl =
                            Utils.getCoverArtUrl(
                                caaReleaseMbid = event.metadata.trackMetadata?.mbidMapping?.caa_release_mbid,
                                caaId = event.metadata.trackMetadata?.mbidMapping?.caa_id
                            )
                        ,
                        enableDropdownIcon = true,
                        enableTrailingContent = true,
                        trailingContent = { modifier ->
                            TitleAndSubtitle(modifier = modifier, title = event.username ?: "Unknown")
                        },
                        onDropdownIconClick = onDropDownClick,
                    ) {
                        onPlay()
                    }
                    
                    // Date
                    Date()
                }
            }
        }
        
        item {
            PagerRearLoadingIndicator(pagingData)
        }
        
    }
}


@Composable
fun NavigationChips(
    currentPageStateProvider: () -> Int,
    scope: CoroutineScope = rememberCoroutineScope(),
    onClick: suspend (Int) -> Unit
){
    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
        repeat(3){ position ->
            if (position == 0) {
                Spacer(modifier = Modifier.width(ListenBrainzTheme.paddings.chipsHorizontal/2))
            }
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
private fun FeedUiEventItem.Date() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Date(event = event, parentUser = parentUser, eventType = eventType)
    }
}

@Composable
private fun RetryButton(modifier: Modifier = Modifier, pagingData: LazyPagingItems<FeedUiEventItem>) {
    if (pagingData.itemCount == 0 && pagingData.loadState.refresh is LoadState.Error) {
        Button(
            modifier = modifier,
            onClick = { pagingData.retry() },
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
    Box(Modifier.fillMaxWidth()) {
        if (
            pagingData.itemCount != 0 &&
            pagingData.loadState.refresh is LoadState.Loading ||
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
                pagingData = pagingData
            )
            
        }
    }
}

@Preview
@Composable
private fun FeedScreenPreview() {
    ListenBrainzTheme {
        FeedScreen(
            uiState = FeedUiState(
                FeedScreenUiState(
                    FeedUiEventData(eventList = flow {
                        emit(PagingData.from(
                            listOf(
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
                            )
                        ))
                    })
                )
            ),
            scrollToTopState = false,
            onScrollToTop = {},
            onDeleteOrHide = { _, _, _ ->
        
            },
            onErrorShown = {},
            onDropDownClick = {
        
            },
            onPlay = {
        
            }
        )
    }
}