package org.listenbrainz.android.service

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import io.ktor.client.statement.HttpResponse
import org.listenbrainz.android.model.playlist.AddCopyPlaylistResponse
import org.listenbrainz.android.model.playlist.DeleteTracks
import org.listenbrainz.android.model.playlist.EditPlaylistResponse
import org.listenbrainz.android.model.playlist.MoveTrack
import org.listenbrainz.android.model.playlist.PlaylistPayload

interface PlaylistService {

    @GET("playlist/{playlist_mbid}")
    suspend fun getPlaylist(@Path("playlist_mbid") playlistMbid: String): PlaylistPayload

    @POST("playlist/{playlist_mbid}/copy")
    suspend fun copyPlaylist(@Path("playlist_mbid") playlistMbid: String): AddCopyPlaylistResponse

    // Returns SVG data, keep as HttpResponse for raw body access
    @POST("art/playlist/{playlist_mbid}/{dimension}/{layout}")
    suspend fun getPlaylistCoverArt(
        @Path("playlist_mbid") playlistMbid: String,
        @Path("dimension") dimension: Int,
        @Path("layout") layout: Int
    ): HttpResponse

    @POST("playlist/{playlist_mbid}/delete")
    suspend fun deletePlaylist(@Path("playlist_mbid") playlistMbid: String): Unit

    @POST("playlist/create")
    suspend fun createPlaylist(@Body playlistPayload: PlaylistPayload): AddCopyPlaylistResponse

    @POST("playlist/edit/{playlist_mbid}")
    suspend fun editPlaylist(
        @Body playlistPayload: PlaylistPayload,
        @Path("playlist_mbid") playlistMbid: String
    ): EditPlaylistResponse

    @POST("playlist/{playlist_mbid}/item/move")
    suspend fun moveTrack(
        @Path("playlist_mbid") playlistMbid: String,
        @Body moveTrack: MoveTrack
    ): EditPlaylistResponse

    @POST("playlist/{playlist_mbid}/item/add")
    suspend fun addTracks(
        @Path("playlist_mbid") playlistMbid: String,
        @Body playlistPayload: PlaylistPayload
    ): EditPlaylistResponse

    @POST("playlist/{playlist_mbid}/item/delete")
    suspend fun deleteTracks(
        @Path("playlist_mbid") playlistMbid: String,
        @Body deleteTracks: DeleteTracks
    ): EditPlaylistResponse
}