package org.listenbrainz.android.data.sources.api

import okhttp3.ResponseBody
import org.listenbrainz.android.data.sources.api.entities.ListenSubmitBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ListenSubmitService {
    @POST("https://api.listenbrainz.org/1/submit-listens")
    fun submitListen(@Header("Authorization") token: String?,
                     @Body body: ListenSubmitBody?): Call<ResponseBody?>?
}