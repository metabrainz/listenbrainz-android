package org.listenbrainz.android.repository.playlists

import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.listenbrainz.android.di.IoDispatcher
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.playlist.AddCopyPlaylistResponse
import org.listenbrainz.android.model.playlist.DeleteTracks
import org.listenbrainz.android.model.playlist.EditPlaylistResponse
import org.listenbrainz.android.model.playlist.MoveTrack
import org.listenbrainz.android.model.playlist.PlaylistData
import org.listenbrainz.android.model.playlist.PlaylistPayload
import org.listenbrainz.android.model.playlist.PlaylistTrack
import org.listenbrainz.android.model.recordingSearch.RecordingSearchPayload
import org.listenbrainz.android.model.userPlaylist.UserPlaylistPayload
import org.listenbrainz.android.service.MBService
import org.listenbrainz.android.service.PlaylistService
import org.listenbrainz.android.service.UserService
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils.parseResponse

class PlaylistDataRepositoryImpl @Inject constructor(
    private val playlistService: PlaylistService,
    private val userService: UserService,
    private val mbService: MBService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PlaylistDataRepository {

    override suspend fun fetchPlaylist(playlistMbid: String?): Resource<PlaylistPayload?> =
        withContext(ioDispatcher) {
            parseResponse {
                failIf(playlistMbid.isNullOrEmpty()) { ResponseError.BadRequest() }
                playlistService.getPlaylist(playlistMbid!!)
            }
        }


    override suspend fun copyPlaylist(playlistMbid: String?): Resource<AddCopyPlaylistResponse?> =
        withContext(ioDispatcher) {
            parseResponse {
                failIf(playlistMbid.isNullOrEmpty()) { ResponseError.BadRequest() }
                playlistService.copyPlaylist(playlistMbid!!)
            }
        }

    override suspend fun deletePlaylist(playlistMbid: String?): Resource<Unit> =
        withContext(ioDispatcher) {
            parseResponse {
                failIf(playlistMbid.isNullOrEmpty()) { ResponseError.BadRequest() }
                playlistService.deletePlaylist(playlistMbid!!)
            }
        }

    override suspend fun getPlaylistCoverArt(
        playlistMBID: String,
        layout: Int
    ): Resource<String?> = withContext(ioDispatcher) {
        // First try if cover art of dimension 3 is available, then 2 and then 1
        val dimensions = listOf(3, 2, 1)
        return@withContext try {
            for (dimension in dimensions) {
                val response = playlistService.getPlaylistCoverArt(playlistMBID, dimension, layout)
                if (response.status.isSuccess()) {
                    val svgData = response.bodyAsText()
                    return@withContext Resource(Resource.Status.SUCCESS, svgData, null)
                }
            }
            Resource(Resource.Status.FAILED, null)
        } catch (e: Exception) {
            Resource(Resource.Status.FAILED, null)
        }
    }


    override suspend fun addPlaylist(playlistPayload: PlaylistPayload): Resource<AddCopyPlaylistResponse?> =
        withContext(ioDispatcher) {
            parseResponse {
                failIf(playlistPayload.playlist.title.isNullOrEmpty()) { ResponseError.BadRequest() }
                playlistService.createPlaylist(playlistPayload)
            }
        }


    override suspend fun editPlaylist(
        playlistPayload: PlaylistPayload,
        playlistMbid: String?
    ): Resource<EditPlaylistResponse?> =
        withContext(ioDispatcher) {
            parseResponse {
                failIf(playlistMbid.isNullOrEmpty()) { ResponseError.BadRequest() }
                playlistService.editPlaylist(playlistPayload, playlistMbid!!)
            }
        }

    override suspend fun searchRecording(
        searchQuery: String?,
        mbid: String?
    ): Resource<RecordingSearchPayload?> =
        withContext(ioDispatcher) {
            parseResponse {
                if (!mbid.isNullOrEmpty())
                    mbService.searchRecording("rid:$mbid")
                else if (!searchQuery.isNullOrEmpty())
                    mbService.searchRecording(searchQuery)
                else
                    throw org.listenbrainz.android.util.Utils.PreEmptiveBadRequestException(ResponseError.BadRequest())
            }
        }


    override suspend fun moveTrack(
        playlistMbid: String?,
        moveTrack: MoveTrack
    ): Resource<EditPlaylistResponse?> =
        withContext(ioDispatcher) {
            parseResponse {
                failIf(playlistMbid.isNullOrEmpty()) { ResponseError.BadRequest() }
                playlistService.moveTrack(playlistMbid!!, moveTrack)
            }
        }

    override suspend fun addTracks(
        playlistMbid: String?,
        playlistTracks: List<PlaylistTrack>
    ): Resource<EditPlaylistResponse?> =
        withContext(ioDispatcher) {
            parseResponse {
                failIf(playlistTracks.isEmpty() || playlistMbid.isNullOrEmpty()) { ResponseError.BadRequest() }
                playlistService.addTracks(
                    playlistMbid!!,
                    PlaylistPayload(PlaylistData(track = playlistTracks))
                )
            }
        }

    override suspend fun deleteTracks(
        playlistMbid: String?,
        deleteTracks: DeleteTracks
    ): Resource<EditPlaylistResponse?> =
        withContext(ioDispatcher) {
            parseResponse {
                failIf(playlistMbid.isNullOrEmpty()) { ResponseError.BadRequest() }
                playlistService.deleteTracks(playlistMbid!!, deleteTracks)
            }
        }

    override suspend fun getUserPlaylists(username: String?, offset: Int, count: Int): Resource<UserPlaylistPayload> {
        return parseResponse {
            failIf(username.isNullOrEmpty()) { ResponseError.BadRequest() }
            userService.getUserPlaylists(username, offset, count)
        }
    }

    override suspend fun getUserCollabPlaylists(username: String?, offset: Int, count: Int): Resource<UserPlaylistPayload> {
        return parseResponse {
            failIf(username.isNullOrEmpty()) { ResponseError.BadRequest() }
            userService.getUserCollabPlaylists(username, offset, count)
        }
    }
}