package org.listenbrainz.android.service

import org.listenbrainz.android.model.Listens
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface UserService {
    @GET("user/{user_name}/listen-count")
    suspend fun getListenCount(@Path("user_name") username : String?) : Response<Listens?>
}