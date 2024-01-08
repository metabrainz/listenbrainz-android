package org.listenbrainz.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.model.yimdata.YimHeatMapColors
import org.listenbrainz.android.ui.theme.YimPaddings
import org.listenbrainz.android.ui.theme.offWhite

@Composable
fun YimHeatMap (paddings: YimPaddings , listensOfYear : List<Int> , backgroundColor : Color? , heatMapColors: YimHeatMapColors) {
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
        shape = RoundedCornerShape(10.dp),
        color = backgroundColor ?: MaterialTheme.colorScheme.surface
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
                                    item >= 150 -> heatMapColors.greaterThan150
                                    item in 100..149 -> heatMapColors.greaterThan100
                                    item in 50..99 -> heatMapColors.greaterThan50
                                    item in 1..49 -> heatMapColors.greaterThan0
                                    else -> offWhite
                                }
                            ),
                    )

                }
            }

            Row(modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)) {
                HeatMapExampleSquare(listenCount = 1, text = "0"     , heatMapColors = heatMapColors)
                HeatMapExampleSquare(listenCount = 50, text = "50"   , heatMapColors = heatMapColors)
                HeatMapExampleSquare(listenCount = 100, text = "100" , heatMapColors = heatMapColors)
                HeatMapExampleSquare(listenCount = 150, text = "150" , heatMapColors = heatMapColors)
            }

        }
    }
}

@Composable
private fun HeatMapExampleSquare(
    listenCount : Int,
    heatMapColors: YimHeatMapColors,
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
                        listenCount >= 150 -> heatMapColors.greaterThan150
                        listenCount in 100..149 -> heatMapColors.greaterThan100
                        listenCount in 50..99 -> heatMapColors.greaterThan50
                        listenCount in 1..49 -> heatMapColors.greaterThan0
                        else -> offWhite
                    }
                ),
        )
    }
}