package org.listenbrainz.android.data.sources.api

import org.listenbrainz.android.data.sources.api.entities.CoverArt
import org.listenbrainz.android.data.sources.api.entities.listens.Listens
import retrofit2.http.GET
import retrofit2.http.Path

interface ListensService {
    @GET("1/user/{user_name}/listens")
    suspend fun getUserListens(@Path("user_name") user_name: String): Listens

    @GET("http://coverartarchive.org/release/{MBID}")
    suspend fun getCoverArt(@Path("MBID") MBID: String): CoverArt
}