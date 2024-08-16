package org.listenbrainz.android.service

import org.listenbrainz.android.model.artist.ArtistBio
import org.listenbrainz.android.model.artist.ArtistWikiExtract
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface MBService {
    @GET("ws/2/artist/{artist_mbid}?fmt=json")
    suspend fun getArtistBio(@Path("artist_mbid") artistMbid: String?): Response<ArtistBio?>

    @GET("artist/{artist_mbid}/wikipedia-extract")
    suspend fun getArtistWikiExtract(@Path("artist_mbid") artistMbid: String?): Response<ArtistWikiExtract?>

}