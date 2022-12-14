package org.listenbrainz.android.presentation.features.yim.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import org.listenbrainz.android.presentation.features.yim.navigation.YimScreens
import org.listenbrainz.android.presentation.features.yim.screens.components.YimHeadingText
import org.listenbrainz.android.presentation.features.yim.screens.components.YimLabelText
import org.listenbrainz.android.presentation.features.yim.screens.components.YimNextButton
import org.listenbrainz.android.presentation.features.yim.screens.components.YimShareButton
import org.listenbrainz.android.presentation.features.yim.ui.theme.LocalYimPaddings
import org.listenbrainz.android.presentation.features.yim.ui.theme.YearInMusicTheme
import org.listenbrainz.android.presentation.features.yim.ui.theme.YimPaddings
import org.listenbrainz.android.presentation.features.yim.ui.theme.yimOffWhite

@Composable
fun YimStatisticsScreen(
    yimViewModel: YimViewModel,
    navController: NavController,
    paddings: YimPaddings = LocalYimPaddings.current
){
    YearInMusicTheme(redTheme = false) {
        var startAnim by remember { mutableStateOf(false) }
        var startSecondAnim by remember { mutableStateOf(false) }
        var startThirdAnim by remember { mutableStateOf(false) }
        
        LaunchedEffect(true){
            delay(1000)
            startAnim = true
            delay(600)
            startSecondAnim = true
            delay(600)
            startThirdAnim = true
        }
        
        
        // Main Content
        Column(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Label Text
            YimLabelText(heading = "Statistics", subHeading = "Delicious.")
            
            // Heat map card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(275.dp)
                    .padding(
                        start = paddings.defaultPadding,
                        end = paddings.defaultPadding,
                        bottom = paddings.defaultPadding
                    ),
                shadowElevation = 10.dp,
                shape = RoundedCornerShape(10.dp)
            ) {
                
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = paddings.smallPadding)
                ) {
                    
                    // Heading text
                    YimHeadingText(text = "Your listening Activity", modifier = Modifier.padding(vertical = paddings.smallPadding))
                    
                    // Month row
                    val listState = rememberLazyListState()
                    LazyRow(
                        state = listState,
                        modifier = Modifier
                            .height(20.dp),
                        userScrollEnabled = false
                    ){
                        items(listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
                        ){ month ->
                            Text(
                                text = month,
                                modifier = Modifier
                                    .width(89.25.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp
                            )
                        }
                    }
                    
                    val gridState = rememberLazyGridState()
                    LaunchedEffect(gridState.isScrollInProgress){
                        listState.animateScrollToItem(index = gridState.firstVisibleItemIndex/26)
                    }
                    // Heat Map Grid
                    LazyHorizontalGrid(
                        state = gridState,
                        modifier = Modifier.padding(bottom = paddings.smallPadding),
                        rows = GridCells.Fixed(7)
                    ){
                        items( yimViewModel.getListensListOfYear() )
                        { item ->
                            
                            // Heatmap square
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(1.dp)
                                    .background(when{
                                        item >= 150 -> Color(0xFFF80729)
                                        item in 100..149 -> Color(0xFFE5743E)
                                        item in 50..99 -> Color(0xFFF9CC4E)
                                        item in 1..49 -> Color(0xFFF6E4B3)
                                        else -> yimOffWhite
                                    }),
                            )
                            
                        }
                    }
                    
                    // TODO: Legend here.
                    
                }
            }
    
            // Total listen count of the year.
            Surface(modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = paddings.defaultPadding,
                    vertical = paddings.defaultPadding
                ),
                shadowElevation = 10.dp,
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddings.defaultPadding)
                ) {
                    Text(text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontFamily = FontFamily(Font(R.font.roboto_light)),
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black,
                                    fontSize = 20.sp
                                )
                            ){
                                append("Your listened to ")
                            }
                            withStyle(
                                style = SpanStyle(
                                    fontFamily = FontFamily(Font(R.font.roboto_light)),
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFFF44336),
                                    fontSize = 20.sp
                                )
                            ){
                                append(yimViewModel.getTotalListenCount().toString())
                            }
                            withStyle(
                                style = SpanStyle(
                                    fontFamily = FontFamily(Font(R.font.roboto_light)),
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black,
                                    fontSize = 20.sp
                                )
                            ){
                                append(" songs this year.")
                            }
                        },
                        textAlign = TextAlign.Center
                    )
                }
                
            }
            
            // Weekday of teh year
            Surface(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = paddings.defaultPadding, vertical = paddings.defaultPadding),
                shadowElevation = 10.dp,
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddings.defaultPadding)
                ) {
                    Text(text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontFamily = FontFamily(Font(R.font.roboto_light)),
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFFF44336),
                                    fontSize = 20.sp
                                )
                            ){
                                append(yimViewModel.getDayOfWeek())
                            }
                            withStyle(
                                style = SpanStyle(
                                    fontFamily = FontFamily(Font(R.font.roboto_light)),
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black,
                                    fontSize = 20.sp
                                )
                            ){
                                append(" was your most active listening day on average.")
                            }
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }
    
           // Share and next button
            Row(modifier = Modifier.padding(top = 30.dp, bottom = 10.dp)) {
                YimShareButton(isRedTheme = false)
                YimNextButton {
                    navController.navigate(route = YimScreens.YimRecommendedPlaylistsScreen.name)
                }
            }
            
        }
    }
}

// TODO: Legend for heat map.
/*
@Composable
private fun HeatMapExampleSquare(
    listenCount : Int,
    text: String
){
    Row(modifier = Modifier.height(20.dp)) {
        Text(text = text)
        Surface(
            modifier = Modifier
                .size(20.dp),
            color = when{
                listenCount >= 150 -> Color(0xFFF80729)
                listenCount in 100..149 -> Color(0xFFE5743E)
                listenCount in 50..99 -> Color(0xFFF9CC4E)
                listenCount in 1..49 -> Color(0xFFF6E4B3)
                else -> yimOffWhite
            },
            content = {}
        )
    }
}
*/