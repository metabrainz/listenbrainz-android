package org.listenbrainz.android.presentation.features.yim.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import org.listenbrainz.android.R
import org.listenbrainz.android.presentation.features.yim.YimViewModel
import org.listenbrainz.android.presentation.features.yim.screens.components.YimShareButton
import org.listenbrainz.android.presentation.features.yim.ui.theme.LocalYimPaddings
import org.listenbrainz.android.presentation.features.yim.ui.theme.YearInMusicTheme
import org.listenbrainz.android.presentation.features.yim.ui.theme.YimPaddings
import org.listenbrainz.android.presentation.theme.lb_purple

@RequiresApi(Build.VERSION_CODES.N)
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
            delay(700)     // Since it takes 700 ms for fist list to animate.
            startSecondAnim = true
        }
    
    
        
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            item {
                Text(
                    buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 36.sp,
                                fontFamily = FontFamily(Font(R.font.roboto_bold))
                            )
                        ){
                            append("Charts")
                        }
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 24.sp,
                                fontFamily = FontFamily(Font(R.font.roboto_light))
                            )
                        ){
                            append("\n\nThese got you through the year. Respect.")
                        }
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(paddings.extraLargePadding)
                )
                
            }
            
            // Top Songs Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 0.dp, max = 500.dp)
                        .padding(horizontal = paddings.DefaultPadding),
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
                                .padding(top = paddings.DefaultPadding)
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
                                .padding(vertical = paddings.DefaultPadding)
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
            }
            
            item {
                Spacer(modifier = Modifier.height(50.dp))
            }
            
            // Top Artists Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 0.dp, max = 500.dp)
                        .padding(horizontal = paddings.DefaultPadding),
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
                            painter = painterResource(id = R.drawable.yim_magnifier),
                            modifier = Modifier
                                .padding(top = paddings.DefaultPadding)
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
                                .padding(vertical = paddings.DefaultPadding)
                                .padding(vertical = paddings.smallPadding)
                        )
        
                        if (startSecondAnim){
                            // List of artists
                            YimTopArtistsList(paddings = paddings, viewModel = viewModel)
                        }
                        
                    }
                }
            }
            
            // Share Button
            if (startSecondAnim) {
                item {
                    Spacer(modifier = Modifier.height(50.dp))
                    YimShareButton(isRedTheme = true)
                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
            
        }
    }
}


@RequiresApi(Build.VERSION_CODES.N)
@Composable
private fun YimTopRecordingsList(
    paddings: YimPaddings,
    viewModel: YimViewModel
) {
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
        items(viewModel.getTopRecordings()!!.toList()) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = paddings.tinyPadding),
                shape = RoundedCornerShape(5.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                    horizontal = 7.dp,
                                    vertical = 2.dp
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
                    
                    Spacer(modifier = Modifier.width(paddings.DefaultPadding))
                    Column(modifier = Modifier) {
                        Text(
                            text = item.releaseName,
                            style = MaterialTheme.typography.bodyLarge
                                .copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = lb_purple
                                ) ,
                        )
                        Text(
                            text = item.artistName,
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

@RequiresApi(Build.VERSION_CODES.N)
@Composable
private fun YimTopArtistsList(
    paddings: YimPaddings,
    viewModel: YimViewModel
) {
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
        itemsIndexed(viewModel.getTopArtists()!!.toList()) { index, item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = paddings.tinyPadding),
                shape = RoundedCornerShape(5.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                        horizontal = 7.dp,
                                        vertical = 2.dp
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
                                color = lb_purple
                            )
                    )
                }
            }
            
            
        }
    }
}

