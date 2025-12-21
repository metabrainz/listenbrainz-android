package org.listenbrainz.android.repository.feed

import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.SocialResponse
import org.listenbrainz.android.model.feed.FeedData
import org.listenbrainz.android.model.feed.FeedEventDeletionData
import org.listenbrainz.android.model.feed.FeedEventVisibilityData
import org.listenbrainz.android.service.FeedServiceKtor
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils.parseKtorResponse
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val service: FeedServiceKtor
) : FeedRepository {
    
    override suspend fun getFeedEvents(
        username: String?,
        maxTs: Long?,
        minTs: Long?,
        count: Int
    ): Resource<FeedData> = parseKtorResponse {
        failIf(username.isNullOrEmpty()) {
            ResponseError.AUTH_HEADER_NOT_FOUND
        }

        service.getFeedEvents(
            username = username,
            maxTs = maxTs,
            minTs = minTs,
            count = count
        )
    }
    
    
    override suspend fun getFeedFollowListens(
        username: String?,
        maxTs: Long?,
        minTs: Long?,
        count: Int
    ): Resource<FeedData> = parseKtorResponse {
        failIf(username.isNullOrEmpty()) {
            ResponseError.AUTH_HEADER_NOT_FOUND
        }

        service.getFeedFollowListens(
            username = username,
            maxTs = maxTs,
            minTs = minTs,
            count = count
        )
    }
    
    
    override suspend fun getFeedSimilarListens(
        username: String?,
        maxTs: Long?,
        minTs: Long?,
        count: Int
    ): Resource<FeedData> = parseKtorResponse {
        failIf(username.isNullOrEmpty()) {
            ResponseError.AUTH_HEADER_NOT_FOUND
        }

        service.getFeedSimilarListens(
            username = username,
            maxTs = maxTs,
            minTs = minTs,
            count = count
        )
    }


    override suspend fun deleteEvent(
        username: String?,
        data: FeedEventDeletionData
    ): Resource<SocialResponse> = parseKtorResponse {
        failIf(username.isNullOrEmpty()) {
            ResponseError.AUTH_HEADER_NOT_FOUND
        }

        service.deleteEvent(
            username = username,
            body = data
        )
    }

    override suspend fun hideEvent(
        username: String?,
        data: FeedEventVisibilityData
    ): Resource<SocialResponse> = parseKtorResponse {
        failIf(username.isNullOrEmpty()) {
            ResponseError.AUTH_HEADER_NOT_FOUND
        }

        service.hideEvent(
            username = username, body = data
        )
    }

    override suspend fun unhideEvent(
        username: String?,
        data: FeedEventVisibilityData
    ): Resource<SocialResponse> = parseKtorResponse {
        failIf(username.isNullOrEmpty()) {
            ResponseError.AUTH_HEADER_NOT_FOUND
        }

        service.unhideEvent(
            username = username,
            body = data
        )
    }
}