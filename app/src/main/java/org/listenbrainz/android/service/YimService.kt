package org.listenbrainz.android.service

import org.listenbrainz.android.model.yimdata.YimPayload
import retrofit2.http.GET
import retrofit2.http.Path
import javax.inject.Singleton

@Singleton
interface YimService {
    
    // https://api.listenbrainz.org/1/stats/user/(user_name)/year-in-music/2022
    @GET("stats/user/{user_name}/year-in-music/2022")
    suspend fun getYimData(@Path("user_name") username: String): YimPayload
    
}

