package org.listenbrainz.android.repository.feed

import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.SocialResponse
import org.listenbrainz.android.model.feed.FeedData
import org.listenbrainz.android.model.feed.FeedEventDeletionData
import org.listenbrainz.android.model.feed.FeedEventVisibilityData
import org.listenbrainz.android.service.FeedServiceKtor
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils.logAndReturn
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val service: FeedServiceKtor
) : FeedRepository {
    
    override suspend fun getFeedEvents(
        username: String?,
        maxTs: Long?,
        minTs: Long?,
        count: Int
    ): Resource<FeedData> = runCatching {
        if (username.isNullOrEmpty()) return@runCatching ResponseError.AUTH_HEADER_NOT_FOUND.asResource()

        val data = service.getFeedEvents(
            username = username,
            maxTs = maxTs,
            minTs = minTs,
            count = count
        )
        Resource.success(data)
    }.getOrElse {
        logAndReturn(it)
    }
    
    
    override suspend fun getFeedFollowListens(
        username: String?,
        maxTs: Long?,
        minTs: Long?,
        count: Int
    ): Resource<FeedData> = runCatching {
        if (username.isNullOrEmpty()) return@runCatching ResponseError.AUTH_HEADER_NOT_FOUND.asResource()

        val data = service.getFeedFollowListens(
            username = username,
            maxTs = maxTs,
            minTs = minTs,
            count = count
        )
        Resource.success(data)
    }.getOrElse {
        logAndReturn(it)
    }
    
    
    override suspend fun getFeedSimilarListens(
        username: String?,
        maxTs: Long?,
        minTs: Long?,
        count: Int
    ): Resource<FeedData> = runCatching {
        if (username.isNullOrEmpty()) return@runCatching ResponseError.AUTH_HEADER_NOT_FOUND.asResource()

        val data = service.getFeedSimilarListens(
            username = username,
            maxTs = maxTs,
            minTs = minTs,
            count = count
        )
        Resource.success(data)
    }.getOrElse {
        logAndReturn(it)
    }


    override suspend fun deleteEvent(
        username: String?,
        data: FeedEventDeletionData
    ): Resource<SocialResponse> =
        runCatching {
            if (username.isNullOrEmpty()) return@runCatching ResponseError.AUTH_HEADER_NOT_FOUND.asResource()

            val response = service.deleteEvent(
                username = username,
                body = data
            )
            Resource.success(response)
        }.getOrElse {
            logAndReturn(it)
        }


    override suspend fun hideEvent(
        username: String?,
        data: FeedEventVisibilityData
    ): Resource<SocialResponse> =
        runCatching {
            if (username.isNullOrEmpty()) return@runCatching ResponseError.AUTH_HEADER_NOT_FOUND.asResource()

            val response = service.hideEvent(
                username = username,
                body = data
            )
            Resource.success(response)
        }.getOrElse {
            logAndReturn(it)
        }


    override suspend fun unhideEvent(
        username: String?,
        data: FeedEventVisibilityData
    ): Resource<SocialResponse> =
        runCatching {
            if (username.isNullOrEmpty()) return@runCatching ResponseError.AUTH_HEADER_NOT_FOUND.asResource()

            val response = service.unhideEvent(
                username = username,
                body = data
            )
            Resource.success(response)
        }.getOrElse {
            logAndReturn(it)
        }
    
}