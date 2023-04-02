package org.listenbrainz.android.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp
import com.spotify.android.appremote.api.error.NotLoggedInException
import com.spotify.android.appremote.api.error.UserNotAuthorizedException
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.PlayerContext
import com.spotify.protocol.types.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.listenbrainz.android.BuildConfig
import org.listenbrainz.android.model.Listen
import org.listenbrainz.android.repository.ListensRepository
import org.listenbrainz.android.service.YouTubeApiService
import org.listenbrainz.android.util.Constants
import org.listenbrainz.android.util.Log.d
import org.listenbrainz.android.util.Log.e
import org.listenbrainz.android.util.Log.v
import org.listenbrainz.android.util.Resource.Status.*
import org.listenbrainz.android.util.Utils.getCoverArtUrl
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class ListensViewModel @Inject constructor(
    val repository: ListensRepository,
    private val application: Application
) : AndroidViewModel(application) {
    // TODO: remove dependency of this view-model on application
    //  by moving spotify app remote to a repository.
    
    private val _listensFlow = MutableStateFlow(listOf<Listen>())
    val listensFlow = _listensFlow.asStateFlow()
    
    private val _coverArtFlow = MutableStateFlow(listOf<String>())
    val coverArtFlow = _coverArtFlow.asStateFlow()
    
    var isLoading: Boolean  by mutableStateOf(true)
    
    var playerState: PlayerState? by mutableStateOf(null)
    var bitmap: Bitmap? by mutableStateOf(null)
    
    private val gson = GsonBuilder().setPrettyPrinting().create()
    
    private var playerStateSubscription: Subscription<PlayerState>? = null
    private var playerContextSubscription: Subscription<PlayerContext>? = null
    var spotifyAppRemote: SpotifyAppRemote? = null
    
    private val errorCallback = { throwable: Throwable -> logError(throwable) }

    init {
        SpotifyAppRemote.setDebugMode(BuildConfig.DEBUG)
    }
    
    fun fetchUserListens(userName: String) {
        viewModelScope.launch {
            val response = repository.fetchUserListens(userName)
            when(response.status){
                SUCCESS -> {
                    val responseListens = response.data!!
                    
                    // Updating coverArts
                    _coverArtFlow.update {
                        val list = mutableListOf<String>()
                        responseListens.forEach {
                            list.add(getCoverArtUrl(
                                caaReleaseMbid = it.track_metadata.mbid_mapping?.caa_release_mbid,
                                caaId = it.track_metadata.mbid_mapping?.caa_id
                            ))
                        }
                        list
                    }
                    // Updating listens
                    _listensFlow.update { response.data }
                    isLoading = false
                }
                LOADING -> {
                    isLoading = true
                }
                FAILED -> {
                    isLoading = false
                }
            }
        }
    }

    suspend fun searchYoutubeMusicVideoId(trackName: String, artist: String, apiKey: String): String? {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(YouTubeApiService::class.java)

        return try {
            val response = service.searchVideos(
                "snippet",
                "$trackName $artist",
                "video",
                "10",
                apiKey
            )

            if (response.isSuccessful) {
                val items = response.body()?.items ?: emptyList()
                if (items.isNotEmpty()) {
                    items[0].id.videoId
                } else {
                    null
                }
            } else {
                Log.e("YouTube API Error", response.errorBody()?.string() ?: "")
                null
            }
        } catch (e: Exception) {
            Log.e("YouTube API Error", "Error occurred while searching for video ID", e)
            null
        }
    }

    private fun updateTrackCoverArt(playerState: PlayerState) {
        // Get image from track
        assertAppRemoteConnected()?.imagesApi?.getImage(playerState.track.imageUri, com.spotify.protocol.types.Image.Dimension.LARGE)?.setResultCallback { bitmapHere ->
            bitmap = bitmapHere
        }
    }
    
    private fun onConnected() {
        onSubscribedToPlayerStateButtonClicked()
        onSubscribedToPlayerContextButtonClicked()
    }
    
    fun connect(spotifyClientId: String) {
        SpotifyAppRemote.disconnect(spotifyAppRemote)
        viewModelScope.launch {
            try {
                spotifyAppRemote = connectToAppRemote(true, spotifyClientId = spotifyClientId)
                onConnected()
            } catch (error: Throwable) {
                logError(error)
            }
        }
    }
    
    private val playerContextEventCallback = Subscription.EventCallback<PlayerContext> { playerContext ->
    
    }
    
    private val playerStateEventCallback = Subscription.EventCallback<PlayerState> { playerStateHere ->
        v(String.format("Player State: %s", gson.toJson(playerStateHere)))
        
        playerState = playerStateHere
        
        updateTrackCoverArt(playerStateHere)
    }
    
    private suspend fun connectToAppRemote(showAuthView: Boolean, spotifyClientId: String): SpotifyAppRemote =
        suspendCoroutine { cont: Continuation<SpotifyAppRemote> ->
            SpotifyAppRemote.connect(
                application,
                ConnectionParams.Builder(spotifyClientId)
                    .setRedirectUri(Constants.SPOTIFY_REDIRECT_URI)
                    .showAuthView(showAuthView)
                    .build(),
                object : Connector.ConnectionListener {
                    override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                        d("App remote Connected!")
                        cont.resume(spotifyAppRemote)
                    }
                    
                    override fun onFailure(error: Throwable) {
                        if (error is CouldNotFindSpotifyApp) {
                            // TODO: Tell user that they need to install the spotify app on the phone
                        }
    
                        if (error is NotLoggedInException) {
                            // TODO: Tell user that they need to login in the spotify app
                        }
    
                        if (error is UserNotAuthorizedException) {
                            // TODO: Explicit user authorization is required to use Spotify.
                            //  The user has to complete the auth-flow to allow the app to use Spotify on their behalf
                        }
                        cont.resumeWithException(error)
                    }
                }
            )
        }
    
    fun playUri(uri: String) {
        assertAppRemoteConnected()?.playerApi?.play(uri)?.setResultCallback {
            logMessage("play command successful!")      //getString(R.string.command_feedback, "play"))
        }?.setErrorCallback(errorCallback)
    }
    
    private fun onSubscribedToPlayerContextButtonClicked() {
        playerContextSubscription = cancelAndResetSubscription(playerContextSubscription)
        playerContextSubscription = assertAppRemoteConnected()?.playerApi?.subscribeToPlayerContext()?.setEventCallback(playerContextEventCallback)?.setErrorCallback { throwable ->
            logError(throwable)
        } as Subscription<PlayerContext>
    }
    
    private fun onSubscribedToPlayerStateButtonClicked() {
        playerStateSubscription = cancelAndResetSubscription(playerStateSubscription)
        playerStateSubscription = assertAppRemoteConnected()?.playerApi?.subscribeToPlayerState()?.setEventCallback(playerStateEventCallback)?.setLifecycleCallback(
            object : Subscription.LifecycleCallback {
                override fun onStart() {
                    logMessage("Event: start")
                }
                
                override fun onStop() {
                    logMessage("Event: end")
                }
            })?.setErrorCallback {
            
        } as Subscription<PlayerState>
    }
    
    private fun <T : Any?> cancelAndResetSubscription(subscription: Subscription<T>?): Subscription<T>? {
        return subscription?.let {
            if (!it.isCanceled) {
                it.cancel()
            }
            null
        }
    }
    
    private fun assertAppRemoteConnected(): SpotifyAppRemote? {
        spotifyAppRemote?.let {
            if (it.isConnected) {
                return it
            }
        }
        logMessage("Spotify is not Connected. Use one of the 'connect' buttons")        //getString(R.string.err_spotify_disconnected))
        return null
    }
    
    private fun logError(throwable: Throwable) {
        throwable.message?.let { e(it) }
    }
    
    private fun logMessage(msg: String) {
        d(msg)
    }
}