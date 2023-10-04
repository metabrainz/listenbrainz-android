package org.listenbrainz.android.ui.screens.yim

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
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
import org.listenbrainz.android.model.yimdata.YimScreens
import org.listenbrainz.android.model.yimdata.YimShareable
import org.listenbrainz.android.ui.components.YimHeadingText
import org.listenbrainz.android.ui.components.YimLabelText
import org.listenbrainz.android.ui.components.YimNavigationStation
import org.listenbrainz.android.ui.theme.LocalYimPaddings
import org.listenbrainz.android.ui.theme.YearInMusicTheme
import org.listenbrainz.android.ui.theme.YimPaddings
import org.listenbrainz.android.ui.theme.offWhite
import org.listenbrainz.android.viewmodel.YimViewModel

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
            .verticalScroll(state = rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .testTag(stringResource(id = R.string.tt_yim_statistics_parent)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Label Text
            YimLabelText(heading = "Statistics", subHeading = "Delicious.")
            
            // Heat map card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
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
                    val listensOfYear = yimViewModel.getListensListOfYear()
                    LazyHorizontalGrid(
                        state = gridState,
                        modifier = Modifier
                            .height(160.dp)
                            .padding(bottom = paddings.smallPadding),
                        rows = GridCells.Fixed(7)
                    ){
                        items( listensOfYear )
                        { item ->
                            
                            // Heatmap square
                            Box(
                                modifier = Modifier
                                    .width(20.dp)
                                    .padding(1.dp)
                                    .background(
                                        when {
                                            item >= 150 -> Color(0xFFF80729)
                                            item in 100..149 -> Color(0xFFE5743E)
                                            item in 50..99 -> Color(0xFFF9CC4E)
                                            item in 1..49 -> Color(0xFFF6E4B3)
                                            else -> offWhite
                                        }
                                    ),
                            )
                            
                        }
                    }
                    
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)) {
                        HeatMapExampleSquare(listenCount = 1, text = "0")
                        HeatMapExampleSquare(listenCount = 50, text = "50")
                        HeatMapExampleSquare(listenCount = 100, text = "100")
                        HeatMapExampleSquare(listenCount = 150, text = "150")
                    }
                    
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
            YimNavigationStation(
                typeOfImage = arrayOf(YimShareable.STATISTICS),
                navController = navController,
                viewModel = yimViewModel,
                modifier = Modifier.padding(vertical = 20.dp),
                route = YimScreens.YimRecommendedPlaylistsScreen
            )
            
        }
    }
}

// TODO: Legend for heat map.
@Composable
private fun HeatMapExampleSquare(
    listenCount : Int,
    text: String
){
    Row(modifier = Modifier
        .height(30.dp)
        .width(60.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            modifier = Modifier.padding(end = if (listenCount > 99) 5.dp else 8.dp)
        )
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(
                    color = when {
                        listenCount >= 150 -> Color(0xFFF80729)
                        listenCount in 100..149 -> Color(0xFFE5743E)
                        listenCount in 50..99 -> Color(0xFFF9CC4E)
                        listenCount in 1..49 -> Color(0xFFF6E4B3)
                        else -> offWhite
                    }
                ),
        )
    }
}
