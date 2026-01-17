package org.listenbrainz.android.service

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import org.listenbrainz.android.model.artist.CBReview

interface CBService {
    @GET("ws/1/review/")
    suspend fun getArtistReviews(
        @Query("entity_id") artistMbid: String?,
        @Query("entity_type") entityType: String? = "artist",
        @Query("limit") limit: Int = 5
    ): CBReview
}