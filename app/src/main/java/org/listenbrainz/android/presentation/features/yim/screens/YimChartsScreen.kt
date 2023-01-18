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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import org.listenbrainz.android.R
import org.listenbrainz.android.presentation.features.yim.YimViewModel
import org.listenbrainz.android.presentation.features.yim.navigation.YimScreens
import org.listenbrainz.android.presentation.features.yim.navigation.YimShareable
import org.listenbrainz.android.presentation.features.yim.screens.components.YimLabelText
import org.listenbrainz.android.presentation.features.yim.screens.components.YimNavigationStation
import org.listenbrainz.android.presentation.features.yim.ui.theme.LocalYimPaddings
import org.listenbrainz.android.presentation.features.yim.ui.theme.YearInMusicTheme
import org.listenbrainz.android.presentation.features.yim.ui.theme.YimPaddings
import org.listenbrainz.android.presentation.theme.lb_purple


@Composable
fun YimChartsScreen(
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
            delay(700)     // Since it takes 700 ms for first list to animate.
            startSecondAnim = true
        }
        
        // Layout starts here
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(state = rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
                YimLabelText(heading = "Charts", subHeading = "These got you through the year. Respect.")
            
                // Top Songs Card
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
                        // Radio Image
                        Image(
                            painter = painterResource(id = R.drawable.yim_radio),
                            modifier = Modifier
                                .padding(top = paddings.defaultPadding)
                                .size(100.dp),
                            contentDescription = null
                        )
            
                        // Heading Text
                        Text(
                            text = "Top Songs of 2022",
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
                            YimTopRecordingsList(paddings, viewModel)
                        }
                        
                    }
                }
            
                // Top Artists Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 0.dp, max = 600.dp)
                        .padding(horizontal = paddings.defaultPadding),
                    shadowElevation = 10.dp,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Map Image
                        Image(
                            painter = painterResource(id = R.drawable.yim_map),
                            modifier = Modifier
                                .padding(top = paddings.defaultPadding)
                                .size(100.dp),
                            contentDescription = null
                        )
        
                        // Heading Text
                        Text(
                            text = "Top Artists of 2022",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = paddings.defaultPadding)
                                .padding(vertical = paddings.smallPadding)
                        )
    
                        AnimatedVisibility(
                            visible = startSecondAnim,
                            enter = expandVertically(animationSpec = tween(700))
                        ) {
                            // List of artists
                            YimTopArtistsList(paddings = paddings, viewModel = viewModel)
                        }
                        
                    }
                }
            
            
            // Share Button and next
            if (startSecondAnim) {
                YimNavigationStation(
                    typeOfImage = arrayOf(YimShareable.ARTISTS, YimShareable.TRACKS),
                    navController = navController,
                    viewModel = viewModel,
                    route = YimScreens.YimStatisticsScreen
                )
            }
            
            
        }
    }
}



@Composable
private fun YimTopRecordingsList(
    paddings: YimPaddings,
    viewModel: YimViewModel
) {
    val list = viewModel.getTopRecordings()!!.toList()
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
        items(list) { item ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = paddings.tinyPadding)
                    .height(55.dp)
                    .background(MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(5.dp),
                shadowElevation = 5.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            vertical = paddings.tinyPadding,
                            horizontal = paddings.smallPadding
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Number of listens Text
                    Column(
                        modifier = Modifier.width(90.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = lb_purple,
                            shape = CircleShape,
                            modifier = Modifier.padding(vertical = paddings.tinyPadding)
                        ) {
                            Text(
                                text = "${item.listenCount} listen${if (item.listenCount != 1) "s" else ""}",
                                modifier = Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical = 4.dp
                                ),
                                style = MaterialTheme.typography.bodyLarge
                                    .copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(paddings.defaultPadding))
                    
                    Column(modifier = Modifier) {
                        Text(
                            text = item.releaseName,
                            style = MaterialTheme.typography.bodyLarge
                                .copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = lb_purple,
                                    lineHeight = 14.sp
                                ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = item.artistName,
                            style = MaterialTheme.typography.bodyMedium
                                .copy(
                                    fontWeight = FontWeight.Bold,
                                    color = lb_purple.copy(alpha = 0.7f)
                                ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                }
            }
            
        }
    }
}


@Composable
private fun YimTopArtistsList(
    paddings: YimPaddings,
    viewModel: YimViewModel
) {
    val list = viewModel.getTopArtists()!!.toList()
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
        itemsIndexed(list) { index, item ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .padding(vertical = paddings.tinyPadding)
                    .background(MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(5.dp),
                shadowElevation = 5.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            vertical = paddings.tinyPadding,
                            horizontal = paddings.smallPadding
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ranking
                    Column(
                        modifier = Modifier.width(35.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "#${index + 1}",
                            modifier = Modifier.padding(horizontal = 5.dp),
                            style = MaterialTheme.typography.bodyMedium
                                .copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = lb_purple
                                )
                        )
                    }
                    
                    
                    // Number of listens Text
                    Column(
                        modifier = Modifier.width(90.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = lb_purple,
                            shape = CircleShape,
                            modifier = Modifier.padding(vertical = paddings.tinyPadding)
                        ) {
                            Text(
                                text = "${item.listenCount} listen${if (item.listenCount != 1) "s" else ""}",
                                modifier = Modifier
                                    .padding(
                                        horizontal = 8.dp,
                                        vertical = 4.dp
                                    ),
                                style = MaterialTheme.typography.bodyMedium
                                    .copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(paddings.smallPadding))
                    
                    Text(
                        text = item.artistName!!,
                        style = MaterialTheme.typography.bodyMedium
                            .copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = lb_purple,
                                lineHeight = 14.sp
                            ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            
        }
    }
}

