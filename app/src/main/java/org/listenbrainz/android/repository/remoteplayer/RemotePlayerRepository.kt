package org.listenbrainz.android.repository.remoteplayer

import com.spotify.protocol.types.PlayerState
import org.listenbrainz.android.model.ListenBitmap
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.util.Resource

interface RemotePlayerRepository {
    
    suspend fun searchYoutubeMusicVideoId(
        trackName: String,
        artist: String
    ): Resource<String>
    
    suspend fun playOnYoutube(
        getYoutubeMusicVideoId: suspend () -> Resource<String>
    ): Resource<Unit>
    
    suspend fun connectToSpotify(onError: (ResponseError) -> Unit = {})
    
    fun disconnectSpotify()
    
    suspend fun updateTrackCoverArt(playerState: PlayerState): ListenBitmap
    
    fun playUri(trackId: String, onFailure: () -> Unit)
}