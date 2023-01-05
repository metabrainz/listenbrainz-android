package org.listenbrainz.android.presentation.features.yim.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.ComponentRegistry
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.GlideLazyListPreloader
import kotlinx.coroutines.delay
import okhttp3.*
import org.listenbrainz.android.R
import org.listenbrainz.android.data.sources.api.entities.yimdata.NewReleasesOfTopArtist
import org.listenbrainz.android.data.sources.api.entities.yimdata.TopRelease
import org.listenbrainz.android.presentation.features.yim.YimViewModel
import org.listenbrainz.android.presentation.features.yim.screens.components.YimLabelText
import org.listenbrainz.android.presentation.features.yim.ui.theme.LocalYimPaddings
import org.listenbrainz.android.presentation.features.yim.ui.theme.YearInMusicTheme
import org.listenbrainz.android.presentation.features.yim.ui.theme.YimPaddings


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun YimRecommendedPlaylistsScreen(
    viewModel: YimViewModel,
    navController: NavController,
    paddings: YimPaddings = LocalYimPaddings.current,
    context: Context = LocalContext.current
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
    
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 0.dp, max = 550.dp)
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
                        // Collage of albums
                        Box(
                            modifier = Modifier.size(300.dp)
                        ){
                        
                        }
                
                        // Heading Text
                        Text(
                            text = "Top discoveries of 2022",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = paddings.defaultPadding)
                                .padding(vertical = paddings.smallPadding)
                        )
                
                        AnimatedVisibility(
                            visible = startAnim,
                            enter = expandVertically(animationSpec = tween(700))
                        ) {
                            // List of songs
                            YimTopDiscoveriesList(paddings, viewModel)
                        }
                
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun PreloadImages(viewModel: YimViewModel){
    val uriList : ArrayList<String> = arrayListOf()
    val topReleases : List<NewReleasesOfTopArtist>? = viewModel.getNewReleasesOfTopArtists()?.toList()
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
        requestBuilder.load(item).placeholder(R.drawable.ic_coverartarchive_logo_no_text)
    }
}

@Composable
private fun YimTopDiscoveriesList(
    paddings: YimPaddings,
    viewModel: YimViewModel
) {

}/*val imageLoader = ImageLoader.Builder(context)
                            .components(fun ComponentRegistry.Builder.() {
                                add(SvgDecoder.Factory())
                            })
                            .build()
                        
                        val request = ImageRequest.Builder(LocalContext.current)
                            .crossfade(true)
                            .crossfade(500)
                            .data("https://api.listenbrainz.org/1/art/year-in-music/2022/lucifer?image=discovery-playlist.svg")
                            .build()
                        
                        imageLoader.enqueue(request)
                        
                        val imagePainter = rememberAsyncImagePainter(
                                model = request,
                                filterQuality = FilterQuality.Medium,
                                error = painterResource(id = R.drawable.ic_metabrainz_logo_no_text),
                            )
                        
                        Image(
                            painter = imagePainter,
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().height(400.dp)
                        )*/
