package org.listenbrainz.android.ui.screens.yim23

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.listenbrainz.android.model.yimdata.Yim23Screens
import org.listenbrainz.android.ui.components.Yim23Footer
import org.listenbrainz.android.ui.components.Yim23Header
import org.listenbrainz.android.ui.theme.LocalYimPaddings
import org.listenbrainz.android.ui.theme.Yim23Theme
import org.listenbrainz.android.ui.theme.YimPaddings
import org.listenbrainz.android.ui.theme.offWhite
import org.listenbrainz.android.viewmodel.Yim23ViewModel

@Composable
fun Yim23StatsGraphScreen (
    viewModel: Yim23ViewModel,
    navController: NavController
) {
    val username by viewModel.getUsernameFlow().collectAsState(initial = "")
    Yim23Theme(themeType = viewModel.themeType.value) {
        Column (modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onBackground) , verticalArrangement = Arrangement.SpaceBetween) {
            Yim23Header(username = username, navController = navController, upperScreen = Yim23Screens.YimStatsHeatMapScreen)
            Yim23Graph(viewModel = viewModel)
            Yim23Footer(footerText = "MY STATS", isUsername = false, navController = navController, downScreen = Yim23Screens.YimPlaylistsTitleScreen)
        }
    }
}


@Composable
private fun Yim23Graph (viewModel: Yim23ViewModel , paddings: YimPaddings = LocalYimPaddings.current) {
    val listState = rememberLazyListState()
    val graphState = rememberLazyListState()
    LaunchedEffect(graphState.isScrollInProgress){
        listState.animateScrollToItem(index = listState.firstVisibleItemIndex)
    }
    Box (modifier = Modifier
        .fillMaxWidth()
        .height(320.dp)
        .background(Color(0xFFe0e5de)) , contentAlignment = Alignment.TopCenter) {
        LazyRow (state = graphState) {
            items(viewModel.getYearListens().toList()) { item ->
                val height = (item.second * 300) / (viewModel.getMostListenedYear()!!)
                Column {
                    Spacer(modifier = Modifier
                        .width(20.dp)
                        .padding(horizontal = 1.dp)
                        .height(300.dp - height.dp))
                    Spacer(modifier = Modifier
                        .width(20.dp)
                        .padding(horizontal = 1.dp)
                        .height(height.dp)
                        .background(Color.Red))
                }
            }
        }
    }



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
}

