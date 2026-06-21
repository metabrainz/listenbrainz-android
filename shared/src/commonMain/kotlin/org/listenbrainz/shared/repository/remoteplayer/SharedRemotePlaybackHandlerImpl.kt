package org.listenbrainz.shared.repository.remoteplayer

import org.listenbrainz.shared.BuildKonfig
import org.listenbrainz.shared.model.ResponseError
import org.listenbrainz.shared.service.YouTubeApiService
import org.listenbrainz.shared.util.Log
import org.listenbrainz.shared.util.Resource
import org.listenbrainz.shared.util.Utils

abstract class SharedRemotePlaybackHandlerImpl(
    private val youtubeApiService: YouTubeApiService
): RemotePlaybackHandler {

    /** Search for video ID on youtube.
     * @return *null* in case no videos are found or an exception occurs.*/
    override suspend fun searchYoutubeMusicVideoId(
        trackName: String,
        artist: String
    ):  Resource<String> = Utils.parseResponse {
        val response = youtubeApiService.searchVideos(
            part = "snippet",
            query = "$trackName $artist",
            type = "video",
            videoCategoryId = "10",
            apiKey = BuildKonfig.YOUTUBE_API_KEY
        )

        val items = response.items

        failIf(items.isEmpty()) {
            ResponseError.RemotePlayerError(
                actualResponse = "Could not find this song on youtube."
            )
        }

        return@parseResponse items.first().id.videoId
    }

    protected fun logError(throwable: Throwable) {
        Log.e(throwable)
    }

    protected fun logMessage(msg: String) {
        Log.d(msg)
    }

    protected val errorCallback = { throwable: Throwable -> logError(throwable) }

}