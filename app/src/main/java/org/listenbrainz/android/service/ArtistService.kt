package org.listenbrainz.android.service

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import org.listenbrainz.android.model.artist.ArtistPayload

interface ArtistService {
    @Headers("Accept: application/json")
    @POST("artist/{artist_mbid}")
    suspend fun getArtistData(@Path("artist_mbid") artistMbid: String): ArtistPayload?
}