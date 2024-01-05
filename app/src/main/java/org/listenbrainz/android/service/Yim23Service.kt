package org.listenbrainz.android.service

import dagger.Provides
import org.listenbrainz.android.model.yimdata.Yim23Payload
import org.listenbrainz.android.model.yimdata.YimPayload
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import javax.inject.Singleton

// TODO: TO BE REMOVED WHEN YIM GOES LIVE

@Singleton
interface Yim23Service {


    @GET("stats/user/{user_name}/year-in-music/2023")
    suspend fun getYimData(@Path("user_name") username: String): Response<Yim23Payload>

}

