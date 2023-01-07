package org.listenbrainz.android.presentation.features.yim.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.GlideLazyListPreloader
import kotlinx.coroutines.delay
import org.listenbrainz.android.R
import org.listenbrainz.android.presentation.features.yim.YimViewModel
import org.listenbrainz.android.presentation.features.yim.navigation.YimScreens
import org.listenbrainz.android.presentation.features.yim.screens.components.YimLabelText
import org.listenbrainz.android.presentation.features.yim.screens.components.YimNextButton
import org.listenbrainz.android.presentation.features.yim.screens.components.YimShareButton
import org.listenbrainz.android.presentation.features.yim.ui.theme.LocalYimPaddings
import org.listenbrainz.android.presentation.features.yim.ui.theme.YearInMusicTheme
import org.listenbrainz.android.presentation.features.yim.ui.theme.YimPaddings
import org.listenbrainz.android.presentation.theme.lb_purple
import java.text.DecimalFormat

@Composable
fun YimDiscoverScreen(
    yimViewModel: YimViewModel,
    navController: NavController,
    paddings: YimPaddings = LocalYimPaddings.current
){
    YearInMusicTheme(redTheme = false) {
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
        
        // Main Content
        Column(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(state = rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            YimLabelText(heading = "Discover", subHeading = "The year's over, but there's still more to uncover!")
    
            // New Albums from you top artists Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 0.dp, max = 600.dp)
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
                    // Magnifier Image
                    Image(
                        painter = painterResource(id = R.drawable.yim_magnifier),
                        modifier = Modifier
                            .padding(top = paddings.defaultPadding)
                            .size(100.dp),
                        contentDescription = null
                    )
            
                    // Heading Text
                    Text(
                        text = "New albums from top artists",
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
                        YimTopAlbumsFromArtistsList(viewModel = yimViewModel)
                    }
            
                }
            }
            
            // Music Buddies
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 0.dp, max = 600.dp)
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
                    // Buddies Image
                    Image(
                        painter = painterResource(id = R.drawable.yim_buddy),
                        modifier = Modifier
                            .padding(top = paddings.defaultPadding)
                            .size(100.dp),
                        contentDescription = null
                    )
            
                    // Heading Text
                    Text(
                        text = "Music buddies",
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
                        YimSimilarUsersList(yimViewModel = yimViewModel)
                    }
            
                }
            }
    
            // Share Button and next
            if (startSecondAnim) {
                Row(modifier = Modifier.padding(bottom = 50.dp)) {
                    YimShareButton(isRedTheme = false)
                    YimNextButton {
                        navController.navigate(route = YimScreens.YimEndgameScreen.name)
                    }
                }
            }
            
        }
    }
}

@Composable
private fun YimSimilarUsersList(
    yimViewModel: YimViewModel,
    paddings: YimPaddings = LocalYimPaddings.current
) {
    val similarUsers = yimViewModel.getSimilarUsers()
    
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
        itemsIndexed(similarUsers) { index, item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = paddings.tinyPadding),
                shape = RoundedCornerShape(5.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .height(50.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
    
                    Column(
                        modifier = Modifier
                            .fillParentMaxWidth(0.1f)
                            .padding(
                                horizontal = paddings.defaultPadding,
                            )
                    ) {
                        // Ranking
                        Column(
                            modifier = Modifier.fillParentMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "#${index + 1}",
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.bodyMedium
                                    .copy(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = lb_purple
                                    )
                            )
                        }
                    }
                    
                    Column(
                        modifier = Modifier
                            .fillParentMaxWidth(0.55f)
                            .padding(
                                horizontal = paddings.smallPadding
                            )
                    ) {
                        Text(
                            text = item.first,
                            style = MaterialTheme.typography.bodyLarge
                                .copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = lb_purple,
                                    lineHeight = 14.sp
                                ) ,
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillParentMaxWidth(0.35f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = item.second.toFloat(),
                            modifier = Modifier.clip(CircleShape).width(70.dp),
                            color = when (item.second) {
                                in 0.70..1.00 -> Color(0xFF382F6F)
                                in 0.30..0.69 -> Color(0xFFF57542)
                                else -> Color(0xFFD03E43)
                            }
                        )
                        Text(
                            text = "${DecimalFormat("#.#").format(item.second*10)}/10",
                            color = Color.Black.copy(alpha = 0.4f),
                            fontFamily = FontFamily(Font(R.font.roboto_regular)),
                            modifier = Modifier.padding(5.dp),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun YimTopAlbumsFromArtistsList(
    viewModel: YimViewModel,
    paddings: YimPaddings = LocalYimPaddings.current,
) {
    val uriList = arrayListOf<String>()
    val newReleasesOfTopArtist = viewModel.getNewReleasesOfTopArtists()
    
    newReleasesOfTopArtist!!.forEach { item ->
        uriList.add("https://archive.org/download/mbid-${item.caaReleaseMbid}/mbid-${item.caaReleaseMbid}-${item.caaId}_thumb250.jpg")
    }
    
    // Pre-loading images
    val listState = rememberLazyListState()
    GlideLazyListPreloader(
        state = listState,
        data = uriList,
        size = Size(75f,75f),
        numberOfItemsToPreload = 15,
        fixedVisibleItemCount = 5
    ){ item, requestBuilder ->
        requestBuilder.load(item).placeholder(R.drawable.ic_coverartarchive_logo_no_text).override(75)
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
        items(newReleasesOfTopArtist) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = paddings.tinyPadding),
                shape = RoundedCornerShape(5.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    
                    // Album cover art
                    GlideImage(
                        model = "https://archive.org/download/mbid-${item.caaReleaseMbid}/mbid-${item.caaReleaseMbid}-${item.caaId}_thumb250.jpg",
                        modifier = Modifier.size(60.dp),
                        contentScale = ContentScale.Fit,
                        contentDescription = "Album Cover Art"
                    ) {
                        it.placeholder(R.drawable.ic_coverartarchive_logo_no_text)
                            .override(75)
                    }
                    
                    Spacer(modifier = Modifier.width(paddings.defaultPadding))
                    
                    Column(modifier = Modifier) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyLarge
                                .copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = lb_purple,
                                    lineHeight = 14.sp
                                ) ,
                        )
                        Text(
                            text = item.artistCreditName,
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