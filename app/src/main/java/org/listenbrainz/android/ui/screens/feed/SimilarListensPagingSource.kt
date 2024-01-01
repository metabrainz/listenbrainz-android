package org.listenbrainz.android.ui.screens.feed

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.repository.feed.FeedRepository
import org.listenbrainz.android.util.Resource

class SimilarListensPagingSource(
    private val username: suspend () -> String,
    private val onError: (error: ResponseError?) -> Unit,
    private val feedRepository: FeedRepository,
    private val ioDispatcher: CoroutineDispatcher
): PagingSource<Int, FeedUiEventItem>() {
    
    override fun getRefreshKey(state: PagingState<Int, FeedUiEventItem>): Int? {
        return (System.currentTimeMillis()/1000).toInt()
    }
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FeedUiEventItem> {
        
        val username = username()
        if (username.isEmpty()) {
            val error = ResponseError.UNAUTHORISED.apply { actualResponse = "Login to access feed." }
            onError(error)
            return LoadResult.Error(Exception(error.toast()))
        }
        
        val result = withContext(ioDispatcher) {
            feedRepository.getFeedSimilarListens(username = username, maxTs = params.key, count = params.loadSize)
        }
        
        return when (result.status) {
            Resource.Status.SUCCESS -> {
                
                val processedEvents = FollowListensPagingSource.processFeedEvents(result.data)
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
                LoadResult.Error(Exception(result.error?.toast()))
            }
            
        }
        
    }
}