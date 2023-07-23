package org.listenbrainz.android.service

import org.listenbrainz.android.model.FeedData
import org.listenbrainz.android.model.SocialResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FeedService {

    @GET("/user/{user_name}/feed/events")
    suspend fun getFeedEvents(
        @Path("user_name") userName: String,
        @Query("count") count: Int = 25,
        @Query("max_ts") maxTs: Int? = null,
        @Query("min_ts") minTs: Int? = null
    ) : Response<FeedData>
    
    @GET("/user/{user_name}/feed/events/listens/following")
    suspend fun getFeedFollowListens(
        @Path("user_name") userName: String,
        @Query("count") count: Int = 40,
        @Query("max_ts") maxTs: Int? = null,
        @Query("min_ts") minTs: Int? = null
    ) : Response<FeedData>
    
    @GET("/user/{user_name}/feed/events/listens/similar")
    suspend fun getFeedSimilarListens(
        @Path("user_name") userName: String,
        @Query("count") count: Int = 40,
        @Query("max_ts") maxTs: Int? = null,
        @Query("min_ts") minTs: Int? = null
    ) : Response<FeedData>
    
    @POST("/user/{user_name}/feed/events/delete")
    suspend fun deleteEvent() : Response<SocialResponse>
    
    @POST("/user/{user_name}/feed/events/hide")
    suspend fun hideEvent() : Response<SocialResponse>
    
    @POST("/user/{user_name}/feed/events/unhide")
    suspend fun unhideEvent() : Response<SocialResponse>
    
}