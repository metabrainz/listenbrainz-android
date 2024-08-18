package org.listenbrainz.android.service

import org.listenbrainz.android.model.artist.ArtistPayload
import retrofit2.Response
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface ArtistService {
    @Headers("Accept: application/json")
    @POST("artist/{artist_mbid}")
    suspend fun getArtistData(@Path("artist_mbid") artistMbid: String?): Response<ArtistPayload?>
}