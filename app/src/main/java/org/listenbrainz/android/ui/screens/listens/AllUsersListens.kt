package org.listenbrainz.android.ui.screens.listens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideLazyListPreloader
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.components.ListenCard
import org.listenbrainz.android.ui.components.LoadingAnimation
import org.listenbrainz.android.ui.navigation.AppNavigationItem
import org.listenbrainz.android.util.Constants
import org.listenbrainz.android.util.LBSharedPreferences
import org.listenbrainz.android.util.Utils.getCoverArtUrl
import org.listenbrainz.android.viewmodel.ListensViewModel


@Composable
fun AllUserListens(
    modifier: Modifier = Modifier,
    viewModel: ListensViewModel,
    navController: NavController
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        
        var showBlacklist by remember { mutableStateOf(false) }
        
        // Error Dialog
        if(LBSharedPreferences.username == "") {
            NotLoggedInErrorDialog(navController)
            return
        }
      
        
        // Loading Animation
        AnimatedVisibility(
            visible = viewModel.isLoading,
            enter = fadeIn(initialAlpha = 0.4f),
            exit = fadeOut(animationSpec = tween(durationMillis = 250))
        ){
            LoadingAnimation()
        }
        
        ListensList(viewModel, modifier)
        
        // BlackList Dialog
        if (showBlacklist){
            ListeningAppsList(viewModel = viewModel) { showBlacklist = false }
        }
        
        // FAB
        AnimatedVisibility(modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp), visible = !showBlacklist) {
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

@Composable
private fun NotLoggedInErrorDialog(navController: NavController) {
    AlertDialog(
        onDismissRequest = { /*DON'T DO ANYTHING*/ },
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
}


@Composable
@OptIn(ExperimentalGlideComposeApi::class)
private fun ListensList(
    viewModel: ListensViewModel,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current
) {
    val youtubeApiKey = stringResource(id = R.string.youtubeApiKey)
    // Listens list
    val listens = viewModel.listensFlow.collectAsState().value
    // Cover art of listens
    val coverArtList = viewModel.coverArtFlow.collectAsState().value
    
    // Preloader.
    val listState = rememberLazyListState()
    GlideLazyListPreloader(
        state = listState,
        data = coverArtList,
        size = Size(250f, 250f),
        numberOfItemsToPreload = 15
    ){ item, requestBuilder ->
        requestBuilder.placeholder(R.drawable.ic_coverartarchive_logo_no_text).override(250).load(item)
    }
    
    LazyColumn(
        modifier = modifier,
        state = listState
    ) {
        items(listens) { listen ->
            ListenCard(
                listen,
                coverArtUrl = getCoverArtUrl(
                    caaReleaseMbid = listen.track_metadata.mbid_mapping?.caa_release_mbid,
                    caaId = listen.track_metadata.mbid_mapping?.caa_id
                )
            )
            {
                if (it.track_metadata.additional_info?.spotify_id != null) {
                    Uri.parse(it.track_metadata.additional_info.spotify_id).lastPathSegment?.let { trackId ->
                        viewModel.playUri("spotify:track:${trackId}")
                    }
                } else {
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
                                val trackUri =
                                    Uri.parse("https://music.youtube.com/watch?v=$videoId")
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.data = trackUri
                                intent.setPackage(Constants.YOUTUBE_MUSIC_PACKAGE_NAME)
                                when {
                                    intent.resolveActivity(context.packageManager) != null -> {
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
        }
    }
}

@Composable
fun ListeningAppsList(
    viewModel: ListensViewModel = hiltViewModel(),
    onDismiss: () -> Unit
){
    var blacklist by remember { mutableStateOf(viewModel.appPreferences.listeningBlacklist) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        backgroundColor = MaterialTheme.colorScheme.background,
        buttons = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterEnd),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ){
                    Text(
                        text = "OK",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        title = {
            Text(
                text = "Listening Apps List",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            LazyColumn {
                items(items = viewModel.appPreferences.listeningApps){packageName ->
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        
                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .fillMaxWidth(0.85f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            
                            Image(
                                modifier = Modifier
                                    .fillMaxWidth(0.15f)
                                    .padding(end = 5.dp),
                                painter = rememberDrawablePainter(drawable = viewModel.repository.getPackageIcon(packageName)),
                                contentDescription = null
                            )
                            
                            Text(
                                modifier = Modifier.fillMaxWidth(0.85f),
                                text = viewModel.repository.getPackageLabel(packageName),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Switch(
                            modifier = Modifier
                                .fillMaxWidth(0.15f)
                                .align(Alignment.CenterEnd),
                            checked = packageName !in blacklist,
                            onCheckedChange = { isChecked ->
                                if (!isChecked){
                                    viewModel.appPreferences.listeningBlacklist = blacklist + packageName
                                    blacklist = blacklist + packageName
                                } else{
                                    viewModel.appPreferences.listeningBlacklist = blacklist - packageName
                                    blacklist = blacklist - packageName
                                }
                                
                            },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = MaterialTheme.colorScheme.inverseOnSurface,
                                checkedThumbColor = MaterialTheme.colorScheme.inverseOnSurface
                            )
                        )
                    }
                }
            }
        }
    )
}