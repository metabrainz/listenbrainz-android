package org.listenbrainz.android.ui.screens.yim

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import okhttp3.*
import org.listenbrainz.android.R
import org.listenbrainz.android.model.yimdata.YimScreens
import org.listenbrainz.android.model.yimdata.Track
import org.listenbrainz.android.ui.components.YimLabelText
import org.listenbrainz.android.ui.components.YimListenCard
import org.listenbrainz.android.ui.components.YimNavigationStation
import org.listenbrainz.android.ui.theme.LocalYimPaddings
import org.listenbrainz.android.ui.theme.YearInMusicTheme
import org.listenbrainz.android.ui.theme.YimPaddings
import org.listenbrainz.android.viewmodel.YimViewModel

@Composable
fun YimRecommendedPlaylistsScreen(
    viewModel: YimViewModel,
    navController: NavController,
    paddings: YimPaddings = LocalYimPaddings.current
){
    YearInMusicTheme(redTheme = true) {
        var startAnim by remember{
            mutableStateOf(false)
        }
    
        LaunchedEffect(Unit) {
            startAnim = true
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(state = rememberScrollState())
                .testTag(stringResource(id = R.string.tt_yim_recommended_playlists_parent)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            
            YimLabelText(heading = "2022 Playlists", subHeading = "Generated just for you")
    
            // Top Discoveries Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 0.dp, max = 750.dp)
                    .padding(
                        start = paddings.defaultPadding,
                        end = paddings.defaultPadding,
                        bottom = 50.dp
                    ),
                shadowElevation = 10.dp,
                shape = RoundedCornerShape(10.dp)
            ) {
                // Inside Card Column
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    
                    Spacer(modifier = Modifier.height(paddings.largePadding))
                    
                    // Collage of albums
                    YimAlbumArt(viewModel = viewModel, isTopDiscoveriesPlaylist = true)
                    // Heading Text
                    Text(
                        text = "Top discoveries of 2022",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = paddings.smallPadding)
                    )
                    AnimatedVisibility(
                        visible = startAnim,
                        enter = expandVertically(animationSpec = tween(durationMillis = 700, delayMillis = 1200))
                    ) {
                        // List of songs
                        YimTopDiscoveriesOrMissedList(paddings, viewModel, isTopDiscoveriesPlaylist = true)
                    }
                }
            }
            
            
            // Top Missed Card
            
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 0.dp, max = 700.dp)
                    .padding(
                        start = paddings.defaultPadding,
                        end = paddings.defaultPadding
                    ),
                shadowElevation = 10.dp,
                shape = RoundedCornerShape(10.dp)
            ) {
                // Inside Card Column
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(paddings.largePadding))
                    // Collage of albums
                    YimAlbumArt(viewModel = viewModel, isTopDiscoveriesPlaylist = false)
                    // Heading Text
                    Text(
                        text = "Missed tracks of 2022",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = paddings.smallPadding)
                    )
                    AnimatedVisibility(
                        visible = startAnim,
                        enter = expandVertically(animationSpec = tween(durationMillis = 700, delayMillis = 1900))
                    ) {
                        // List of songs
                        YimTopDiscoveriesOrMissedList(paddings, viewModel, isTopDiscoveriesPlaylist = false)
                    }
                }
            }
            
            // Share Button and next
            YimNavigationStation(
                navController = navController,
                viewModel = viewModel,
                typeOfImage = arrayOf(),    //arrayOf("discovery-playlist", "missed-playlist"),     // Files too large
                route = YimScreens.YimDiscoverScreen
            )
            
        }
    }
}

@Composable
@OptIn(ExperimentalGlideComposeApi::class)
private fun YimAlbumArt(viewModel: YimViewModel, isTopDiscoveriesPlaylist: Boolean) {
    Box(
        modifier = Modifier.size(310.dp),
        contentAlignment = Alignment.Center
    ) {
        val list = remember {
            viewModel.getUrlsForAlbumArt(isTopDiscoveriesPlaylist = isTopDiscoveriesPlaylist)
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.size(225.dp),
            userScrollEnabled = false
        ) {
            items(list.size) { index ->
                GlideImage(
                    model = list[index],
                    modifier = Modifier.size(75.dp),
                    contentDescription = "Album Cover Art"
                ) {
                    it.placeholder(R.drawable.ic_coverartarchive_logo_no_text)
                        .override(120)
                }
            }
        }
        Image(
            painter = painterResource(id = R.drawable.yim_frame),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(310.dp)
                .offset(y = (-16).dp),
            contentDescription = "Album frame"
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun YimTopDiscoveriesOrMissedList(
    paddings: YimPaddings,
    viewModel: YimViewModel,
    isTopDiscoveriesPlaylist: Boolean,
    context: Context = LocalContext.current
) {
    
    val playlistMap : Map<Track, String>
            = remember {
        if (isTopDiscoveriesPlaylist)
            viewModel.getTopDiscoveriesPlaylistAndArtCover()
        else
            viewModel.getTopMissedPlaylistAndArtCover()
    }
    
    val listOfTracks = remember {
        arrayListOf<Map.Entry<Track, String>>().apply {
            playlistMap.forEach { item ->
                add(item)
            }
        }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondary),
        verticalArrangement = Arrangement.Center,
        contentPadding = PaddingValues(
            horizontal = paddings.smallPadding,
            vertical = paddings.smallPadding
        )
    ) {
        items(listOfTracks) { item ->
            
            // Listen Card
            YimListenCard(
                releaseName = item.key.title,
                artistName = item.key.creator,
                coverArtUrl = item.value,
            ){
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(item.key.identifier))
                )
            }
            
        }
    }
}

