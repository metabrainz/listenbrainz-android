package org.listenbrainz.android.service

import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Path
import org.listenbrainz.android.model.artist.Artist

interface ArtistService {
    @POST("artist/{artist_mbid}")
    suspend fun getArtistData(@Path("artist_mbid") artistMbid: String?): Response<Artist?>
}