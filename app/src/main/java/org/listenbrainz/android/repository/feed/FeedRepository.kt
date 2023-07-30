package org.listenbrainz.android.repository.feed

import org.listenbrainz.android.model.FeedData
import org.listenbrainz.android.model.FeedEventDeletionData
import org.listenbrainz.android.model.FeedEventVisibilityData
import org.listenbrainz.android.model.SocialResponse
import org.listenbrainz.android.util.Resource

interface FeedRepository {
    
    suspend fun getFeedEvents(username: String, maxTs: Int, minTs: Int) : Resource<FeedData>
    
    suspend fun getFeedFollowListens(username: String, maxTs: Int, minTs: Int) : Resource<FeedData>
    
    suspend fun getFeedSimilarListens(username: String, maxTs: Int, minTs: Int) : Resource<FeedData>
    
    suspend fun deleteEvent(username: String, data: FeedEventDeletionData) : Resource<SocialResponse>
    
    suspend fun hideEvent(username: String, data: FeedEventVisibilityData) : Resource<SocialResponse>
    
    suspend fun unhideEvent(username: String, data: FeedEventVisibilityData) : Resource<SocialResponse>
    
}