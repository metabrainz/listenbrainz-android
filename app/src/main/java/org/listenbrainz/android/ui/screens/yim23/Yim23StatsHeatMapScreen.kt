package org.listenbrainz.android.ui.screens.yim23

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import org.listenbrainz.android.R
import org.listenbrainz.android.model.yimdata.Yim23Screens
import org.listenbrainz.android.ui.components.Yim23Footer
import org.listenbrainz.android.ui.components.Yim23Header
import org.listenbrainz.android.ui.components.YimHeadingText
import org.listenbrainz.android.ui.theme.ColorScheme
import org.listenbrainz.android.ui.theme.LocalYimPaddings
import org.listenbrainz.android.ui.theme.Yim23Theme
import org.listenbrainz.android.ui.theme.YimPaddings
import org.listenbrainz.android.ui.theme.offWhite
import org.listenbrainz.android.viewmodel.Yim23ViewModel

@Composable
fun Yim23StatsHeatMapScreen (
    viewModel: Yim23ViewModel,
    navController: NavController
) {
    val username by viewModel.getUsernameFlow().collectAsState(initial = "")
    val mostListenedMonth : Pair<String , Int> = remember {viewModel.getMostListenedMonth()}
    Yim23Theme(themeType = viewModel.themeType.value) {
        Column (modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onBackground) ,
            verticalArrangement = Arrangement.SpaceBetween ,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Yim23Header(username = username, navController = navController)
            Text("I listened to the most music in ${mostListenedMonth.first}" ,
                textAlign = TextAlign.Center , color = MaterialTheme.colorScheme.background ,
                style = MaterialTheme.typography.bodyLarge ,
                modifier = Modifier.padding(start = 20.dp , end = 20.dp))
            Text("(${mostListenedMonth.second} Songs)" , textAlign = TextAlign.Center ,
                color = MaterialTheme.colorScheme.background ,
                style = MaterialTheme.typography.bodyLarge)
            Yim23HeatMap(viewModel = viewModel)
            Spacer(modifier = Modifier.padding(bottom = 11.dp))
            Yim23Footer(footerText = "MY STATS", isUsername = true, navController = navController,
                downScreen = Yim23Screens.YimStatsGraphScreen)
        }
    }
}

@Composable
private fun Yim23HeatMap (viewModel: Yim23ViewModel , paddings: YimPaddings = LocalYimPaddings.current) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 11.dp, end = 11.dp)
            .clip(RoundedCornerShape(10.dp))
            .height(240.dp)
            .background(Color(0xFFe0e5de))
            .padding(
                start = paddings.defaultPadding,
                end = paddings.defaultPadding,
                bottom = paddings.defaultPadding
            ),

    ) {

        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = paddings.smallPadding),
            verticalArrangement = Arrangement.Center
        ) {

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
            val listensOfYear = remember {viewModel.getListensListOfYear()}
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
                                    item >= 150 -> Color(0xFFE5743E)
                                    item in 100..149 -> Color(0xFF353070)
                                    item in 50..99 -> Color(0xFFbeb6e4)
                                    item in 1..49 -> Color(0xFFeeedf0)
                                    else -> offWhite
                                }
                            ),
                    )

                }
            }


        }
    }

    }




