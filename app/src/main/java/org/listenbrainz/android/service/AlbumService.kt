package org.listenbrainz.android.service

import org.listenbrainz.android.model.album.Album
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Path

interface AlbumService {
    @POST("album/{album_mbid}/")
    suspend fun getAlbumData(@Path("album_mbid") albumMbid: String?): Response<Album?>
}