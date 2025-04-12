package org.listenbrainz.android.service

import org.listenbrainz.android.model.explore.HueSoundPayload
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ExploreService {

    @GET("explore/color/{hexCode}")
    suspend fun getReleasesFromColor(@Path("hexCode") colorHex: String): Response<HueSoundPayload>
}