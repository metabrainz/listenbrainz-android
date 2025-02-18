package org.listenbrainz.android.repository.playlists

import jakarta.inject.Inject
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.playlist.CopyPlaylistResponse
import org.listenbrainz.android.model.playlist.PlaylistPayload
import org.listenbrainz.android.service.PlaylistService
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils.parseResponse
import retrofit2.awaitResponse

class PlaylistDataRepositoryImpl @Inject constructor(
    private val playlistService: PlaylistService
) : PlaylistDataRepository {

    override suspend fun fetchPlaylist(playlistMbid: String?): Resource<PlaylistPayload?> =
        parseResponse {
            if (playlistMbid.isNullOrEmpty()) return ResponseError.BAD_REQUEST.asResource()
            playlistService.getPlaylist(playlistMbid)
        }

    override suspend fun copyPlaylist(playlistMbid: String?): Resource<CopyPlaylistResponse?> =
        parseResponse {
            if (playlistMbid.isNullOrEmpty()) return ResponseError.BAD_REQUEST.asResource()
            playlistService.copyPlaylist(playlistMbid)
        }

    override suspend fun getPlaylistCoverArt(
        playlistMBID: String,
        dimension: Int,
        layout: Int
    ): Resource<String?> {
        return try {
            val response =
                playlistService.getPlaylistCoverArt(playlistMBID, dimension, layout).awaitResponse()
            if (response.isSuccessful) {
                val svgData = response.body()?.string()
                Resource(Resource.Status.SUCCESS, svgData, null)
            } else {
                Resource(Resource.Status.FAILED, null)
            }
        } catch (e: Exception) {
            Resource(Resource.Status.FAILED, null)
        }
    }

}