package org.listenbrainz.android.service

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import org.listenbrainz.android.model.YouTubeSearchResponse

interface YouTubeApiService {
    @GET("youtube/v3/search")
    suspend fun searchVideos(
        @Query("part") part: String,
        @Query("q") query: String,
        @Query("type") type: String,
        @Query("videoCategoryId") videoCategoryId: String,
        @Query("key") apiKey: String
    ): YouTubeSearchResponse
}