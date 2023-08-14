package org.listenbrainz.android.repository.social

import org.listenbrainz.android.model.FeedEvent
import org.listenbrainz.android.model.RecommendationData
import org.listenbrainz.android.model.Review
import org.listenbrainz.android.model.SearchResult
import org.listenbrainz.android.model.SimilarUserData
import org.listenbrainz.android.model.SocialData
import org.listenbrainz.android.model.SocialResponse
import org.listenbrainz.android.util.Resource

interface SocialRepository {
    
    suspend fun getFollowers(username: String) : Resource<SocialData>
    
    suspend fun getFollowing(username: String) : Resource<SocialData>
    
    suspend fun followUser(username: String) : Resource<SocialResponse>
    
    suspend fun unfollowUser(username: String) : Resource<SocialResponse>
    
    suspend fun getSimilarUsers(username: String) : Resource<SimilarUserData>
    
    suspend fun searchUser(username: String) : Resource<SearchResult>
    
    suspend fun postPersonalRecommendation(username: String, data: RecommendationData): Resource<FeedEvent>
    
    suspend fun postRecommendationToAll(username: String, data: RecommendationData): Resource<FeedEvent>
    
    suspend fun postReview(username: String, data: Review): Resource<FeedEvent>
    
    suspend fun deletePin(id: Int): Resource<SocialResponse>
    
}