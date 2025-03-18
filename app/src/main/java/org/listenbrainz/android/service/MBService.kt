package org.listenbrainz.android.service

import org.listenbrainz.android.model.album.AlbumInfo
import org.listenbrainz.android.model.artist.ArtistWikiExtract
import org.listenbrainz.android.model.recordingSearch.RecordingSearchPayload
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MBService {
    @GET("artist/{artist_mbid}/wikipedia-extract")
    suspend fun getArtistWikiExtract(@Path("artist_mbid") artistMbid: String?): Response<ArtistWikiExtract?>

    @GET("ws/2/release-group/{album_id}")
    suspend fun getAlbumInfo(@Path("album_id") albumMbid: String?, @Query("fmt") format: String = "json")
    : Response<AlbumInfo?>

    @GET("ws/2/recording/")
    suspend fun searchRecording(@Query("query") query: String): Response<RecordingSearchPayload?>
}