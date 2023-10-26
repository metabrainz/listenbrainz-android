package org.listenbrainz.android.service

import com.google.common.net.HttpHeaders.AUTHORIZATION
import org.listenbrainz.android.model.CoverArt
import org.listenbrainz.android.model.ListenBrainzExternalServices
import org.listenbrainz.android.model.ListenSubmitBody
import org.listenbrainz.android.model.Listens
import org.listenbrainz.android.model.PostResponse
import org.listenbrainz.android.model.TokenValidation
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ListensService {
    
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
    
}