package org.listenbrainz.android.service

import org.listenbrainz.android.model.YouTubeSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {
    @GET("youtube/v3/search")
    suspend fun searchVideos(
        @Query("part") part: String,
        @Query("q") query: String,
        @Query("type") type: String,
        @Query("videoCategoryId") videoCategoryId: String,
        @Query("key") apiKey: String
    ): Response<YouTubeSearchResponse>
}