package org.listenbrainz.android.service

import org.listenbrainz.android.model.CoverArt
import org.listenbrainz.android.model.Listens
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ListensService {
    @GET("1/user/{user_name}/listens")
    suspend fun getUserListens(@Path("user_name") user_name: String, @Query("count") count: Int): Listens

    @GET("http://coverartarchive.org/release/{MBID}")
    suspend fun getCoverArt(@Path("MBID") MBID: String): CoverArt
}