package org.listenbrainz.android.ui.screens.listens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spotify.android.appremote.api.SpotifyAppRemote
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.model.Listen
import org.listenbrainz.android.ui.components.ListenCard
import org.listenbrainz.android.ui.components.LoadingAnimation
import org.listenbrainz.android.ui.screens.profile.UserData
import org.listenbrainz.android.util.Constants
import org.listenbrainz.android.util.Utils.getCoverArtUrl
import org.listenbrainz.android.viewmodel.ListensViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListensScreen(
    viewModel: ListensViewModel = hiltViewModel(),
    spotifyClientId: String = stringResource(id = R.string.spotifyClientId),
    shouldScrollToTop: MutableState<Boolean>,
    context: Context = LocalContext.current
) {
    DisposableEffect(Unit) {
        viewModel.connect(spotifyClientId = spotifyClientId)
        onDispose {
            SpotifyAppRemote.disconnect(viewModel.spotifyAppRemote)
        }
    }

    LaunchedEffect(Unit){
        viewModel.appPreferences.username.let {username ->
            if (username != null) {
                viewModel.fetchUserListens(userName = username)
            }
        }
    }

    val listState = rememberLazyListState()

    // Scroll to the top when shouldScrollToTop becomes true
    LaunchedEffect(shouldScrollToTop.value) {
        if (shouldScrollToTop.value) {
            listState.scrollToItem(0)
            shouldScrollToTop.value = false
        }
    }

    val youtubeApiKey = stringResource(id = R.string.youtubeApiKey)

    fun onListenTap(listen: Listen) {
        if (listen.track_metadata.additional_info?.spotify_id != null) {
            Uri.parse(listen.track_metadata.additional_info.spotify_id).lastPathSegment?.let { trackId ->
                viewModel.playUri("spotify:track:${trackId}")
            }
        } else {
            // Execute the API request asynchronously
            val scope = CoroutineScope(Dispatchers.Main)
            scope.launch {
                val videoId = viewModel
                    .searchYoutubeMusicVideoId(
                        context = context,
                        trackName = listen.track_metadata.track_name,
                        artist = listen.track_metadata.artist_name,
                        apiKey = youtubeApiKey
                    )
                when {
                    videoId != null -> {
                        // Play the track in the YouTube Music app
                        val trackUri =
                            Uri.parse("https://music.youtube.com/watch?v=$videoId")
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = trackUri
                        intent.setPackage(Constants.YOUTUBE_MUSIC_PACKAGE_NAME)
                        val activities =
                            context.packageManager.queryIntentActivities(intent, 0)

                        when {
                            activities.isNotEmpty() -> {
                                context.startActivity(intent)
                            }

                            else -> {
                                // Display an error message
                                Toast.makeText(
                                    context,
                                    "YouTube Music is not installed to play the track.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    else -> {
                        /*
                        // Play track via Amazon Music
                        val intent = Intent()
                        val query = listen.track_metadata.track_name + " " + listen.track_metadata.artist_name
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        intent.setClassName(
                            "com.amazon.mp3",
                            "com.amazon.mp3.activity.IntentProxyActivity"
                        )
                        intent.action = MediaStore.INTENT_ACTION_MEDIA_SEARCH
                        intent.putExtra(MediaStore.EXTRA_MEDIA_TITLE, query)
                        context.startActivity(intent)
                        */
                    }
                }
            }
        }
    }


    // Listens list
    val listens = viewModel.listensFlow.collectAsState().value
    val listeningNow = viewModel.listeningNow.collectAsState().value

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        var showBlacklist by remember { mutableStateOf(false) }

        LazyColumn(state = listState) {
            item {
                UserData(
                    viewModel = viewModel
                )
            }

            item {
                val pagerState = rememberPagerState(
                    initialPage = 0
                )

                HorizontalPager(state = pagerState, pageCount = 2, modifier = Modifier.fillMaxSize()) { page ->
                    when (page) {
                        0 -> {
                            AnimatedVisibility(visible = viewModel.listeningNow.collectAsState().value != null) {
                                ListeningNowCard(
                                    listeningNow!!,
                                    getCoverArtUrl(
                                        caaReleaseMbid = listeningNow.track_metadata.mbid_mapping?.caa_release_mbid,
                                        caaId = listeningNow.track_metadata.mbid_mapping?.caa_id
                                    )
                                ) {
                                    onListenTap(listeningNow)
                                }
                            }
                        }

                        1 -> {
                            AnimatedVisibility(visible = viewModel.playerState?.track?.name != null) {
                                ListeningNowOnSpotify(
                                    playerState = viewModel.playerState,
                                    bitmap = viewModel.bitmap
                                )
                            }
                        }
                    }
                }
            }

            items(listens) { listen ->
                ListenCard(
                    listen,
                    getCoverArtUrl(
                        caaReleaseMbid = listen.track_metadata.mbid_mapping?.caa_release_mbid,
                        caaId = listen.track_metadata.mbid_mapping?.caa_id
                    )
                )
                {
                  onListenTap(listen)
                }
            }
        }

        // Loading Animation
        AnimatedVisibility(
            visible = viewModel.isLoading,
            enter = fadeIn(initialAlpha = 0.4f),
            exit = fadeOut(animationSpec = tween(durationMillis = 250))
        ) {
            LoadingAnimation()
        }

        // BlackList Dialog
        if (showBlacklist) {
            ListeningAppsList(viewModel = viewModel) { showBlacklist = false }
        }

        // FAB
        if(!viewModel.appPreferences.lbAccessToken.isNullOrEmpty() && viewModel.appPreferences.isNotificationServiceAllowed) {
            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp), visible = !showBlacklist
            ) {
                FloatingActionButton(
                    modifier = Modifier.border(1.dp, Color.Gray, shape = CircleShape),
                    shape = CircleShape,
                    onClick = { showBlacklist = true },
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = "Blacklist"
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ListensScreenPreview() {
    ListensScreen(
        shouldScrollToTop = remember { mutableStateOf(false) }
    )
}