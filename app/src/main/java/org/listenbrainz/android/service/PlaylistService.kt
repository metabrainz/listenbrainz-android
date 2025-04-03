package org.listenbrainz.android.service

import okhttp3.ResponseBody
import org.listenbrainz.android.model.playlist.AddCopyPlaylistResponse
import org.listenbrainz.android.model.playlist.DeleteTracks
import org.listenbrainz.android.model.playlist.EditPlaylistResponse
import org.listenbrainz.android.model.playlist.MoveTrack
import org.listenbrainz.android.model.playlist.PlaylistPayload
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PlaylistService {

    @GET("playlist/{playlist_mbid}")
    suspend fun getPlaylist(@Path("playlist_mbid") playlistMbid: String): Response<PlaylistPayload?>

    @POST("playlist/{playlist_mbid}/copy")
    suspend fun copyPlaylist(@Path("playlist_mbid") playlistMbid: String): Response<AddCopyPlaylistResponse?>

    @POST("art/playlist/{playlist_mbid}/{dimension}/{layout}")
    fun getPlaylistCoverArt(
        @Path("playlist_mbid") playlistMbid: String,
        @Path("dimension") dimension: Int,
        @Path("layout") layout: Int
    ): Call<ResponseBody>

    @POST("playlist/{playlist_mbid}/delete")
    suspend fun deletePlaylist(@Path("playlist_mbid") playlistMbid: String): Response<Unit>

    @POST("playlist/create")
    suspend fun createPlaylist(@Body playlistPayload: PlaylistPayload): Response<AddCopyPlaylistResponse?>

    @POST("playlist/edit/{playlist_mbid}")
    suspend fun editPlaylist(
        @Body playlistPayload: PlaylistPayload,
        @Path("playlist_mbid") playlistMbid: String
    ): Response<EditPlaylistResponse?>

    @POST("playlist/{playlist_mbid}/item/move")
    suspend fun moveTrack(
        @Path("playlist_mbid") playlistMbid: String,
        @Body moveTrack: MoveTrack
    ): Response<EditPlaylistResponse?>

    @POST("playlist/{playlist_mbid}/item/add")
    suspend fun addTracks(
        @Path("playlist_mbid") playlistMbid: String,
        @Body playlistPayload: PlaylistPayload
    ): Response<EditPlaylistResponse?>

    @POST("playlist/{playlist_mbid}/item/delete")
    suspend fun deleteTracks(
        @Path("playlist_mbid") playlistMbid: String,
        @Body deleteTracks: DeleteTracks
    ): Response<EditPlaylistResponse?>
}