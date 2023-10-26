package org.listenbrainz.android.service

import org.listenbrainz.android.model.PinData
import org.listenbrainz.android.model.PinnedRecording
import org.listenbrainz.android.model.RecommendationData
import org.listenbrainz.android.model.Review
import org.listenbrainz.android.model.SearchResult
import org.listenbrainz.android.model.SimilarUserData
import org.listenbrainz.android.model.SocialData
import org.listenbrainz.android.model.SocialResponse
import org.listenbrainz.android.model.feed.FeedEvent
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SocialService {
    
    @GET("user/{user_name}/followers")
    suspend fun getFollowersData(@Path("user_name") username: String): Response<SocialData>
    
    @GET("user/{user_name}/following")
    suspend fun getFollowingData(@Path("user_name") username: String): Response<SocialData>
    
    @POST("user/{user_name}/unfollow")
    suspend fun unfollowUser(@Path("user_name") username: String): Response<SocialResponse>
    
    @POST("user/{user_name}/follow")
    suspend fun followUser(@Path("user_name") username: String): Response<SocialResponse>
    
    @GET("user/{user_name}/similar-users")
    suspend fun getSimilarUsersData(@Path("user_name") username: String): Response<SimilarUserData>
    
    @GET("search/users")
    suspend fun searchUser(@Query("search_term") username: String): Response<SearchResult>
    
    @POST("user/{user_name}/timeline-event/create/recommend-personal")
    suspend fun postPersonalRecommendation(
        @Path("user_name") username: String,
        @Body data: RecommendationData
    ) : Response<FeedEvent>
    
    @POST("user/{user_name}/timeline-event/create/recording")
    suspend fun postRecommendationToAll(
        @Path("user_name") username: String,
        @Body data: RecommendationData
    ) : Response<FeedEvent>
    
    @POST("user/{user_name}/timeline-event/create/review")
    suspend fun postReview(
        @Path("user_name") username: String,
        @Body data: Review
    ) : Response<FeedEvent>
    
    @POST("pin")
    suspend fun postPin(
        @Body data: PinnedRecording
    ) : Response<PinData>
    
    @POST("pin/delete/{id}")
    suspend fun deletePin(
        @Path("id") id: Int
    ) : Response<SocialResponse>
    
}