package org.listenbrainz.android.repository.playlists

import jakarta.inject.Inject
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.playlist.CopyPlaylistResponse
import org.listenbrainz.android.model.playlist.PlaylistPayload
import org.listenbrainz.android.service.PlaylistService
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils.parseResponse

class PlaylistDataRepositoryImpl @Inject constructor(
    private val playlistService: PlaylistService
) : PlaylistDataRepository {

    override suspend fun fetchPlaylist(playlistMbid: String?): Resource<PlaylistPayload?> =
        parseResponse {
            if (playlistMbid.isNullOrEmpty()) return ResponseError.BAD_REQUEST.asResource()
            playlistService.getPlaylist(playlistMbid)
        }

    override suspend fun copyPlaylist(playlistMbid: String?): Resource<CopyPlaylistResponse?> = parseResponse {
        if (playlistMbid.isNullOrEmpty()) return ResponseError.BAD_REQUEST.asResource()
        playlistService.copyPlaylist(playlistMbid)
    }
}