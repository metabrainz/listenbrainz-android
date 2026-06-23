package org.listenbrainz.shared.repository.remoteplayer

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.net.toUri
import com.spotify.android.appremote.BuildConfig
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp
import com.spotify.android.appremote.api.error.NotLoggedInException
import com.spotify.android.appremote.api.error.UserNotAuthorizedException
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.ImageUri
import com.spotify.protocol.types.PlayerContext
import com.spotify.protocol.types.PlayerState
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.listenbrainz.shared.BuildKonfig
import org.listenbrainz.shared.model.ListenBitmap
import org.listenbrainz.shared.model.ResponseError
import org.listenbrainz.shared.model.playback.SharedPlayerContext
import org.listenbrainz.shared.model.playback.SharedPlayerState
import org.listenbrainz.shared.repository.PlatformContext
import org.listenbrainz.shared.service.YouTubeApiService
import org.listenbrainz.shared.util.Constants
import org.listenbrainz.shared.util.Resource
import org.listenbrainz.shared.util.Log
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AndroidRemotePlaybackHandlerImpl(
    private val appContext: PlatformContext,
    private val youtubeApiService: YouTubeApiService,
    private val logger: Log = Log
) : SharedRemotePlaybackHandlerImpl(youtubeApiService) {

    private val mutex = Mutex()
    private var spotifyAppRemote: SpotifyAppRemote? = null

    /** This variable is used to maintain concurrency because spotify async tasks can cause
     * continuations to resume twice.*/
    private var isResumed: AtomicBoolean = AtomicBoolean(false)

    init {
        SpotifyAppRemote.setDebugMode(BuildConfig.DEBUG)
    }

    override suspend fun playOnYoutube(getYoutubeMusicVideoId: suspend () -> Resource<String>): Resource<Unit> {

        val result = getYoutubeMusicVideoId()

        return when (result.status) {
            Resource.Status.SUCCESS -> {
                // Play the track in the YouTube Music app
                val trackUri = "https://music.youtube.com/watch?v=${result.data}".toUri()

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
                        val message = "YouTube Music is not installed to play the track."
                        logger.e(message)
                        // Display an error message
                        ResponseError.DoesNotExist(message).asResource()
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
                ResponseError.DoesNotExist().asResource()
            }
        }
    }


    override suspend fun connectToSpotify(onError: (ResponseError) -> Unit) {
        try {
            mutex.withLock {
                logger.d("Init connection to spotify.")
                SpotifyAppRemote.disconnect(spotifyAppRemote)
                isResumed.set(false)
                spotifyAppRemote = connectToAppRemote(
                    true,
                    spotifyClientId = BuildKonfig.SPOTIFY_CLIENT_ID,
                    onError
                )
            }
        } catch (error: Throwable) {
            logError(error)
        }
    }


    private suspend fun connectToAppRemote(
        showAuthView: Boolean,
        spotifyClientId: String,
        onError: (ResponseError) -> Unit
    ): SpotifyAppRemote = suspendCancellableCoroutine { cont: Continuation<SpotifyAppRemote> ->
        SpotifyAppRemote.connect(
            appContext,
            ConnectionParams.Builder(spotifyClientId)
                .setRedirectUri(Constants.SPOTIFY_REDIRECT_URI)
                .showAuthView(showAuthView)
                .build(),
            object : Connector.ConnectionListener {

                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                    logger.d("App remote Connected!")
                    if (!isResumed.get()) {
                        cont.resume(spotifyAppRemote)
                        isResumed.set(true)
                    }
                }

                override fun onFailure(error: Throwable) {
                    if (error is CouldNotFindSpotifyApp) {
                        // Tell user that they need to install the spotify app on the phone.
                        onError(
                            ResponseError.RemotePlayerError(
                                actualResponse = "Install the Spotify app in order to play songs seamlessly."
                            )
                        )
                    }

                    if (error is NotLoggedInException) {
                        // Tell user that they need to login in the spotify app.
                        onError(
                            ResponseError.RemotePlayerError(
                                actualResponse = "Login into Spotify app in order to play songs from your account."
                            )
                        )
                    }

                    if (error is UserNotAuthorizedException) {
                        // Explicit user authorization is required to use Spotify.
                        // The user has to complete the auth-flow to allow the app to use Spotify on their behalf.
                        onError(
                            ResponseError.RemotePlayerError(
                                actualResponse = "Authorize ListenBrainz Android in order to play songs from Spotify."
                            )
                        )
                    }

                    // Throw exception
                    if (!isResumed.get()) {
                        logError(error)
                        cont.resumeWithException(error)
                        isResumed.set(true)
                    }

                }
            }
        )
    }


    override suspend fun disconnectSpotify() {
        if (!mutex.isLocked) {
            // Means our app is not establishing another instance and
            // we are free to disconnect and make spotifyAppRemote null.
            // We need our mutex to be free because we don't want spotify to be made
            // null immediately AFTER another instance is assigned by a new screen.
            mutex.withLock {
                SpotifyAppRemote.disconnect(spotifyAppRemote)
                spotifyAppRemote = null
            }
        }
    }


    override suspend fun fetchSpotifyTrackCoverArt(playerState: SharedPlayerState?): ListenBitmap = suspendCancellableCoroutine { cont ->
        fun getFallBackCoverArt(): ListenBitmap {
            // Fallback Cover Art
            val fallbackResourceId = appContext.resources.getIdentifier(
                "ic_coverartarchive_logo_no_text",
                "drawable",
                appContext.packageName
            )
            val androidBitmap = if (fallbackResourceId != 0) {
                BitmapFactory.decodeResource(
                    appContext.resources, fallbackResourceId
                )
            } else {
                null
            }
            return ListenBitmap(
                bitmap = androidBitmap?.asImageBitmap(),
                id = null
            )
        }

        // Return if URI is null
        if (playerState == null) {
            cont.resume(getFallBackCoverArt())
        }

        // Get image from track
        (assertAppRemoteConnected() ?: return@suspendCancellableCoroutine)
            .imagesApi
            .getImage(
                ImageUri(playerState?.imageUri ?: ""),
                com.spotify.protocol.types.Image.Dimension.LARGE
            )
            ?.setResultCallback { bitmapHere ->
            cont.resume(
                ListenBitmap(
                    bitmap = bitmapHere.asImageBitmap(),
                    id = playerState?.trackUri
                )
            )
        }?.setErrorCallback {
            cont.resume(getFallBackCoverArt())
        }
    }


    override fun playUri(trackId: String, onFailure: () -> Unit) {
        assertAppRemoteConnected()?.playerApi?.play("spotify:track:${trackId}")?.setResultCallback {
            logMessage("Play command successful!")      //getString(R.string.command_feedback, "play"))
        }?.setErrorCallback {
            errorCallback(it)
            onFailure()
        }
    }


    override fun play(onPlay: () -> Unit) {
        assertAppRemoteConnected()?.playerApi?.resume()?.setResultCallback {
            onPlay()
            logMessage("Play command successful!")      //getString(R.string.command_feedback, "play"))
        }?.setErrorCallback(errorCallback)
    }


    override fun pause(onPause: () -> Unit) {
        assertAppRemoteConnected()?.playerApi?.pause()?.setResultCallback {
            logMessage("Pause command successful!")      //getString(R.string.command_feedback, "play"))
        }?.setErrorCallback(errorCallback)
        onPause()
    }


    override fun getPlayerContext(): Flow<SharedPlayerContext?> = callbackFlow {
        
        val playerContextSubscription = assertAppRemoteConnected()?.playerApi
            ?.subscribeToPlayerContext()
            ?.setEventCallback { playerContext ->
                trySendBlocking(playerContext?.toSharedContext)
                    .onFailure {
                        it?.printStackTrace()
                    }
            }
            ?.setErrorCallback{ error ->
                trySendBlocking(null)
                    .onFailure {
                        it?.printStackTrace()
                    }
                errorCallback(error)
                cancel(error?.localizedMessage.toString())
            } as Subscription<PlayerContext>
    
        awaitClose {
            logMessage("Spotify: Player context subscription cancelled.")
            cancelAndResetSubscription(playerContextSubscription)
        }
    }.distinctUntilChanged().cancellable()


    override fun getPlayerState(): Flow<SharedPlayerState?> = callbackFlow {

        val playerStateSubscription = assertAppRemoteConnected()?.playerApi?.subscribeToPlayerState()
            ?.setEventCallback{ playerState ->
                trySendBlocking(playerState?.toSharedState)
                    .onFailure {
                        it?.printStackTrace()
                    }
            }
            ?.setLifecycleCallback(
                object : Subscription.LifecycleCallback {
                    override fun onStart() {
                        logMessage("Event: start")
                    }

                    override fun onStop() {
                        logMessage("Event: end")
                    }
                }
            )
            ?.setErrorCallback { error ->
                logMessage("PlayerState Callback: ${error.localizedMessage}")
                trySendBlocking(null)
                    .onFailure {
                        it?.printStackTrace()
                    }
            } as Subscription<PlayerState>?
        
        awaitClose {
            logMessage("Spotify: Player state subscription cancelled.")
            cancelAndResetSubscription(playerStateSubscription)
        }

    }.distinctUntilChanged().cancellable()

    // Private utility functions

    private fun <T : Any?> cancelAndResetSubscription(subscription: Subscription<T>?) {
        subscription?.let {
            if (!it.isCanceled) {
                it.cancel()
            }
        }
    }

    private fun assertAppRemoteConnected(): SpotifyAppRemote? {
        spotifyAppRemote?.let {
            if (it.isConnected) {
                return it
            }
        }
        logMessage("Spotify is not Connected.")        //getString(R.string.err_spotify_disconnected))
        return null
    }

}

internal val PlayerState.toSharedState : SharedPlayerState
    get() = SharedPlayerState(
        trackUri = this.track?.uri,
        trackName = this.track?.name,
        imageUri = this.track?.imageUri?.raw,
        artistName = this.track?.artist?.name,
        albumName = this.track?.album?.name,
        isPaused = this.isPaused,
        playbackPosition = this.playbackPosition,
        trackDuration = this.track?.duration ?: 0L
    )

internal val PlayerContext.toSharedContext : SharedPlayerContext
    get() = SharedPlayerContext(
        title = this.title,
        url = this.uri ?: ""
    )