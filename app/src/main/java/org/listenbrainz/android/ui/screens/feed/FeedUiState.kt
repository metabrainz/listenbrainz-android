package org.listenbrainz.android.ui.screens.feed

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.listenbrainz.android.model.FeedEvent
import org.listenbrainz.android.model.FeedEventType
import org.listenbrainz.android.model.ResponseError

data class FeedUiState(
    val myFeedData: FeedUiEventData = FeedUiEventData(),
    val isLoading: Boolean = true,
    val error: ResponseError? = null
)

data class FeedUiEventData(
    val isHiddenMap: Map<Int, Boolean> = emptyMap(),
    val isDeletedMap: Map<Int, Boolean> = emptyMap(),
    var eventList: Flow<PagingData<FeedUiEventItem>> = emptyFlow()
)

data class FeedUiEventItem(
    val eventType: FeedEventType,
    val event: FeedEvent,
    val parentUser: String = ""
)