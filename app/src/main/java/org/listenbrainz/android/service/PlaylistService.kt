package org.listenbrainz.android.service

import org.listenbrainz.android.model.playlist.CopyPlaylistResponse
import org.listenbrainz.android.model.playlist.PlaylistPayload
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PlaylistService {

    @GET("playlist/{playlist_mbid}")
    suspend fun getPlaylist(@Path("playlist_mbid") playlistMbid: String): Response<PlaylistPayload?>

    @POST("playlist/{playlist_mbid}/copy")
    suspend fun copyPlaylist(@Path("playlist_mbid") playlistMbid: String): Response<CopyPlaylistResponse?>
}