package org.listenbrainz.sharedtest.mocks

import org.listenbrainz.android.model.playlist.AddCopyPlaylistResponse
import org.listenbrainz.android.model.playlist.PlaylistPayload
import org.listenbrainz.android.repository.playlists.PlaylistDataRepository
import org.listenbrainz.android.util.Resource
import org.listenbrainz.sharedtest.testdata.PlaylistDataRepositoryTestData.playlistDetailsTestData
import org.listenbrainz.sharedtest.testdata.PlaylistDataRepositoryTestData.samplePlaylistCoverArt


class MockPlaylistDataRepository : PlaylistDataRepository {
    override suspend fun fetchPlaylist(playlistMbid: String?): Resource<PlaylistPayload?> {
        return Resource(Resource.Status.SUCCESS, playlistDetailsTestData)
    }

    override suspend fun copyPlaylist(playlistMbid: String?): Resource<AddCopyPlaylistResponse?> {
        return Resource(
            Resource.Status.SUCCESS,
            AddCopyPlaylistResponse("new_playlist_mbid", "Playlist copied successfully")
        )
    }

    override suspend fun deletePlaylist(playlistMbid: String?): Resource<Unit> {
        return Resource(
            Resource.Status.SUCCESS,
            Unit
        )
    }

    override suspend fun getPlaylistCoverArt(
        playlistMBID: String,
        dimension: Int,
        layout: Int
    ): Resource<String?> {
        return Resource(
            Resource.Status.SUCCESS,
            samplePlaylistCoverArt
        )
    }

}