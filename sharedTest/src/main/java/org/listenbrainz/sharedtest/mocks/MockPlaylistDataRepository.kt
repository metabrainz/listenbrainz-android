package org.listenbrainz.sharedtest.mocks

import org.listenbrainz.android.model.playlist.AddCopyPlaylistResponse
import org.listenbrainz.android.model.playlist.DeleteTracks
import org.listenbrainz.android.model.playlist.EditPlaylistResponse
import org.listenbrainz.android.model.playlist.MoveTrack
import org.listenbrainz.android.model.playlist.PlaylistPayload
import org.listenbrainz.android.model.playlist.PlaylistTrack
import org.listenbrainz.android.model.recordingSearch.RecordingSearchPayload
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

    override suspend fun getPlaylistCoverArt(playlistMBID: String, layout: Int): Resource<String?> {
        return Resource(
            Resource.Status.SUCCESS,
            samplePlaylistCoverArt
        )
    }

    override suspend fun addPlaylist(playlistPayload: PlaylistPayload): Resource<AddCopyPlaylistResponse?> {
        return Resource(
            Resource.Status.SUCCESS,
            AddCopyPlaylistResponse("new_playlist_mbid", "Playlist copied successfully")
        )
    }

    override suspend fun editPlaylist(
        playlistPayload: PlaylistPayload,
        playlistMbid: String?
    ): Resource<EditPlaylistResponse?> {
        return Resource(
            Resource.Status.SUCCESS,
            EditPlaylistResponse("Ok")
        )
    }

    override suspend fun searchRecording(
        searchQuery: String?,
        mbid: String?
    ): Resource<RecordingSearchPayload?> {
        return Resource(
            Resource.Status.SUCCESS,
            RecordingSearchPayload()
        )
    }

    override suspend fun moveTrack(
        playlistMbid: String?,
        moveTrack: MoveTrack
    ): Resource<EditPlaylistResponse?> {
        return Resource(
            Resource.Status.SUCCESS,
            EditPlaylistResponse("Ok")
        )
    }

    override suspend fun addTracks(
        playlistMbid: String?,
        playlistTracks: List<PlaylistTrack>
    ): Resource<EditPlaylistResponse?> {
        return Resource(
            Resource.Status.SUCCESS,
            EditPlaylistResponse("Ok")
        )
    }

    override suspend fun deleteTracks(
        playlistMbid: String?,
        deleteTracks: DeleteTracks
    ): Resource<EditPlaylistResponse?> {
        return Resource(
            Resource.Status.SUCCESS,
            EditPlaylistResponse("Ok")
        )
    }

}