package org.listenbrainz.android.service

import okhttp3.ResponseBody
import org.listenbrainz.android.model.ListenBrainzExternalServices
import org.listenbrainz.android.model.ListenSubmitBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ListenBrainzService {
    @POST("1/submit-listens")
    fun submitListen(@Header("Authorization") token: String?,
                     @Body body: ListenSubmitBody?): Call<ResponseBody?>?

    @GET("1/user/{user_name}/services")
    fun getServicesLinkedToAccount(
        @Header("Authorization") token: String?,
        @Path("user_name") user_name: String,
    ): Call<ListenBrainzExternalServices>?

}