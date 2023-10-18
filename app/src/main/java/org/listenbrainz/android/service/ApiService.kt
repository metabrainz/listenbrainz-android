package org.listenbrainz.android.service

import org.listenbrainz.android.model.CoverArt
import org.listenbrainz.android.model.ListenBrainzExternalServices
import org.listenbrainz.android.model.ListenSubmitBody
import org.listenbrainz.android.model.Listens
import org.listenbrainz.android.model.PinData
import org.listenbrainz.android.model.PinnedRecording
import org.listenbrainz.android.model.PostResponse
import org.listenbrainz.android.model.RecommendationData
import org.listenbrainz.android.model.Review
import org.listenbrainz.android.model.SearchResult
import org.listenbrainz.android.model.SimilarUserData
import org.listenbrainz.android.model.SocialData
import org.listenbrainz.android.model.SocialResponse
import org.listenbrainz.android.model.TokenValidation
import org.listenbrainz.android.model.feed.FeedData
import org.listenbrainz.android.model.feed.FeedEvent
import org.listenbrainz.android.model.feed.FeedEventDeletionData
import org.listenbrainz.android.model.feed.FeedEventVisibilityData
import org.listenbrainz.android.util.Constants.Headers.AUTHORIZATION
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    
    /** Listens Services **/
    
    @GET("user/{user_name}/listens")
    suspend fun getUserListens(
        @Path("user_name") username: String,
        @Query("count") count: Int
    ): Response<Listens>
    
    @GET("http://coverartarchive.org/release/{MBID}")
    suspend fun getCoverArt(@Path("MBID") mbid: String): Response<CoverArt>
    
    @GET("validate-token")
    suspend fun checkTokenValidity(
        @Header(AUTHORIZATION) authHeader: String
    ): Response<TokenValidation>
    
    @POST("submit-listens")
    suspend fun submitListen(@Body body: ListenSubmitBody?): Response<PostResponse>
    
    @GET("user/{user_name}/services")
    suspend fun getServicesLinkedToAccount(
        @Path("user_name") username: String,
    ): Response<ListenBrainzExternalServices>
    
    /** Social Services **/

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
    
    /** Feed Services **/

    @GET("user/{user_name}/feed/events")
    suspend fun getFeedEvents(
        @Path("user_name") username: String,
        @Query("count") count: Int = 25,
        @Query("max_ts") maxTs: Int? = null,
        @Query("min_ts") minTs: Int? = null
    ) : Response<FeedData>
    
    @GET("user/{user_name}/feed/events/listens/following")
    suspend fun getFeedFollowListens(
        @Path("user_name") username: String,
        @Query("count") count: Int = 40,
        @Query("max_ts") maxTs: Int? = null,
        @Query("min_ts") minTs: Int? = null
    ) : Response<FeedData>
    
    @GET("user/{user_name}/feed/events/listens/similar")
    suspend fun getFeedSimilarListens(
        @Path("user_name") username: String,
        @Query("count") count: Int = 40,
        @Query("max_ts") maxTs: Int? = null,
        @Query("min_ts") minTs: Int? = null
    ) : Response<FeedData>
    
    @POST("user/{user_name}/feed/events/delete")
    suspend fun deleteEvent(
        @Path("user_name") username: String,
        @Body body: FeedEventDeletionData
    ) : Response<SocialResponse>
    
    @POST("user/{user_name}/feed/events/hide")
    suspend fun hideEvent(
        @Path("user_name") username: String,
        @Body body: FeedEventVisibilityData
    ) : Response<SocialResponse>
    
    @POST("user/{user_name}/feed/events/unhide")
    suspend fun unhideEvent(
        @Path("user_name") username: String,
        @Body body: FeedEventVisibilityData
    ) : Response<SocialResponse>
    
}