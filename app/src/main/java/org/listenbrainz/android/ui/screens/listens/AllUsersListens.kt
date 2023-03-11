package org.listenbrainz.android.ui.screens.listens

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.components.ListenCard
import org.listenbrainz.android.ui.components.Loader
import org.listenbrainz.android.ui.navigation.AppNavigationItem
import org.listenbrainz.android.util.Constants
import org.listenbrainz.android.util.LBSharedPreferences
import org.listenbrainz.android.viewmodel.ListensViewModel


@Composable
fun AllUserListens(
    modifier: Modifier = Modifier,
    viewModel: ListensViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val youtubeApiKey = stringResource(id = R.string.youtubeApiKey)

    if(LBSharedPreferences.username == "") {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = {
                    navController.navigate(AppNavigationItem.Profile.route)
                })
                { Text(text = "OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    navController.popBackStack()
                })
                { Text(text = "Cancel") }
            },
            title = { Text(text = "Please login to your profile") },
            text = { Text(text = "We will fetch your listens once you have logged in") }
        )
        return
    }

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
                coverArt = listen.coverArt,     // TODO: Fix coverArts not working
                onItemClicked = {
                    if(it.track_metadata.additional_info?.spotify_id != null) {
                        Uri.parse(it.track_metadata.additional_info.spotify_id).lastPathSegment?.let { trackId ->
                            viewModel.playUri("spotify:track:${trackId}")
                        }
                    }
                    else{
                        // Execute the API request asynchronously
                        val scope = CoroutineScope(Dispatchers.Main)
                        scope.launch {
                            val videoId = viewModel
                                .searchYoutubeMusicVideoId(
                                    trackName = listen.track_metadata.track_name,
                                    artist = listen.track_metadata.artist_name,
                                    apiKey = youtubeApiKey
                                )
                            when {
                                videoId != null -> {
                                    // Play the track in the YouTube Music app
                                    val trackUri = Uri.parse("https://music.youtube.com/watch?v=$videoId")
                                    val intent = Intent(Intent.ACTION_VIEW)
                                    intent.data = trackUri
                                    intent.setPackage(Constants.YOUTUBE_MUSIC_PACKAGE_NAME)
                                    when {
                                        intent.resolveActivity(context.packageManager) != null -> {
                                            context.startActivity(intent)
                                        }
                                        else -> {
                                            // Display an error message
                                            Toast.makeText(context, "YouTube Music is not installed to play the track.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                else -> {
                                    // Play track via Amazon Music
//                                    val intent = Intent()
//                                    val query = listen.track_metadata.track_name + " " + listen.track_metadata.artist_name
//                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                                    intent.setClassName(
//                                        "com.amazon.mp3",
//                                        "com.amazon.mp3.activity.IntentProxyActivity"
//                                    )
//                                    intent.action = MediaStore.INTENT_ACTION_MEDIA_SEARCH
//                                    intent.putExtra(MediaStore.EXTRA_MEDIA_TITLE, query)
//                                    context.startActivity(intent)
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}