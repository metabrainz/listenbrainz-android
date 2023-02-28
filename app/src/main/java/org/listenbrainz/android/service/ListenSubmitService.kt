package org.listenbrainz.android.service

import okhttp3.ResponseBody
import org.listenbrainz.android.model.ListenSubmitBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ListenSubmitService {
    @POST("https://api.listenbrainz.org/1/submit-listens")
    fun submitListen(@Header("Authorization") token: String?,
                     @Body body: ListenSubmitBody?): Call<ResponseBody?>?
}