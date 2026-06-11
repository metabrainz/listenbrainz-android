package org.listenbrainz.sharedtest.mocks

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.listenbrainz.shared.model.ListenBitmap
import org.listenbrainz.shared.model.ResponseError
import org.listenbrainz.shared.model.playback.SharedPlayerContext
import org.listenbrainz.shared.model.playback.SharedPlayerState
import org.listenbrainz.shared.repository.remoteplayer.RemotePlaybackHandler
import org.listenbrainz.shared.util.Resource


class MockRemotePlaybackHandler : RemotePlaybackHandler {
    override suspend fun searchYoutubeMusicVideoId(
        trackName: String,
        artist: String
    ): Resource<String> {
        TODO("Not yet implemented")
    }

    override suspend fun playOnYoutube(getYoutubeMusicVideoId: suspend () -> Resource<String>): Resource<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun connectToSpotify(onError: (ResponseError) -> Unit) {
        TODO("Not yet implemented")
    }

    override suspend fun disconnectSpotify() {
        TODO("Not yet implemented")
    }

    override suspend fun fetchSpotifyTrackCoverArt(playerState: SharedPlayerState?): ListenBitmap {
        TODO("Not yet implemented")
    }

    override fun playUri(trackId: String, onFailure: () -> Unit) {
        TODO("Not yet implemented")
    }

    override fun play(onPlay: () -> Unit) {
        TODO("Not yet implemented")
    }

    override fun pause(onPause: () -> Unit) {
        TODO("Not yet implemented")
    }

    override fun getPlayerState(): Flow<SharedPlayerState?> {
        return flow {

        }
    }

    override fun getPlayerContext(): Flow<SharedPlayerContext?> {
        TODO("Not yet implemented")
    }

}