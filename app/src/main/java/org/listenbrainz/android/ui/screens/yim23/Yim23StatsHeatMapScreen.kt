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
import org.listenbrainz.android.model.yimdata.YimHeatMapColors
import org.listenbrainz.android.ui.components.Yim23Footer
import org.listenbrainz.android.ui.components.Yim23Header
import org.listenbrainz.android.ui.components.YimHeadingText
import org.listenbrainz.android.ui.components.YimHeatMap
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
    val mostListenedMonth : Pair<String , Int> = remember {viewModel.getMostListenedMonth()}
    val yim23HeatMapColors : YimHeatMapColors = YimHeatMapColors(
        greaterThan150 = Color(0xFFE5743E),
        greaterThan100 = Color(0xFF353070),
        greaterThan50 = Color(0xFFbeb6e4),
        greaterThan0 = Color(0xFFeeedf0)
    )

    Yim23BaseScreen(
        viewModel     = viewModel,
        navController = navController,
        footerText    = "MY STATS",
        isUsername    = true,
        downScreen    = Yim23Screens.YimStatsGraphScreen
    ) {
        Text("I listened to the most music in ${mostListenedMonth.first} (${mostListenedMonth.second} Songs)" ,
            textAlign = TextAlign.Center , color = MaterialTheme.colorScheme.background ,
            style = MaterialTheme.typography.bodyLarge ,
            modifier = Modifier.padding(start = 20.dp , end = 20.dp))
        YimHeatMap(paddings = YimPaddings(), listensOfYear = viewModel.getListensListOfYear() , backgroundColor = Color(0xFFe0e5de) , heatMapColors = yim23HeatMapColors)
        Spacer(modifier = Modifier.padding(bottom = 11.dp))
    }
}