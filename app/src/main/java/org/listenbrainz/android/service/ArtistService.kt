package org.listenbrainz.android.service

import org.listenbrainz.android.model.artist.PopularAlbums
import org.listenbrainz.android.model.artist.PopularTracks
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ArtistService {
    @GET("popularity/top-recordings-for-artist/{artist_mbid}")
    suspend fun getPopularTracksOfArtist(@Path("artist_mbid") artistMbid: String?): Response<PopularTracks?>

    @GET("popularity/top-release-groups-for-artist/{artist_mbid}")
    suspend fun getPopularAlbumsOfArtist(@Path("artist_mbid") artistMbid: String?): Response<PopularAlbums?>
}