package org.listenbrainz.android.repository.playlists

import jakarta.inject.Inject
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.playlist.AddCopyPlaylistResponse
import org.listenbrainz.android.model.playlist.DeleteTracks
import org.listenbrainz.android.model.playlist.EditPlaylistResponse
import org.listenbrainz.android.model.playlist.MoveTrack
import org.listenbrainz.android.model.playlist.PlaylistData
import org.listenbrainz.android.model.playlist.PlaylistPayload
import org.listenbrainz.android.model.playlist.PlaylistTrack
import org.listenbrainz.android.model.recordingSearch.RecordingSearchPayload
import org.listenbrainz.android.service.MBService
import org.listenbrainz.android.service.PlaylistService
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils.parseResponse
import retrofit2.awaitResponse

class PlaylistDataRepositoryImpl @Inject constructor(
    private val playlistService: PlaylistService,
    private val mbService: MBService
) : PlaylistDataRepository {

    override suspend fun fetchPlaylist(playlistMbid: String?): Resource<PlaylistPayload?> =
        parseResponse {
            if (playlistMbid.isNullOrEmpty()) return ResponseError.BAD_REQUEST.asResource()
            playlistService.getPlaylist(playlistMbid)
        }

    override suspend fun copyPlaylist(playlistMbid: String?): Resource<AddCopyPlaylistResponse?> =
        parseResponse {
            if (playlistMbid.isNullOrEmpty()) return ResponseError.BAD_REQUEST.asResource()
            playlistService.copyPlaylist(playlistMbid)
        }

    override suspend fun deletePlaylist(playlistMbid: String?): Resource<Unit> =
        parseResponse {
            if (playlistMbid.isNullOrEmpty()) return ResponseError.BAD_REQUEST.asResource()
            playlistService.deletePlaylist(playlistMbid)
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

    override suspend fun addPlaylist(playlistPayload: PlaylistPayload): Resource<AddCopyPlaylistResponse?> {
        return parseResponse {
            if (playlistPayload.playlist.title.isNullOrEmpty())
                return ResponseError.BAD_REQUEST.asResource()
            playlistService.createPlaylist(playlistPayload)
        }
    }

    override suspend fun editPlaylist(
        playlistPayload: PlaylistPayload,
        playlistMbid: String?
    ): Resource<EditPlaylistResponse?> {
        return parseResponse {
            if (playlistMbid.isNullOrEmpty())
                return ResponseError.BAD_REQUEST.asResource()
            playlistService.editPlaylist(playlistPayload, playlistMbid)
        }
    }

    override suspend fun searchRecording(searchQuery: String): Resource<RecordingSearchPayload?> {
        return parseResponse {
            if (searchQuery.isEmpty())
                return ResponseError.BAD_REQUEST.asResource()
            mbService.searchRecording(searchQuery)
        }
    }

    override suspend fun moveTrack(
        playlistMbid: String?,
        moveTrack: MoveTrack
    ): Resource<EditPlaylistResponse?> = parseResponse {
        if (playlistMbid.isNullOrEmpty())
            return ResponseError.BAD_REQUEST.asResource()
        playlistService.moveTrack(playlistMbid, moveTrack)
    }

    override suspend fun addTracks(playlistMbid: String?, playlistTracks: List<PlaylistTrack>): Resource<EditPlaylistResponse?> = parseResponse {
        if (playlistTracks.isEmpty() || playlistMbid.isNullOrEmpty())
            return ResponseError.BAD_REQUEST.asResource()
        playlistService.addTracks(playlistMbid,PlaylistPayload(PlaylistData(track = playlistTracks)))
    }

    override suspend fun deleteTracks(playlistMbid: String?, deleteTracks: DeleteTracks): Resource<EditPlaylistResponse?> = parseResponse {
        if(playlistMbid.isNullOrEmpty())
            return ResponseError.BAD_REQUEST.asResource()
        playlistService.deleteTracks(playlistMbid,deleteTracks)
    }
}