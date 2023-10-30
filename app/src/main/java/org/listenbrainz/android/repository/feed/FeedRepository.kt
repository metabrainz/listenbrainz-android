package org.listenbrainz.android.repository.feed

import org.listenbrainz.android.model.SocialResponse
import org.listenbrainz.android.model.feed.FeedData
import org.listenbrainz.android.model.feed.FeedEventDeletionData
import org.listenbrainz.android.model.feed.FeedEventVisibilityData
import org.listenbrainz.android.util.Resource

interface FeedRepository {
    
    suspend fun getFeedEvents(username: String?, maxTs: Int? = null, minTs: Int? = null, count: Int = FeedEventCount) : Resource<FeedData>
    
    suspend fun getFeedFollowListens(username: String?, maxTs: Int? = null, minTs: Int? = null, count: Int = FeedListensCount) : Resource<FeedData>
    
    suspend fun getFeedSimilarListens(username: String?, maxTs: Int? = null, minTs: Int? = null, count: Int = FeedListensCount) : Resource<FeedData>
    
    suspend fun deleteEvent(username: String?, data: FeedEventDeletionData) : Resource<SocialResponse>
    
    suspend fun hideEvent(username: String?, data: FeedEventVisibilityData) : Resource<SocialResponse>
    
    suspend fun unhideEvent(username: String?, data: FeedEventVisibilityData) : Resource<SocialResponse>
    
    companion object {
        const val FeedEventCount = 25
        const val FeedListensCount = 40
    }
}