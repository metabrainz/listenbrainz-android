package org.listenbrainz.shared.repository.remoteplayer

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.listenbrainz.shared.model.ListenBitmap
import org.listenbrainz.shared.model.ResponseError
import org.listenbrainz.shared.model.playback.SharedPlayerContext
import org.listenbrainz.shared.model.playback.SharedPlayerState
import org.listenbrainz.shared.service.YouTubeApiService
import org.listenbrainz.shared.util.Log
import org.listenbrainz.shared.util.Resource
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

class IosRemotePlaybackHandlerImpl(
    youtubeApiService: YouTubeApiService,
    private val logger: Log = Log
) : SharedRemotePlaybackHandlerImpl(youtubeApiService) {

    override suspend fun playOnYoutube(getYoutubeMusicVideoId: suspend () -> Resource<String>): Resource<Unit> {
        val result = getYoutubeMusicVideoId()
        return when(result.status){
            Resource.Status.SUCCESS -> {
                val trackUri = "https://music.youtube.com/watch?v=${result.data}"
                val url = NSURL.URLWithString(trackUri)

                if(url!= null){
                    UIApplication.sharedApplication.openURL(
                        url = url,
                        options = emptyMap<Any?,Any?>(),
                        completionHandler = null
                    )
                    Resource.success(Unit)
                }
                else{
                    ResponseError.DoesNotExist("Invalid track url destination.").asResource<Unit>().also {
                        logger.e(it.error)
                    }
                }
            }
            else -> {
                ResponseError.DoesNotExist().asResource<Unit>().also {
                    logger.e(it.error)
                }
            }
        }
    }

    // no support for ios right now
    override suspend fun connectToSpotify(onError: (ResponseError) -> Unit) {
        TODO("Not yet implemented")
    }

    // no support for ios right now
    override suspend fun disconnectSpotify() {
        TODO("Not yet implemented")
    }

    // no support for ios right now
    override suspend fun fetchSpotifyTrackCoverArt(playerState: SharedPlayerState?): ListenBitmap {
        return ListenBitmap(bitmap = null)
    }

    // no support for ios right now
    override fun playUri(trackId: String, onFailure: () -> Unit) {
        onFailure()
    }

    // no support for ios right now
    override fun play(onPlay: () -> Unit) {
        TODO("Not yet implemented")
    }

    // no support for ios right now
    override fun pause(onPause: () -> Unit) {
        TODO("Not yet implemented")
    }

    // no support for ios right now
    override fun getPlayerState(): Flow<SharedPlayerState?> {
        return flowOf(null)
    }

    // no support for ios right now
    override fun getPlayerContext(): Flow<SharedPlayerContext?> {
        return flowOf(null)
    }

}