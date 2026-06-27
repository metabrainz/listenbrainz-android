package org.listenbrainz.shared.service

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import org.listenbrainz.shared.model.PinData
import org.listenbrainz.shared.model.PinnedRecording
import org.listenbrainz.shared.model.RecommendationData
import org.listenbrainz.shared.model.Review
import org.listenbrainz.shared.model.SearchResult
import org.listenbrainz.shared.model.SimilarUserData
import org.listenbrainz.shared.model.SocialData
import org.listenbrainz.shared.model.SocialResponse
import org.listenbrainz.shared.model.feed.FeedEvent

interface SocialService {

    @GET("user/{user_name}/followers")
    suspend fun getFollowersData(@Path("user_name") username: String): SocialData

    @GET("user/{user_name}/following")
    suspend fun getFollowingData(@Path("user_name") username: String): SocialData

    @POST("user/{user_name}/unfollow")
    suspend fun unfollowUser(@Path("user_name") username: String): SocialResponse

    @POST("user/{user_name}/follow")
    suspend fun followUser(@Path("user_name") username: String): SocialResponse

    @GET("user/{user_name}/similar-users")
    suspend fun getSimilarUsersData(@Path("user_name") username: String): SimilarUserData

    @GET("search/users")
    suspend fun searchUser(@Query("search_term") username: String): SearchResult

    @POST("user/{user_name}/timeline-event/create/recommend-personal")
    suspend fun postPersonalRecommendation(
        @Path("user_name") username: String,
        @Body data: RecommendationData
    ): FeedEvent

    @POST("user/{user_name}/timeline-event/create/recording")
    suspend fun postRecommendationToAll(
        @Path("user_name") username: String,
        @Body data: RecommendationData
    ): FeedEvent

    @POST("user/{user_name}/timeline-event/create/review")
    suspend fun postReview(
        @Path("user_name") username: String,
        @Body data: Review
    ): FeedEvent

    @POST("pin")
    suspend fun postPin(
        @Body data: PinnedRecording
    ): PinData

    @POST("pin/delete/{id}")
    suspend fun deletePin(
        @Path("id") id: Int
    ): SocialResponse

}