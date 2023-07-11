package org.listenbrainz.android.service

import org.listenbrainz.android.di.AuthHeader
import org.listenbrainz.android.model.SearchResult
import org.listenbrainz.android.model.SimilarUserData
import org.listenbrainz.android.model.SocialData
import org.listenbrainz.android.model.SocialResponse
import org.listenbrainz.android.util.Constants.Headers.AUTHORIZATION
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import javax.inject.Singleton

@Singleton
interface SocialService {
    
    @GET("user/{user_name}/followers")
    suspend fun getFollowersData(@Path("user_name") username: String): Response<SocialData>
    
    @GET("user/{user_name}/following")
    suspend fun getFollowingData(@Path("user_name") username: String): Response<SocialData>
    
    @POST("user/{user_name}/unfollow")
    suspend fun unfollowUser(
        @Path("user_name") username: String,
        @AuthHeader @Header(AUTHORIZATION) authHeader: String
    ): Response<SocialResponse>
    
    @POST("user/{user_name}/follow")
    suspend fun followUser(
        @Path("user_name") username: String,
        @AuthHeader @Header(AUTHORIZATION) authHeader: String
    ): Response<SocialResponse>
    
    @GET("user/{user_name}/similar-users")
    suspend fun getSimilarUsersData(@Path("user_name") username: String): Response<SimilarUserData>
    
    @GET("search/users")
    suspend fun searchUser(@Query("search_term") username: String): Response<SearchResult>
    
}