package org.listenbrainz.android.service

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import org.listenbrainz.android.model.album.AlbumInfo
import org.listenbrainz.android.model.artist.ArtistWikiExtract
import org.listenbrainz.android.model.recordingSearch.RecordingSearchPayload

interface MBService {
    @GET("artist/{artist_mbid}/wikipedia-extract")
    suspend fun getArtistWikiExtract(@Path("artist_mbid") artistMbid: String): ArtistWikiExtract

    @GET("ws/2/release-group/{album_id}")
    suspend fun getAlbumInfo(@Path("album_id") albumMbid: String, @Query("fmt") format: String = "json"): AlbumInfo

    @GET("ws/2/recording/")
    suspend fun searchRecording(@Query("query") query: String): RecordingSearchPayload
}