package org.listenbrainz.android.service

import okhttp3.ResponseBody
import org.listenbrainz.android.model.CoverArt
import org.listenbrainz.android.model.ListenBrainzExternalServices
import org.listenbrainz.android.model.ListenSubmitBody
import org.listenbrainz.android.model.Listens
import org.listenbrainz.android.model.TokenValidation
import org.listenbrainz.android.util.Constants.Headers.AUTHORIZATION
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ListensService {
    @GET("user/{user_name}/listens")
    suspend fun getUserListens(@Path("user_name") user_name: String, @Query("count") count: Int): Listens

    @GET("http://coverartarchive.org/release/{MBID}")
    suspend fun getCoverArt(@Path("MBID") MBID: String): CoverArt
    
    @GET("validate-token")
    suspend fun checkIfTokenIsValid(@Header(AUTHORIZATION) token: String?): TokenValidation

    @POST("submit-listens")
    fun submitListen(@Header(AUTHORIZATION) token: String?,
                     @Body body: ListenSubmitBody?): Call<ResponseBody?>?

    @GET("user/{user_name}/services")
    suspend fun getServicesLinkedToAccount(
        @Header(AUTHORIZATION) authHeader: String?,
        @Path("user_name") user_name: String,
    ): ListenBrainzExternalServices
}