package org.listenbrainz.android.repository.social

import org.listenbrainz.android.model.PinData
import org.listenbrainz.android.model.RecommendationData
import org.listenbrainz.android.model.Review
import org.listenbrainz.android.model.SearchResult
import org.listenbrainz.android.model.SimilarUserData
import org.listenbrainz.android.model.SocialData
import org.listenbrainz.android.model.SocialResponse
import org.listenbrainz.android.model.feed.FeedEvent
import org.listenbrainz.android.util.Resource
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

interface SocialRepository {
    
    suspend fun getFollowers(username: String?) : Resource<SocialData>
    
    suspend fun getFollowing(username: String) : Resource<SocialData>
    
    suspend fun followUser(username: String) : Resource<SocialResponse>
    
    suspend fun unfollowUser(username: String) : Resource<SocialResponse>
    
    suspend fun getSimilarUsers(username: String) : Resource<SimilarUserData>
    
    suspend fun searchUser(username: String) : Resource<SearchResult>
    
    suspend fun postPersonalRecommendation(username: String?, data: RecommendationData): Resource<FeedEvent>
    
    suspend fun postRecommendationToAll(username: String?, data: RecommendationData): Resource<FeedEvent>
    
    suspend fun postReview(username: String?, data: Review): Resource<FeedEvent>
    
    suspend fun pin(recordingMsid: String?, recordingMbid: String?, blurbContent: String?, pinnedUntil: Int = (getPinTimeMs() /1000).toInt()): Resource<PinData>
    
    suspend fun deletePin(id: Int): Resource<SocialResponse>
    
    companion object {
        fun getPinDateString(): String {
            val formatter = SimpleDateFormat("MMM dd, hh:mm aaa", Locale.getDefault())
        
            val calendar: Calendar = Calendar.getInstance(Locale.ENGLISH)
                .apply { timeInMillis = getPinTimeMs() }
        
            return formatter.format(calendar.time)
        }
    
        fun getPinTimeMs(): Long = System.currentTimeMillis() + 604800000
    }
}