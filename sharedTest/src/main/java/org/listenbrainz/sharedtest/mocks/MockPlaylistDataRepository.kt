package org.listenbrainz.sharedtest.mocks

import org.listenbrainz.android.model.playlist.CopyPlaylistResponse
import org.listenbrainz.android.model.playlist.PlaylistPayload
import org.listenbrainz.android.repository.playlists.PlaylistDataRepository
import org.listenbrainz.android.util.Resource
import org.listenbrainz.sharedtest.testdata.PlaylistDataRepositoryTestData.playlistDetailsTestData


class MockPlaylistDataRepository : PlaylistDataRepository {
    override suspend fun fetchPlaylist(playlistMbid: String?): Resource<PlaylistPayload?> {
        return Resource(Resource.Status.SUCCESS, playlistDetailsTestData)
    }

    override suspend fun copyPlaylist(playlistMbid: String?): Resource<CopyPlaylistResponse?> {
        return Resource(
            Resource.Status.SUCCESS,
            CopyPlaylistResponse("new_playlist_mbid", "Playlist copied successfully")
        )
    }

}