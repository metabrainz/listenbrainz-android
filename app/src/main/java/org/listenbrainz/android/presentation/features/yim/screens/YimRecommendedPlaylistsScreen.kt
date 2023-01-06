package org.listenbrainz.android.presentation.features.yim.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.GlideLazyListPreloader
import kotlinx.coroutines.delay
import okhttp3.*
import org.listenbrainz.android.R
import org.listenbrainz.android.data.sources.api.entities.yimdata.Track
import org.listenbrainz.android.presentation.features.yim.YimViewModel
import org.listenbrainz.android.presentation.features.yim.navigation.YimScreens
import org.listenbrainz.android.presentation.features.yim.screens.components.YimLabelText
import org.listenbrainz.android.presentation.features.yim.screens.components.YimNextButton
import org.listenbrainz.android.presentation.features.yim.screens.components.YimShareButton
import org.listenbrainz.android.presentation.features.yim.ui.theme.LocalYimPaddings
import org.listenbrainz.android.presentation.features.yim.ui.theme.YearInMusicTheme
import org.listenbrainz.android.presentation.features.yim.ui.theme.YimPaddings
import org.listenbrainz.android.presentation.theme.lb_purple

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
    
        var startSecondAnim by remember{
            mutableStateOf(false)
        }
    
        LaunchedEffect(Unit) {
            delay(1200)
            startAnim = true
            delay(700)     // Since it takes 700 ms for fist list to animate.
            startSecondAnim = true
        }
        
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            
            item {
                YimLabelText(heading = "2022 Playlists", subHeading = "Generated just for you")
            }
    
            // Top Discoveries Card
            item {
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
                            enter = expandVertically(animationSpec = tween(700))
                        ) {
                            // List of songs
                            YimTopDiscoveriesOrMissedList(paddings, viewModel, isTopDiscoveriesPlaylist = true)
                        }
                
                    }
                }
            }
            
            // Top Missed Card
            item {
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
                            enter = expandVertically(animationSpec = tween(700))
                        ) {
                            // List of songs
                            YimTopDiscoveriesOrMissedList(paddings, viewModel, isTopDiscoveriesPlaylist = false)
                        }
            
                    }
                }
            }
    
            // Share Button and next
            if (startSecondAnim) {
                item {
                    Row(modifier = Modifier.padding(vertical = 50.dp)) {
                        YimShareButton(isRedTheme = true)
                        YimNextButton {
                            navController.navigate(route = YimScreens.YimDiscoverScreen.name)
                        }
                    }
                }
            }
            
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
        LazyHorizontalGrid(
            rows = GridCells.Fixed(3),
            modifier = Modifier.size(225.dp)
        ) {
            items(viewModel.getUrlsForAlbumArt(isTopDiscoveriesPlaylist = isTopDiscoveriesPlaylist)) { url ->
                GlideImage(
                    model = url,
                    modifier = Modifier.size(75.dp),
                    contentDescription = "Album Cover Art"
                ) {
                    it.placeholder(R.drawable.ic_coverartarchive_logo_no_text)
                        .override(125)
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
    val uriList = arrayListOf<String>()
    val listOfTracks = arrayListOf<Map.Entry<Track, String>>()
    
    val playlistMap : Map<Track, String>
        = if (isTopDiscoveriesPlaylist)
            viewModel.getTopDiscoveriesPlaylistAndArtCover()
        else
            viewModel.getTopMissedPlaylistAndArtCover()
    
    playlistMap.forEach { item ->
        uriList.add(item.value)
        listOfTracks.add(item)
    }
    
    // Pre-loading images
    val listState = rememberLazyListState()
    GlideLazyListPreloader(
        state = listState,
        data = uriList,
        size = Size(85f,85f),
        numberOfItemsToPreload = 15,
        fixedVisibleItemCount = 5
    ){ item, requestBuilder ->
        requestBuilder.load(item).placeholder(R.drawable.ic_coverartarchive_logo_no_text)
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = paddings.tinyPadding)
                    .clickable(enabled = true) {
                              context.startActivity(
                                  Intent(Intent.ACTION_VIEW, Uri.parse(item.key.identifier))
                              )
                        // Sends the user to recordings page just like website.
                    },
                shape = RoundedCornerShape(5.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    
                    // Album cover art
                    GlideImage(
                        model = item.value,
                        modifier = Modifier.size(60.dp),
                        contentScale = ContentScale.Fit,
                        contentDescription = "Album Cover Art"
                    ) {
                        it.placeholder(R.drawable.ic_coverartarchive_logo_no_text)
                            .override(85)
                    }
                    
                    Spacer(modifier = Modifier.width(paddings.defaultPadding))
                    
                    Column(modifier = Modifier) {
                        Text(
                            text = item.key.title,
                            style = MaterialTheme.typography.bodyLarge
                                .copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = lb_purple,
                                    lineHeight = 14.sp
                                ) ,
                        )
                        Text(
                            text = item.key.creator,
                            style = MaterialTheme.typography.bodyMedium
                                .copy(
                                    fontWeight = FontWeight.Bold,
                                    color = lb_purple.copy(alpha = 0.7f)
                                )
                        )
                    }
                }
            }
        }
    }
}