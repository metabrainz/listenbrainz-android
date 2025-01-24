package org.listenbrainz.android.repository.playlists

import org.listenbrainz.android.model.playlist.PlaylistPayload
import org.listenbrainz.android.util.Resource

interface PlaylistDataRepository {

    suspend fun fetchPlaylist(playlistMbid: String?): Resource<PlaylistPayload?>
}