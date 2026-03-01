package org.listenbrainz.android.ui.screens.feed

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.feed.FeedData
import org.listenbrainz.android.model.feed.FeedEventType
import org.listenbrainz.android.repository.feed.FeedRepository
import org.listenbrainz.android.util.Resource

class MyFeedPagingSource (
    private val username: suspend () -> String,
    private val addEntryToMap: (Int, Boolean) -> Unit,
    private val onError: (error: ResponseError?) -> Unit,
    private val feedRepository: FeedRepository,
    private val ioDispatcher: CoroutineDispatcher
): PagingSource<Long, FeedUiEventItem>() {

    override fun getRefreshKey(state: PagingState<Long, FeedUiEventItem>): Long {
        return System.currentTimeMillis() / 1000
    }
    
    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, FeedUiEventItem> {
        
        val username = username()
        if (username.isEmpty()) {
            val error = ResponseError.Unauthorised(actualResponse = "Login to access feed.")
            onError(error)
            return LoadResult.Error(Exception(error.toast))
        }
        
        val result = withContext(ioDispatcher) {
            feedRepository.getFeedEvents(username = username, maxTs = params.key, count = params.loadSize)
        }
        
        return when (result.status) {
            Resource.Status.SUCCESS -> {
                
              val processedEvents = withContext(ioDispatcher) {
    processFeedEvents(result.data)
}

                val nextKey = processedEvents.lastOrNull()?.event?.created?.let { newKey ->
                    // Termination condition.
                    if (params.key != null && newKey >= params.key!!)
                        return LoadResult.Page(
                            data = emptyList(),
                            prevKey = null,
                            nextKey = null
                        )
                    else
                        newKey
                    
                }

                LoadResult.Page(
                    data = processedEvents,
                    prevKey = null,
                    nextKey = nextKey
                )
            }
            else -> {
                onError(result.error)
                LoadResult.Error(Exception(result.error?.toast))
            }
            
        }
        
    }
    
  private suspend fun processFeedEvents(feedData: FeedData?): List<FeedUiEventItem> {

    val items = mutableListOf<FeedUiEventItem>()
   


    feedData?.payload?.events?.forEach { event ->

        if (event.hidden == true) {
            event.id?.let { addEntryToMap(it, true) }
        }

        val eventType = FeedEventType.resolveEvent(event)

   var referenced: org.listenbrainz.android.model.feed.FeedEvent? = null

if (eventType == FeedEventType.THANKS) {

    val originalId = event.metadata.originalEventId
    val originalType = event.metadata.originalEventType

    if (originalId != null) {
        try {

            val response = if (originalType == "recording_pin") {
                feedRepository.getPinById(originalId)
            } else {
                feedRepository.getFeedEventById(originalId)
            }

            if (response.status == Resource.Status.SUCCESS) {
                referenced = response.data
            }

        } catch (_: Exception) {}
    }
}


        items.add(
            FeedUiEventItem(
                event = event,
                eventType = eventType,
                parentUser = feedData.payload.userId,
                referencedEvent = referenced
            )
        )
    }

    return items
}
}