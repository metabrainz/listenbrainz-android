package org.listenbrainz.android.ui.screens.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import org.listenbrainz.android.model.FeedEvent
import org.listenbrainz.android.model.FeedEventType
import org.listenbrainz.android.ui.components.ErrorBar
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.FeedViewModel

@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value
    
    FeedScreen(
        uiState = uiState,
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


@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun FeedScreen(
    uiState: FeedUiState,
    onDeleteOrHide: (event: FeedEvent, eventType: FeedEventType, parentUser: String) -> Unit,
    onDropDownClick: () -> Unit,
    onErrorShown: () -> Unit,
    onPlay: () -> Unit,
) {
    
    val myFeedPagingData = uiState.myFeedData.eventList.collectAsLazyPagingItems()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = myFeedPagingData.itemCount == 0 && myFeedPagingData.loadState.refresh is LoadState.Loading,
        onRefresh = {
            myFeedPagingData.refresh()
        }
    )
    
    Box(
        Modifier
            .fillMaxSize()
            .pullRefresh(state = pullRefreshState)
    ) {
        
        if (myFeedPagingData.itemCount == 0 && myFeedPagingData.loadState.refresh is LoadState.Error) {
            Button(
                modifier = Modifier.align(Alignment.Center),
                onClick = { myFeedPagingData.retry() },
                colors = ButtonDefaults.buttonColors(containerColor = ListenBrainzTheme.colorScheme.lbSignature)
            ) {
                Text(
                    text = "Retry",
                    color = ListenBrainzTheme.colorScheme.onLbSignature,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        LazyColumn(Modifier.fillMaxWidth()) {
            
            items(count = myFeedPagingData.itemCount) { index: Int ->
                
                myFeedPagingData[index]?.apply {
                    AnimatedVisibility(
                        visible = uiState.myFeedData.isDeletedMap[event.id] != true,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        eventType.Content(
                            event = event,
                            parentUser = parentUser,
                            isHidden = uiState.myFeedData.isHiddenMap[event.id] == true,
                            onDeleteOrHide = {
                                onDeleteOrHide(
                                    event,
                                    eventType,
                                    parentUser
                                ) },
                            onDropdownClick = { onDropDownClick() },
                            onClick = { onPlay() }
                        )
                        
                    }
                }
                
            }
            
            item {
                Box(Modifier.fillMaxWidth()) {
                    if (
                        myFeedPagingData.itemCount != 0 &&
                        myFeedPagingData.loadState.refresh is LoadState.Loading ||
                        myFeedPagingData.loadState.append is LoadState.Loading
                    ) {
        
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(vertical = 32.dp) // Default height of m3 button is 40.dp
                                .size(24.dp),
                            strokeCap = StrokeCap.Round,
                            color = ListenBrainzTheme.colorScheme.lbSignature
                        )
        
                    } else if (myFeedPagingData.loadState.append is LoadState.Error) {
                        
                        Button(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(vertical = 24.dp),
                            onClick = { myFeedPagingData.retry() },
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
                
            }
            
        }
        
        ErrorBar(error = uiState.error, onErrorShown = onErrorShown)
    
        PullRefreshIndicator(
            modifier = Modifier.align(Alignment.TopCenter),
            refreshing = myFeedPagingData.itemCount == 0 && myFeedPagingData.loadState.refresh is LoadState.Loading,
            state = pullRefreshState
        )
    }
    
}

@Preview
@Composable
private fun FeedScreenPreview() {
    ListenBrainzTheme {
        FeedScreen(
            uiState = FeedUiState(),
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