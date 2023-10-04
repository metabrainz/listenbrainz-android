package org.listenbrainz.android.repository.feed

import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.SocialResponse
import org.listenbrainz.android.model.feed.FeedData
import org.listenbrainz.android.model.feed.FeedEventDeletionData
import org.listenbrainz.android.model.feed.FeedEventVisibilityData
import org.listenbrainz.android.service.FeedService
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val service: FeedService
) : FeedRepository {
    
    override suspend fun getFeedEvents(
        username: String?,
        maxTs: Int?,
        minTs: Int?,
        count: Int
    ) : Resource<FeedData> =
        runCatching {
            if (username.isNullOrEmpty()) return@runCatching Resource.failure(error = ResponseError.AUTH_HEADER_NOT_FOUND)
            
            val response = service.getFeedEvents(
                username = username,
                maxTs = maxTs,
                minTs = minTs,
                count = count
            )
        
            return@runCatching if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.failure(error = ResponseError.getError(response = response))
            }
        
        }.getOrElse { Utils.logAndReturn(it) }
    
    
    override suspend fun getFeedFollowListens(
        username: String?,
        maxTs: Int?,
        minTs: Int?,
        count: Int
    ): Resource<FeedData> =
        runCatching {
            if (username.isNullOrEmpty()) return@runCatching Resource.failure(error = ResponseError.AUTH_HEADER_NOT_FOUND)
            
            val response = service.getFeedFollowListens(
                username = username,
                maxTs = maxTs,
                minTs = minTs,
                count = count
            )
        
            return@runCatching if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.failure(error = ResponseError.getError(response = response))
            }
        
        }.getOrElse { Utils.logAndReturn(it) }
    
    
    override suspend fun getFeedSimilarListens(
        username: String?,
        maxTs: Int?,
        minTs: Int?,
        count: Int
    ): Resource<FeedData> =
        runCatching {
            if (username.isNullOrEmpty()) return@runCatching Resource.failure(error = ResponseError.AUTH_HEADER_NOT_FOUND)
            
            val response = service.getFeedSimilarListens(
                username = username,
                maxTs = maxTs,
                minTs = minTs,
                count = count
            )
        
            return@runCatching if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.failure(error = ResponseError.getError(response = response))
            }
        
        }.getOrElse { Utils.logAndReturn(it) }
    
    
    override suspend fun deleteEvent(username: String?, data: FeedEventDeletionData) : Resource<SocialResponse> =
        runCatching {
            if (username.isNullOrEmpty()) return@runCatching Resource.failure(error = ResponseError.AUTH_HEADER_NOT_FOUND)
            
            val response = service.deleteEvent(
                username = username,
                body = data
            )
        
            return@runCatching if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.failure(error = ResponseError.getError(response = response))
            }
        
        }.getOrElse { Utils.logAndReturn(it) }
    
    
    override suspend fun hideEvent(username: String?, data: FeedEventVisibilityData) : Resource<SocialResponse> =
        runCatching {
            if (username.isNullOrEmpty()) return@runCatching Resource.failure(error = ResponseError.AUTH_HEADER_NOT_FOUND)
            
            val response = service.hideEvent(
                username = username,
                body = data
            )
        
            return@runCatching if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.failure(error = ResponseError.getError(response = response))
            }
        
        }.getOrElse { Utils.logAndReturn(it) }
    
    
    override suspend fun unhideEvent(username: String?, data: FeedEventVisibilityData) : Resource<SocialResponse> =
        runCatching {
            if (username.isNullOrEmpty()) return@runCatching Resource.failure(error = ResponseError.AUTH_HEADER_NOT_FOUND)
            
            val response = service.unhideEvent(
                username = username,
                body = data
            )
        
            return@runCatching if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.failure(error = ResponseError.getError(response = response))
            }
        
        }.getOrElse { Utils.logAndReturn(it) }
    
    
}