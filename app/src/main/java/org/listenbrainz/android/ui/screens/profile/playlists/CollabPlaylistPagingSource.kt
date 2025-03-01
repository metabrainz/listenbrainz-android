package org.listenbrainz.android.ui.screens.profile.playlists

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.userPlaylist.UserPlaylist
import org.listenbrainz.android.repository.playlists.PlaylistDataRepository
import org.listenbrainz.android.repository.user.UserRepository
import org.listenbrainz.android.util.Resource

class CollabPlaylistPagingSource(
    private val username:String?,
    private val onError: (error: ResponseError?) -> Unit,
    private val userRepository: UserRepository,
    private val playlistDataRepository: PlaylistDataRepository,
    private val ioDispatcher: CoroutineDispatcher
) : PagingSource<Int, UserPlaylist>() {
    override fun getRefreshKey(state: PagingState<Int, UserPlaylist>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UserPlaylist> {
        if (username.isNullOrEmpty()) {
            val error = ResponseError.BAD_REQUEST.apply {
                actualResponse = "Some error occurred! Username not found"
            }
            onError(error)
            return LoadResult.Error(Exception(error.toast))
        }

        val result = withContext(ioDispatcher) {
            userRepository.getUserCollabPlaylists(
                username = username,
                offset = params.key ?: 0,
                count = params.loadSize
            )
        }

        return when (result.status) {
            Resource.Status.SUCCESS -> {
                val data = (result.data?.playlists ?: emptyList()).map { it.playlist }
                val dataWithCoverArt = data.map {
                    val coverArtResult = it.getPlaylistMBID()
                        ?.let { it1 -> playlistDataRepository.getPlaylistCoverArt(it1) }
                    it.copy(coverArt = coverArtResult?.data)
                }
                val nextKey = if (data.isEmpty()) null else params.key?.plus(params.loadSize)
                LoadResult.Page(
                    data = dataWithCoverArt,
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

}