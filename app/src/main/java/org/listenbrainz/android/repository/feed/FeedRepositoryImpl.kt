package org.listenbrainz.android.repository.feed

import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.SocialResponse
import org.listenbrainz.android.model.feed.FeedData
import org.listenbrainz.android.model.feed.FeedEventDeletionData
import org.listenbrainz.android.model.feed.FeedEventVisibilityData
import org.listenbrainz.android.service.FeedService
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils.parseResponse
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val service: FeedService
) : FeedRepository {
    
    override suspend fun getFeedEvents(
        username: String?,
        maxTs: Int?,
        minTs: Int?,
        count: Int
    ) : Resource<FeedData> = parseResponse {
        if (username.isNullOrEmpty()) return ResponseError.AUTH_HEADER_NOT_FOUND.asResource()
        
        service.getFeedEvents(
            username = username,
            maxTs = maxTs,
            minTs = minTs,
            count = count
        )
    }
    
    
    override suspend fun getFeedFollowListens(
        username: String?,
        maxTs: Int?,
        minTs: Int?,
        count: Int
    ): Resource<FeedData> = parseResponse {
        if (username.isNullOrEmpty()) return ResponseError.AUTH_HEADER_NOT_FOUND.asResource()
        
        service.getFeedFollowListens(
            username = username,
            maxTs = maxTs,
            minTs = minTs,
            count = count
        )
    }
    
    
    override suspend fun getFeedSimilarListens(
        username: String?,
        maxTs: Int?,
        minTs: Int?,
        count: Int
    ): Resource<FeedData> =parseResponse {
        if (username.isNullOrEmpty()) return ResponseError.AUTH_HEADER_NOT_FOUND.asResource()
        
        service.getFeedSimilarListens(
            username = username,
            maxTs = maxTs,
            minTs = minTs,
            count = count
        )
    }
    
    
    override suspend fun deleteEvent(username: String?, data: FeedEventDeletionData) : Resource<SocialResponse> =
        parseResponse {
            if (username.isNullOrEmpty()) return ResponseError.AUTH_HEADER_NOT_FOUND.asResource()
            
            service.deleteEvent(
                username = username,
                body = data
            )
        }
    
    
    override suspend fun hideEvent(username: String?, data: FeedEventVisibilityData) : Resource<SocialResponse> =
        parseResponse {
            if (username.isNullOrEmpty()) return ResponseError.AUTH_HEADER_NOT_FOUND.asResource()
            
            service.hideEvent(
                username = username,
                body = data
            )
        }
    
    
    override suspend fun unhideEvent(username: String?, data: FeedEventVisibilityData) : Resource<SocialResponse> =
        parseResponse {
            if (username.isNullOrEmpty()) return ResponseError.AUTH_HEADER_NOT_FOUND.asResource()
            
            service.unhideEvent(
                username = username,
                body = data
            )
        }
    
}