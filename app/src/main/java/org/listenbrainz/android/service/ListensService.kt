package org.listenbrainz.android.service

import okhttp3.ResponseBody
import org.listenbrainz.android.model.CoverArt
import org.listenbrainz.android.model.ListenBrainzExternalServices
import org.listenbrainz.android.model.ListenSubmitBody
import org.listenbrainz.android.model.Listens
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ListensService {
    @GET("1/user/{user_name}/listens")
    suspend fun getUserListens(@Path("user_name") user_name: String, @Query("count") count: Int): Listens

    @GET("http://coverartarchive.org/release/{MBID}")
    suspend fun getCoverArt(@Path("MBID") MBID: String): CoverArt

    @POST("1/submit-listens")
    fun submitListen(@Header("Authorization") token: String?,
                     @Body body: ListenSubmitBody?): Call<ResponseBody?>?

    @GET("1/user/{user_name}/services")
    fun getServicesLinkedToAccount(
        @Header("Authorization") token: String?,
        @Path("user_name") user_name: String,
    ): Call<ListenBrainzExternalServices>
}