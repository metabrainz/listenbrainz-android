package org.listenbrainz.android.service

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import org.listenbrainz.android.model.CoverArt
import org.listenbrainz.android.model.ListenBrainzExternalServices
import org.listenbrainz.android.model.ListenSubmitBody
import org.listenbrainz.android.model.Listens
import org.listenbrainz.android.model.PostResponse
import org.listenbrainz.android.model.TokenValidation

interface ListensService {
    
    @GET("user/{user_name}/listens")
    suspend fun getUserListens(
        @Path("user_name") username: String,
        @Query("count") count: Int,
        @Query("max_ts") maxTs: Long? = null,
        @Query("min_ts") minTs: Long? = null
    ): Listens
    
    @GET("http://coverartarchive.org/release/{MBID}")
    suspend fun getCoverArt(@Path("MBID") mbid: String): CoverArt
    
    @GET("validate-token")
    suspend fun checkTokenValidity(
        @Header("Authorization") authHeader: String
    ): TokenValidation
    
    @POST("submit-listens")
    suspend fun submitListen(@Body body: ListenSubmitBody?): PostResponse
    
    @GET("user/{user_name}/services")
    suspend fun getServicesLinkedToAccount(
        @Path("user_name") username: String,
    ): ListenBrainzExternalServices

    @GET("user/{user_name}/playing-now")
    suspend fun getNowPlaying(
        @Path("user_name") username: String
    ): Listens
    
}