package org.listenbrainz.android.ui.screens.feed

import androidx.compose.runtime.Stable
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.feed.FeedEvent
import org.listenbrainz.android.model.feed.FeedEventType

/** Top most state wrapper for Feed Screen.*/
@Stable
data class FeedUiState(
    val myFeedState: FeedUiEventData = FeedUiEventData(),
    val followListensFeedState: FeedUiEventData = FeedUiEventData(),
    val similarListensFeedState: FeedUiEventData = FeedUiEventData(),
    val searchResult: List<String> = emptyList(),
    val error: ResponseError? = null
)

/** Data held by each screen.*/
@Stable
data class FeedUiEventData(
    val isHiddenMap: Map<Int, Boolean> = emptyMap(),
    val isDeletedMap: Map<Int, Boolean> = emptyMap(),
    val eventList: Flow<PagingData<FeedUiEventItem>> = emptyFlow()
)

/** UI representation for one feed event.*/
@Stable
data class FeedUiEventItem(
    val eventType: FeedEventType,
    val event: FeedEvent,
    val parentUser: String = ""
)