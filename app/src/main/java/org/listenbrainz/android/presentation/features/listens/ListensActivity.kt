package org.listenbrainz.android.presentation.features.listens

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.gson.GsonBuilder
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.PlayerContext
import com.spotify.protocol.types.PlayerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.presentation.features.components.BottomNavigationBar
import org.listenbrainz.android.presentation.features.components.ListenCard
import org.listenbrainz.android.presentation.features.components.TopAppBar
import org.listenbrainz.android.presentation.features.listens.ListensActivity.AuthParams.CLIENT_ID
import org.listenbrainz.android.presentation.features.listens.ListensActivity.AuthParams.REDIRECT_URI
import org.listenbrainz.android.presentation.features.login.LoginActivity
import org.listenbrainz.android.presentation.features.login.LoginSharedPreferences.username
import org.listenbrainz.android.presentation.theme.ListenBrainzTheme
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalPagerApi::class)
@AndroidEntryPoint
class ListensActivity: ComponentActivity() {
    var loading =  mutableStateOf(true)
    var playerState: PlayerState? by mutableStateOf(null)
    var bitmap: Bitmap? by mutableStateOf(null)

    object AuthParams {
        const val CLIENT_ID = "2a0d7ed37e9841269130b82843d3f842"
        const val REDIRECT_URI = "org.metabrainz.android://callback"
    }

    companion object {
        const val TAG = "MusicBrainz Player"
    }

    private val gson = GsonBuilder().setPrettyPrinting().create()

    private var playerStateSubscription: Subscription<PlayerState>? = null
    private var playerContextSubscription: Subscription<PlayerContext>? = null
    private var spotifyAppRemote: SpotifyAppRemote? = null

    private val errorCallback = { throwable: Throwable -> logError(throwable) }

    @OptIn(ExperimentalPagerApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SpotifyAppRemote.setDebugMode(true)

        setContent {
            ListenBrainzTheme() {
                Scaffold(
                    backgroundColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
                    topBar = { TopAppBar(activity = this, title = "Listens") },
                    bottomBar = { BottomNavigationBar(activity = this) }
                ) {
                    var listensModifier: Modifier by remember { mutableStateOf(Modifier.padding(it)) }
            
                    if (playerState?.track?.name != null) {
                        listensModifier = Modifier
                            .padding(it)
                            .padding(top = 200.dp)
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(initialAlpha = 0.4f),
                            exit = fadeOut(animationSpec = tween(durationMillis = 250))
                        ) {
                            NowPlaying(
                                modifier = Modifier.padding(it),
                                activity = this@ListensActivity,
                                playerState = playerState,
                                bitmap = bitmap
                            )
                        }
                    }
            
                    AllUserListens(
                        modifier = listensModifier,
                        activity = this
                    )
                }
            }
        }

    }

    private fun updateTrackCoverArt(playerState: PlayerState) {
        // Get image from track
        assertAppRemoteConnected()?.imagesApi?.getImage(playerState.track.imageUri, com.spotify.protocol.types.Image.Dimension.LARGE)?.setResultCallback { bitmapHere ->
                bitmap = bitmapHere
            }
    }

    override fun onResume() {
        super.onResume()
        connect()
    }

    override fun onStop() {
        super.onStop()
        SpotifyAppRemote.disconnect(spotifyAppRemote)
    }

    private fun onConnected() {
        onSubscribedToPlayerStateButtonClicked()
        onSubscribedToPlayerContextButtonClicked()
    }

    private fun connect() {
        SpotifyAppRemote.disconnect(spotifyAppRemote)
        lifecycleScope.launch {
            try {
                spotifyAppRemote = connectToAppRemote(true)
                onConnected()
            } catch (error: Throwable) {
                logError(error)
            }
        }
    }

    private val playerContextEventCallback = Subscription.EventCallback<PlayerContext> { playerContext ->

    }

    private val playerStateEventCallback = Subscription.EventCallback<PlayerState> { playerStateHere ->
        Log.v(TAG, String.format("Player State: %s", gson.toJson(playerStateHere)))

        playerState = playerStateHere

        updateTrackCoverArt(playerStateHere)
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
                }
            )
        }

    fun playUri(uri: String) {
        assertAppRemoteConnected()?.playerApi?.play(uri)?.setResultCallback { logMessage(getString(R.string.command_feedback, "play")) }?.setErrorCallback(errorCallback)
    }

    fun showCurrentPlayerState(view: View) {
        view.tag?.let {
            showDialog("PlayerState", gson.toJson(it))
        }
    }

    fun onSubscribedToPlayerContextButtonClicked() {
        playerContextSubscription = cancelAndResetSubscription(playerContextSubscription)
        playerContextSubscription = assertAppRemoteConnected()?.playerApi?.subscribeToPlayerContext()?.setEventCallback(playerContextEventCallback)?.setErrorCallback { throwable ->
                logError(throwable)
            } as Subscription<PlayerContext>
    }

    fun onSubscribedToPlayerStateButtonClicked() {
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
        Log.e(TAG, getString(R.string.err_spotify_disconnected))
        return null
    }

    private fun logError(throwable: Throwable) {
        Log.e(TAG, "", throwable)
    }

    private fun logMessage(msg: String) {
        Log.d(TAG, msg)
    }

    private fun showDialog(title: String, message: String) {
        androidx.appcompat.app.AlertDialog.Builder(this).setTitle(title).setMessage(message).create().show()
    }
}

@Composable
fun NowPlaying(
    modifier: Modifier,
    activity: Activity,
    playerState: PlayerState?,
    bitmap: Bitmap?
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .height(180.dp)
            .clickable(onClick = {
                //onItemClicked(listen)
            }),
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.onSurface
    ) {
        Row(
            modifier = Modifier
            .padding(16.dp)
        ) {
            Text(
                text = "Now playing",
                modifier = Modifier.padding(4.dp),
                color = MaterialTheme.colors.surface,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.subtitle1,
                textAlign = TextAlign.Center,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(top = 40.dp)
        ) {
            val painter = rememberAsyncImagePainter(
                ImageRequest.Builder(activity)
                    .data(data = bitmap)
                    .placeholder(R.drawable.ic_coverartarchive_logo_no_text)
                    .error(R.drawable.ic_coverartarchive_logo_no_text)
                    .build()
            )

            Image(
                modifier = Modifier
                    .size(80.dp, 80.dp)
                    .clip(RoundedCornerShape(16.dp)),
                painter = painter,
                alignment = Alignment.CenterStart,
                contentDescription = "",
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                playerState?.track?.name?.let { track ->
                    Text(
                        text = track,

                        modifier = Modifier.padding(0.dp, 0.dp, 12.dp, 0.dp),
                        color = MaterialTheme.colors.surface,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.subtitle1,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = buildString {
                        append(playerState?.track?.artist?.name)
                    },
                    modifier = Modifier.padding(0.dp, 0.dp, 12.dp, 0.dp),
                    color = MaterialTheme.colors.surface,
                    style = MaterialTheme.typography.caption,
                    maxLines = 2
                )

                Row(verticalAlignment = Alignment.Bottom) {
                    playerState?.track?.album?.name?.let { album ->
                        Text(
                            text = album,
                            modifier = Modifier.padding(0.dp, 12.dp, 12.dp, 0.dp),
                            color = MaterialTheme.colors.surface,
                            style = MaterialTheme.typography.caption,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AllUserListens(
    modifier: Modifier = Modifier,
    viewModel: ListensViewModel = viewModel(),
    activity: Activity
) {
    if(username == ""){
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = {
                    activity.startActivity(Intent(activity, LoginActivity::class.java))
                })
                { Text(text = "OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    activity.finish()
                })
                { Text(text = "Cancel") }
            },
            title = { Text(text = "Please login to your profile") },
            text = { Text(text = "We will fetch your listens once you have logged in") }
        )
        return
    }

    username?.let { viewModel.fetchUserListens(userName = it) }

    AnimatedVisibility(
        visible = viewModel.isLoading,
        enter = fadeIn(initialAlpha = 0.4f),
        exit = fadeOut(animationSpec = tween(durationMillis = 250))
    ){
        Loader()
    }
    LazyColumn(modifier) {
            items(viewModel.listens) { listen->
                ListenCard(
                    listen,
                    coverArt = listen.coverArt,
                    onItemClicked = {
                        if(it.track_metadata.additional_info?.spotify_id != null) {
                            Uri.parse(it.track_metadata.additional_info.spotify_id).lastPathSegment?.let { trackId ->
                                (activity as ListensActivity).playUri("spotify:track:${trackId}")
                            }
                        }
                    }
                )
            }
    }
}

@Composable
fun Loader() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.headphone_meb_loading))
    LottieAnimation(composition)
}