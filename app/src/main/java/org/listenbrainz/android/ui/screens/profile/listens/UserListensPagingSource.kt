package org.listenbrainz.android.ui.screens.profile.listens

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.listenbrainz.android.model.Listen
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.repository.listens.ListensRepository
import org.listenbrainz.android.util.Resource

class UserListensPagingSource(
    private val username: String?,
    private val onError: (error: ResponseError?) -> Unit,
    private val listensRepository: ListensRepository,
    private val ioDispatcher: CoroutineDispatcher,
) : PagingSource<Long, Listen>() {

    override fun getRefreshKey(state: PagingState<Long, Listen>): Long? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Listen> {
        if (username.isNullOrEmpty()) {
            val error = ResponseError.BadRequest(
                actualResponse = "Some error occurred! Username not found"
            )
            onError(error)
            return LoadResult.Error(Exception(error.toast))
        }

        val result = withContext(ioDispatcher) {
            listensRepository.fetchUserListens(
                username = username,
                count = params.loadSize,
                maxTs = params.key
            )
        }

        return when (result.status) {
            Resource.Status.SUCCESS -> {
                val listens = result.data?.payload?.listens ?: emptyList()

                // Get the minimum listened_at timestamp from the current batch
                // This will be used as maxTs for the next page
                val nextKey = if (listens.isNotEmpty()) {
                    listens
                        .minOfOrNull { it.listenedAt ?: it.insertedAt ?: Long.MAX_VALUE }
                        .takeIf { it != Long.MAX_VALUE }
                } else {
                    null
                }

                println(listens.size)
                LoadResult.Page(
                    data = listens,
                    prevKey = null, // We only support forward pagination
                    nextKey = nextKey
                )
            }

            else -> {
                onError(result.error)
                LoadResult.Error(Exception(result.error?.toast))
            }
        }
    }
}
