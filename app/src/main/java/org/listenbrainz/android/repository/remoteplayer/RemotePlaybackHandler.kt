package org.listenbrainz.android.repository.remoteplayer

import com.spotify.protocol.types.PlayerContext
import com.spotify.protocol.types.PlayerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import org.listenbrainz.android.model.ListenBitmap
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.util.Resource

interface RemotePlaybackHandler {
    
    suspend fun searchYoutubeMusicVideoId(
        trackName: String,
        artist: String
    ): Resource<String>
    
    /** @param getYoutubeMusicVideoId Use [searchYoutubeMusicVideoId] to search for video ID while passing your own coroutine dispatcher.*/
    suspend fun playOnYoutube(
        getYoutubeMusicVideoId: suspend () -> Resource<String>
    ): Resource<Unit>
    
    /** Connect to spotify app remote using this function. **Must** connect in *onStart* only.
     *
     * **Note**: Only use [Dispatchers.Main] to establish connection.
     * Coroutine-safe*/
    suspend fun connectToSpotify(onError: (ResponseError) -> Unit = {})
    
    /** Disconnect to spotify app remote using this function. **Must** disconnect in *onStop* only.
     *
     * Coroutine-safe*/
    suspend fun disconnectSpotify()
    
    suspend fun fetchSpotifyTrackCoverArt(playerState: PlayerState?): ListenBitmap
    
    /** Usually, LB will supply the Spotify-link of the track, but this function requires track the track ID. To
     * obtain the track id from a LB provided data (usually spotifyId field of a data class) do this:
     *
     * ```
     * Uri.parse(spotifyId).lastPathSegment?.let { trackId ->
     *     playUri(trackId)
     * }
     * ```
     * @param onFailure should be alternative play option to spotify and should create its own coroutine.
     * */
    fun playUri(trackId: String, onFailure: () -> Unit)
    
    fun play(onPlay: () -> Unit = {})
    
    fun pause(onPause: () -> Unit = {})
    
    /** Main function to access all the details about spotify player.
     * @return null if flow is cancelled.*/
    fun getPlayerState(): Flow<PlayerState?>
    
    /** @return null if flow is cancelled. */
    fun getPlayerContext(): Flow<PlayerContext?>
}