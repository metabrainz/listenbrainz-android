package org.listenbrainz.android.ui.screens.feed

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.listenbrainz.android.model.FeedData
import org.listenbrainz.android.model.FeedEventType
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.repository.feed.FeedRepository
import org.listenbrainz.android.util.Resource

class SimilarListensPagingSource(
    private val username: () -> String?,
    private val onError: (error: ResponseError?) -> Unit,
    private val feedRepository: FeedRepository,
    private val ioDispatcher: CoroutineDispatcher
): PagingSource<Int, FeedUiEventItem>() {
    
    override fun getRefreshKey(state: PagingState<Int, FeedUiEventItem>): Int? {
        return if ((state.anchorPosition ?: 0) < 10)
            null
        else
            state.lastItemOrNull()?.event?.created
    }
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FeedUiEventItem> {
        
        val username = username()
        if (username.isNullOrEmpty()) {
            val error = ResponseError.UNAUTHORISED.apply { actualResponse = "Login to access feed." }
            onError(ResponseError.UNAUTHORISED.apply { actualResponse = "Login to access feed." })
            return LoadResult.Error(Exception(error.toast()))
        }
        
        val result = withContext(ioDispatcher) {
            feedRepository.getFeedSimilarListens(username = username, maxTs = params.key, count = params.loadSize)
        }
        
        return when (result.status) {
            Resource.Status.SUCCESS -> {
                
                val processedEvents = processFeedEvents(result.data)
                val nextKey = processedEvents.lastOrNull()?.event?.created
                
                LoadResult.Page(
                    data = processedEvents,
                    prevKey = null,
                    nextKey = nextKey
                )
            }
            else -> {
                onError(result.error)
                LoadResult.Error(Exception(result.error?.toast()))
            }
            
        }
        
    }
    
    private fun processFeedEvents(similarListens: FeedData?): List<FeedUiEventItem> {
        
        return mutableListOf<FeedUiEventItem>().apply {
            
            similarListens?.payload?.events?.forEach { event ->
                add(
                    FeedUiEventItem(
                        event = event,
                        eventType = FeedEventType.resolveEvent(event),
                        parentUser = similarListens.payload.userId
                    )
                )
            }
        }
    }
}