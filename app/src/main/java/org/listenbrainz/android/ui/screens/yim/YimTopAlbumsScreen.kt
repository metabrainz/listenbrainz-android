package org.listenbrainz.android.ui.screens.yim

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.coroutines.delay
import org.listenbrainz.android.R
import org.listenbrainz.android.model.yimdata.YimScreens
import org.listenbrainz.android.model.yimdata.TopRelease
import org.listenbrainz.android.ui.components.YimHeadingText
import org.listenbrainz.android.ui.components.YimNavigationStation
import org.listenbrainz.android.ui.theme.LocalYimPaddings
import org.listenbrainz.android.ui.theme.YearInMusicTheme
import org.listenbrainz.android.ui.theme.YimPaddings
import org.listenbrainz.android.util.Utils
import org.listenbrainz.android.viewmodel.YimViewModel


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
            animationSpec = tween(durationMillis = 1000, delayMillis = 1000),
            label = "cardHeight"
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
                        

                        val topReleases : List<TopRelease>? = yimViewModel.getTopReleases()?.toList()
                        
                        YimAlbumViewer(list = topReleases)
                        
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
                    typeOfImage = arrayOf(),//arrayOf(YimShareables.Albums),
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
private fun YimAlbumViewer(list: List<TopRelease>?, listState: LazyListState = rememberLazyListState()) {
    
    // This prevents image from being blur or crashing the app.
    var renderImage by remember { mutableStateOf(false) }
    LaunchedEffect(true){
        delay(2000)
        renderImage = true
    }
    
    // Avoids pop in
    val alphaAnimation by animateFloatAsState(
        targetValue = if (renderImage) 1f else 0f,
        animationSpec = tween(1000),
        label = "alphaAnimation"
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
                
                if (index == 0)
                    Spacer(modifier = Modifier.width(LocalYimPaddings.current.defaultPadding))
                else
                    Spacer(modifier = Modifier.width(LocalYimPaddings.current.tinyPadding))
                
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    
                    GlideImage(
                        model = Utils.getCoverArtUrl(
                            caaReleaseMbid = item.caaReleaseMbid,
                            caaId = item.caaId,
                            size = 500
                        ),
                        modifier = Modifier
                            .size(300.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentDescription = "Album Poster"
                    ) {
                        it.override(300).placeholder(R.drawable.yim_album_placeholder)
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
                
                if (index == list.lastIndex)
                    Spacer(modifier = Modifier.width(LocalYimPaddings.current.defaultPadding))
                else
                    Spacer(modifier = Modifier.width(LocalYimPaddings.current.tinyPadding))
                
            }
        }
    }
}