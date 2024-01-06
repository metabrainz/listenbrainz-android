package org.listenbrainz.android.ui.screens.yim23

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.listenbrainz.android.model.yimdata.Yim23Screens
import org.listenbrainz.android.ui.theme.LocalYimPaddings
import org.listenbrainz.android.ui.theme.YimPaddings
import org.listenbrainz.android.viewmodel.Yim23ViewModel

@Composable
fun Yim23StatsGraphScreen (
    viewModel: Yim23ViewModel,
    navController: NavController
) {
    val mostListenedYear = remember { viewModel.getMostListenedYear() }
    Yim23BaseScreen(
        viewModel     = viewModel,
        navController = navController,
        footerText    = "MY STATS",
        isUsername    = true,
        downScreen    = Yim23Screens.YimPlaylistsTitleScreen
    ) {
        Spacer(modifier = Modifier.padding(top = 11.dp))
        Text(
            "Most of the songs I listened to were from ${mostListenedYear!!.key} " +
                    "(${mostListenedYear.value} songs)",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.background,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.padding(top = 21.dp))
        Yim23Graph(viewModel = viewModel)
    }
}

@Composable
private fun Yim23Graph (viewModel: Yim23ViewModel , paddings: YimPaddings = LocalYimPaddings.current) {
    val listState   = rememberLazyListState()
    val graphState  = rememberLazyListState()
    val yearListens = remember {viewModel.getYearListens().toList()}

    Box (modifier = Modifier
        .fillMaxWidth()
        .padding(start = 11.dp, end = 11.dp)
        .height(250.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(Color(0xFFe0e5de)) , contentAlignment = Alignment.TopCenter) {
        LazyRow (state = graphState) {
            items(yearListens) { item ->
                val height = (item.second * 250) / (viewModel.getMostListenedYear()!!.value)
                Column {
                    Spacer(modifier = Modifier
                        .width(20.dp)
                        .padding(horizontal = 1.dp)
                        .height(250.dp - height.dp))
                    Spacer(modifier = Modifier
                        .width(20.dp)
                        .padding(horizontal = 1.dp)
                        .padding(top = 25.dp)
                        .height(height.dp)
                        .background(Color(0xFFe36b3c)))
                }
            }
        }
    }
    LaunchedEffect(graphState.isScrollInProgress){
        listState.animateScrollToItem(index = graphState.firstVisibleItemIndex/4)
    }

    LazyRow(
        state = listState,
        modifier = Modifier
            .height(20.dp)
            .padding(start = 11.dp, end = 11.dp),
        userScrollEnabled = false
    ){
        items(listOf("1960" , "1965" , "1970" , "1975" , "1980" , "1985" , "1990" , "1995" , "2000"
            , "2005" , "2010" , "2015" , "2020")
        ){ month ->
            Text(
                text = month,
                modifier = Modifier.width(89.dp),
                textAlign = TextAlign.Center,
                fontSize = 12.sp
            )
        }
    }
}


