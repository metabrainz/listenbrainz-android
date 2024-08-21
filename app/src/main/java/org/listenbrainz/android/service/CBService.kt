package org.listenbrainz.android.service

import org.listenbrainz.android.model.artist.CBReview
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CBService {
    @GET("ws/1/review/")
    suspend fun getArtistReviews(@Query("entity_id") artistMbid: String?, @Query("entity_type") entityType: String? = "artist", @Query("limit") limit: Int = 5): Response<CBReview?>
}