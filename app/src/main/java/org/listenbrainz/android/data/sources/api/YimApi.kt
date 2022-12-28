package org.listenbrainz.android.data.sources.api

import org.listenbrainz.android.data.sources.api.entities.yimdata.YimData
import retrofit2.http.GET
import retrofit2.http.Path
import javax.inject.Singleton

@Singleton
interface YimApi {
    
    // https://api.listenbrainz.org/1/stats/user/(user_name)/year-in-music
    @GET("1/stats/user/{user_name}/year-in-music")
    suspend fun getYimData(@Path("user_name") username: String): YimData
    
}

