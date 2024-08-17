package org.listenbrainz.android.service

import org.listenbrainz.android.model.artist.ArtistReview
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CBService {
    @GET("ws/1/review/?limit=5&entity_id={artist_mbid}")
    suspend fun getArtistReviews(@Path("artist_mbid") artistMbid: String?, @Query("entity_type") entityType: String? = "artist"): Response<ArtistReview?>
}