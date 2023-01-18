package org.listenbrainz.android.presentation.features.yim.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.GlideLazyListPreloader
import kotlinx.coroutines.delay
import org.listenbrainz.android.R
import org.listenbrainz.android.data.sources.api.entities.yimdata.TopRelease
import org.listenbrainz.android.presentation.features.yim.YimViewModel
import org.listenbrainz.android.presentation.features.yim.navigation.YimScreens
import org.listenbrainz.android.presentation.features.yim.navigation.YimShareable
import org.listenbrainz.android.presentation.features.yim.screens.components.YimHeadingText
import org.listenbrainz.android.presentation.features.yim.screens.components.YimNavigationStation
import org.listenbrainz.android.presentation.features.yim.ui.theme.LocalYimPaddings
import org.listenbrainz.android.presentation.features.yim.ui.theme.YearInMusicTheme
import org.listenbrainz.android.presentation.features.yim.ui.theme.YimPaddings

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun YimTopAlbumsScreen(
    yimViewModel: YimViewModel,
    navController: NavController,
    paddings: YimPaddings = LocalYimPaddings.current,
) {
    YearInMusicTheme(redTheme = false) {
        
        var startAnim by remember{
            mutableStateOf(false)
        }
        
        val cardHeight by animateDpAsState(
            targetValue = if (startAnim) 460.dp else 50.dp,
            animationSpec = tween(durationMillis = 1000, delayMillis = 1000)
        )
    
        LaunchedEffect(Unit) {
            startAnim = true
        }
    
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Elevated Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cardHeight)
                    .padding(horizontal = paddings.defaultPadding),
                shadowElevation = 5.dp,
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Card Heading
                    YimHeadingText(text = "Top Albums of 2022")
                
                    // Album Viewer
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = paddings.tinyPadding)
                            .background(MaterialTheme.colorScheme.secondary),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        
                        /** Variables for glide preloader*/
                        val uriList : ArrayList<String> = arrayListOf()
                        val topReleases : List<TopRelease>? = yimViewModel.getTopReleases()?.toList()
                        topReleases?.forEach { item ->
                            // https://archive.org/download/mbid-{caa_release_mbid}/mbid-{caa_release_mbid}-{caa_id}_thumb500.jpg
                            uriList.add("https://archive.org/download/mbid-${item.caaReleaseMbid}/mbid-${item.caaReleaseMbid}-${item.caaId}_thumb500.jpg")
                        }
                        
                        // Pre-loading images
                        val listState = rememberLazyListState()
                        GlideLazyListPreloader(
                            state = listState,
                            data = uriList,
                            size = Size(300f,300f),
                            numberOfItemsToPreload = 20,
                            fixedVisibleItemCount = 3
                        ){ item, requestBuilder ->
                            requestBuilder.load(item).placeholder(R.drawable.yim_album_placeholder)
                        }
                        
                        YimAlbumViewer(list = topReleases, listState = listState, viewModel = yimViewModel)
                        
                    }
                }
                
            }
            
            
            // To Avoid multiple recompositions for both share and next buttons
            var animateShareButton by remember { mutableStateOf(false) }
            LaunchedEffect(Unit){
                delay(2700)
                animateShareButton = true
            }
            
            // Share Button and next Button
            AnimatedVisibility(visible = animateShareButton) {
                YimNavigationStation(
                    typeOfImage = arrayOf(YimShareable.ALBUMS),
                    navController = navController,
                    viewModel = yimViewModel,
                    modifier = Modifier.padding(top = 40.dp),
                    route = YimScreens.YimChartsScreen
                )
            }
        }
    }
}


@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
@Composable
private fun YimAlbumViewer(list: List<TopRelease>?, listState: LazyListState, viewModel: YimViewModel) {
    
    // This prevents image from being blur or crashing the app.
    var renderImage by remember { mutableStateOf(false) }
    LaunchedEffect(true){
        delay(2000)
        renderImage = true
    }
    
    // Avoids pop in
    val alphaAnimation by animateFloatAsState(
        targetValue = if (renderImage) 1f else 0f,
        animationSpec = tween(1000)
    )
    
    // This if condition avoids stuttering as it blocks composition until rest of the animations are finished.
    if (renderImage) {
        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                // Animates the grayish background of this window.
                .padding(vertical = LocalYimPaddings.current.extraLargePadding)
                .alpha(alphaAnimation)
                .animateContentSize(),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)        // Centre snapping effect
        ) {
            
            itemsIndexed(list!!.toList()) { index, item ->
                
                if (index == 0) {
                    Spacer(modifier = Modifier.width(LocalYimPaddings.current.defaultPadding))
                }else
                    Spacer(modifier = Modifier.width(LocalYimPaddings.current.smallPadding))
                    
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    
                    GlideImage(
                        model = "https://archive.org/download/mbid-${item.caaReleaseMbid}/mbid-${item.caaReleaseMbid}-${item.caaId}_thumb500.jpg",
                        modifier = Modifier
                            .size(300.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentDescription = "Album Poster"
                    ) {
                        it.override(300,300).placeholder(R.drawable.yim_album_placeholder)  // TODO: Mess with size
                    }
                    
                    Spacer(modifier = Modifier.height(5.dp))
                    
                    // Track name
                    Text(
                        text = item.releaseName,
                        modifier = Modifier
                            .padding(horizontal = LocalYimPaddings.current.defaultPadding)
                            .width(290.dp),
                        color = Color(0xFF39296F),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = FontFamily(Font(R.font.roboto_bold))
                    )
                    
                    // Artist text
                    Text(
                        text = item.artistName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF727272)
                    )
                }
    
                if (index == list.lastIndex) {
                    Spacer(modifier = Modifier.width(LocalYimPaddings.current.defaultPadding))
                }else
                    Spacer(modifier = Modifier.width(LocalYimPaddings.current.smallPadding))
                
            }
            
        }
    }
}

/** Album Viewer Using Coil (Here for future if decision shakes)*/
/*
@Composable
fun CoilAlbumViewer(list: List<TopRecording>?) {
    
    // This prevents image from being blur or crashing the app.
   var renderImage by remember { mutableStateOf(false) }
    LaunchedEffect(true){
        delay(2100)
        renderImage = true
    }
    
    val listState = rememberLazyListState()
    
    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            // Animates the grayish background of this window.
            .padding(
                vertical = if (renderImage)
                    LocalYimPaddings.current.extraLargePadding
                else
                    0.dp
            )
            .animateContentSize(),
    ) {
        if (list != null) {
            items(list.toList()) { item ->
                // https://archive.org/download/mbid-{caa_release_mbid}/mbid-{caa_release_mbid}-{caa_id}_thumb500.jpg
                val imagePainter = if (item.caaId != null) {
                    rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("https://archive.org/download/mbid-${item.caaReleaseMbid}/mbid-${item.caaReleaseMbid}-${item.caaId}_thumb250.jpg")
                            .crossfade(true)
                            .placeholder(R.drawable.ic_metabrainz_logo_no_text)
                            .build(),
                        filterQuality = FilterQuality.Medium,
                        error = painterResource(id = R.drawable.ic_metabrainz_logo_no_text),
                    )
                }else {
                    rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(R.drawable.ic_album)
                            .build()
                    )
                }
                
               if (renderImage) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Image(
                            painter = imagePainter,
                            modifier = Modifier
                                .size(300.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Fit,
                            contentDescription = null
                        )
                        Text(text = item.trackName)
                    }
                    Spacer(modifier = Modifier.width(LocalYimPaddings.current.defaultPadding))
               }
               
            }
        }
    }
}*/

