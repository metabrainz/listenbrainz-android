package org.listenbrainz.sharedtest.mocks

import com.spotify.protocol.types.PlayerContext
import com.spotify.protocol.types.PlayerState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.listenbrainz.android.model.ListenBitmap
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.repository.remoteplayer.RemotePlaybackHandler
import org.listenbrainz.android.util.Resource


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

    override suspend fun fetchSpotifyTrackCoverArt(playerState: PlayerState?): ListenBitmap {
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

    override fun getPlayerState(): Flow<PlayerState?> {
        return flow {

        }
    }

    override fun getPlayerContext(): Flow<PlayerContext?> {
        TODO("Not yet implemented")
    }

}