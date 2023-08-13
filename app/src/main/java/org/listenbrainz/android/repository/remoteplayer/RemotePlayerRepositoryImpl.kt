package org.listenbrainz.android.repository.remoteplayer

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import org.listenbrainz.android.R
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.service.YouTubeApiService
import org.listenbrainz.android.util.Constants
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemotePlayerRepositoryImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val youtubeApiService: YouTubeApiService
) : RemotePlayerRepository {
    
    /** Search for video ID on youtube.
     * @return *null* in case no videos are found or an exception occurs.*/
    override suspend fun searchYoutubeMusicVideoId(
        trackName: String,
        artist: String
    ): Resource<String> = runCatching {
        
        val response = youtubeApiService.searchVideos(
            part = "snippet",
            query = "$trackName $artist",
            type = "video",
            videoCategoryId = "10",
            apiKey = appContext.getString(R.string.youtubeApiKey)
        )
    
        return@runCatching if (response.isSuccessful) {
            val items = response.body()?.items
            if (!items.isNullOrEmpty()) {
                Resource.success(items.first().id.videoId)
            } else {
                Resource.failure(error = ResponseError.DOES_NOT_EXIST.apply { actualResponse = "Could not find this song on youtube." })
            }
        } else {
            Resource.failure(error = ResponseError.getError(response = response))
        }
        
    }.getOrElse { Utils.logAndReturn(it) }
    
    
    /** @param getYoutubeMusicVideoId Use [searchYoutubeMusicVideoId] to search for video ID while passing your own coroutine dispatcher.*/
    override suspend fun playOnYoutube(getYoutubeMusicVideoId: suspend () -> Resource<String>): Resource<Unit> {
        
        val result = getYoutubeMusicVideoId()
    
        return when(result.status) {
            Resource.Status.SUCCESS -> {
                // Play the track in the YouTube Music app
                val trackUri = Uri.parse("https://music.youtube.com/watch?v=${result.data}")
                
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = trackUri
                intent.setPackage(Constants.YOUTUBE_MUSIC_PACKAGE_NAME)
                
                val activities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    appContext.packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
                } else {
                    appContext.packageManager.queryIntentActivities(intent, 0)
                }
    
                when {
                    activities.isNotEmpty() -> {
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        appContext.startActivity(intent)
                        Resource.success(Unit)
                    }
                    else -> {
                        // Display an error message
                        Resource.failure(error = ResponseError.DOES_NOT_EXIST.apply { actualResponse = "YouTube Music is not installed to play the track." })
                    }
                }
            }
    
            else -> {
                /*
                // Play track via Amazon Music
                val intent = Intent()
                val query = listen.trackMetadata.trackName + " " + listen.trackMetadata.artistName
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.setClassName(
                    "com.amazon.mp3",
                    "com.amazon.mp3.activity.IntentProxyActivity"
                )
                intent.action = MediaStore.INTENT_ACTION_MEDIA_SEARCH
                intent.putExtra(MediaStore.EXTRA_MEDIA_TITLE, query)
                context.startActivity(intent)
                */
                Resource.failure(error = ResponseError.DOES_NOT_EXIST)
            }
        }
    }
    
}