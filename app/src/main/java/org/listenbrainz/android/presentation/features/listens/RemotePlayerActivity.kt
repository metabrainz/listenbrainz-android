package org.listenbrainz.android.presentation.features.listens

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.GsonBuilder
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.SpotifyDisconnectedException
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.Image
import com.spotify.protocol.types.PlayerContext
import com.spotify.protocol.types.PlayerState
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.databinding.AppRemoteLayoutBinding
import org.listenbrainz.android.presentation.features.listens.RemotePlayerActivity.AuthParams.CLIENT_ID
import org.listenbrainz.android.presentation.features.listens.RemotePlayerActivity.AuthParams.REDIRECT_URI
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RemotePlayerActivity : AppCompatActivity() {

    object AuthParams {
        const val CLIENT_ID = "fadec988097f4480bd71608cac76d82c"
        const val REDIRECT_URI = "org.listenbrainz.android://callback"
    }

    companion object {
        const val TAG = "ListenBrainz Player"
        const val STEP_MS = 15000L
    }

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private lateinit var trackId: String

    private var playerStateSubscription: Subscription<PlayerState>? = null
    private var playerContextSubscription: Subscription<PlayerContext>? = null
    private var spotifyAppRemote: SpotifyAppRemote? = null

    private lateinit var views: List<View>
    private lateinit var trackProgressBar: TrackProgressBar
    private lateinit var binding: AppRemoteLayoutBinding

    private val errorCallback = { throwable: Throwable -> logError(throwable) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppRemoteLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        trackId = intent.getStringExtra("spotify_trackId")!!

        binding.seekTo.apply {
            isEnabled = false
            progressDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
            indeterminateDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        }

        trackProgressBar = TrackProgressBar(binding.seekTo) { seekToPosition: Long -> seekTo(seekToPosition) }

        views = listOf(
            binding.subscribeToPlayerStateButton,
            binding.playPauseButton,
            binding.seekForwardButton,
            binding.seekBackButton,
            binding.skipPrevButton,
            binding.skipNextButton,
            binding.seekTo)

        SpotifyAppRemote.setDebugMode(true)

        onDisconnected()
        connect()
    }

    private val playerStateEventCallback = Subscription.EventCallback<PlayerState> { playerState ->
        Log.v(TAG, String.format("Player State: %s", gson.toJson(playerState)))

        updateTrackStateButton(playerState)

        updatePlayPauseButton(playerState)

        updateTrackCoverArt(playerState)

        updateSeekbar(playerState)
    }

    private fun updatePlayPauseButton(playerState: PlayerState) {
        // Invalidate play / pause
        when {
            playerState.isPaused -> {
                binding.playPauseButton.setImageResource(R.drawable.btn_play)
            }
            else -> {
                binding.playPauseButton.setImageResource(R.drawable.btn_pause)
            }
        }
    }

    private fun updateTrackStateButton(playerState: PlayerState) {
        binding.currentTrackLabel.apply {
            text = String.format(Locale.US, "%s\n%s", playerState.track.name, playerState.track.artist.name)
            tag = playerState
        }
    }

    private fun updateSeekbar(playerState: PlayerState) {
        // Update progressbar
        trackProgressBar.apply {
            when {
                playerState.playbackSpeed > 0 -> {
                    unpause()
                }
                else -> {
                    pause()
                }
            }
            // Invalidate seekbar length and position
            binding.seekTo.max = playerState.track.duration.toInt()
            binding.seekTo.isEnabled = true
            setDuration(playerState.track.duration)
            update(playerState.playbackPosition)
        }
    }

    private fun updateTrackCoverArt(playerState: PlayerState) {
        // Get image from track
        assertAppRemoteConnected()
            .imagesApi
            .getImage(playerState.track.imageUri, Image.Dimension.LARGE)
            .setResultCallback { bitmap ->
                binding.image.setImageBitmap(bitmap)
            }
    }

    private fun seekTo(seekToPosition: Long) {
        assertAppRemoteConnected()
            .playerApi
            .seekTo(seekToPosition)
            .setErrorCallback(errorCallback)
    }

    override fun onStop() {
        super.onStop()
        SpotifyAppRemote.disconnect(spotifyAppRemote)
        onDisconnected()
    }

    private fun onConnected() {
        for (input in views) {
            input.isEnabled = true
        }

        onSubscribedToPlayerStateButtonClicked(binding.subscribeToPlayerStateButton)
        onSubscribedToPlayerContextButtonClicked()

        playUri("spotify:track:${trackId}")
    }

    private fun onDisconnected() {
        for (view in views) {
            view.isEnabled = false
        }
        binding.image.setImageResource(R.drawable.widget_placeholder)
        binding.subscribeToPlayerStateButton.apply {
            visibility = View.VISIBLE
            setText(R.string.title_current_track)
        }

        binding.currentTrackLabel.visibility = View.INVISIBLE
    }

    private fun connect() {
        SpotifyAppRemote.disconnect(spotifyAppRemote)
        lifecycleScope.launch {
            try {
                spotifyAppRemote = connectToAppRemote(true)
                onConnected()
            } catch (error: Throwable) {
                onDisconnected()
                logError(error)
            }
        }
    }

    private suspend fun connectToAppRemote(showAuthView: Boolean): SpotifyAppRemote? =
        suspendCoroutine { cont: Continuation<SpotifyAppRemote> ->
            SpotifyAppRemote.connect(
                application,
                ConnectionParams.Builder(CLIENT_ID)
                    .setRedirectUri(REDIRECT_URI)
                    .showAuthView(showAuthView)
                    .build(),
                object : Connector.ConnectionListener {
                    override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                        cont.resume(spotifyAppRemote)
                    }

                    override fun onFailure(error: Throwable) {
                        cont.resumeWithException(error)
                    }
                })
        }

    private fun playUri(uri: String) {
        assertAppRemoteConnected()
            .playerApi
            .play(uri)
            .setResultCallback { logMessage(getString(R.string.command_feedback, "play")) }
            .setErrorCallback(errorCallback)
    }

    fun showCurrentPlayerContext(view: View) {
        view.tag?.let {
            showDialog("PlayerContext", gson.toJson(it))
        }
    }

    fun showCurrentPlayerState(view: View) {
        view.tag?.let {
            showDialog("PlayerState", gson.toJson(it))
        }
    }

    fun onSkipPreviousButtonClicked(notUsed: View) {
        assertAppRemoteConnected()
            .playerApi
            .skipPrevious()
            .setResultCallback { logMessage(getString(R.string.command_feedback, "skip previous")) }
            .setErrorCallback(errorCallback)
    }

    fun onPlayPauseButtonClicked(notUsed: View) {
        assertAppRemoteConnected().let {
            it.playerApi
                .playerState
                .setResultCallback { playerState ->
                    if (playerState.isPaused) {
                        it.playerApi
                            .resume()
                            .setResultCallback { logMessage(getString(R.string.command_feedback, "play")) }
                            .setErrorCallback(errorCallback)
                    } else {
                        it.playerApi
                            .pause()
                            .setResultCallback { logMessage(getString(R.string.command_feedback, "pause")) }
                            .setErrorCallback(errorCallback)
                    }
                }
        }

    }

    fun onSkipNextButtonClicked(notUsed: View) {
        assertAppRemoteConnected()
            .playerApi
            .skipNext()
            .setResultCallback { logMessage(getString(R.string.command_feedback, "skip next")) }
            .setErrorCallback(errorCallback)
    }

    fun onSeekBack(notUsed: View) {
        assertAppRemoteConnected()
            .playerApi
            .seekToRelativePosition(-STEP_MS)
            .setResultCallback { logMessage(getString(R.string.command_feedback, "seek back")) }
            .setErrorCallback(errorCallback)
    }

    fun onSeekForward(notUsed: View) {
        assertAppRemoteConnected()
            .playerApi
            .seekToRelativePosition(STEP_MS)
            .setResultCallback { logMessage(getString(R.string.command_feedback, "seek fwd")) }
            .setErrorCallback(errorCallback)
    }

    fun onSubscribedToPlayerContextButtonClicked() {
        playerContextSubscription = cancelAndResetSubscription(playerContextSubscription)

        playerContextSubscription = assertAppRemoteConnected()
            .playerApi
            .subscribeToPlayerContext()
            .setErrorCallback { throwable ->
                logError(throwable)
            } as Subscription<PlayerContext>
    }

    fun onSubscribedToPlayerStateButtonClicked(notUsed: View) {
        playerStateSubscription = cancelAndResetSubscription(playerStateSubscription)

        binding.currentTrackLabel.visibility = View.VISIBLE
        binding.subscribeToPlayerStateButton.visibility = View.INVISIBLE

        playerStateSubscription = assertAppRemoteConnected()
            .playerApi
            .subscribeToPlayerState()
            .setEventCallback(playerStateEventCallback)
            .setLifecycleCallback(
                object : Subscription.LifecycleCallback {
                    override fun onStart() {
                        logMessage("Event: start")
                    }

                    override fun onStop() {
                        logMessage("Event: end")
                    }
                })
            .setErrorCallback {
                binding.currentTrackLabel.visibility = View.INVISIBLE
                binding.subscribeToPlayerStateButton.visibility = View.VISIBLE
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

    private fun assertAppRemoteConnected(): SpotifyAppRemote {
        spotifyAppRemote?.let {
            if (it.isConnected) {
                return it
            }
        }
        Log.e(TAG, getString(R.string.err_spotify_disconnected))
        throw SpotifyDisconnectedException()
    }

    private fun logError(throwable: Throwable) {
        Log.e(TAG, "", throwable)
    }

    private fun logMessage(msg: String) {
        Log.d(TAG, msg)
    }

    private fun showDialog(title: String, message: String) {
        AlertDialog.Builder(this).setTitle(title).setMessage(message).create().show()
    }
}
