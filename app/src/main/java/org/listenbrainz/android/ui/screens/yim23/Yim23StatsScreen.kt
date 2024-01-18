package org.listenbrainz.android.ui.screens.yim23

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.listenbrainz.android.R
import org.listenbrainz.android.model.yimdata.TopGenre
import org.listenbrainz.android.model.yimdata.Yim23Screens
import org.listenbrainz.android.model.yimdata.YimShareable
import org.listenbrainz.android.viewmodel.Yim23ViewModel

@Composable
fun Yim23StatsScreen (
    viewModel: Yim23ViewModel,
    navController: NavController
) {
    Yim23BaseScreen(
        viewModel     = viewModel,
        navController = navController,
        footerText    = "MY STATS",
        shareable     = YimShareable.STATISTICS,
        isUsername    = true,
        downScreen    = Yim23Screens.YimStatsHeatMapScreen
    ) {
        Yim23Stats(viewModel = viewModel)
    }
}

@Composable
private fun Yim23Stats (viewModel: Yim23ViewModel) {
    val topGenre         : TopGenre     = remember {viewModel.getTopGenres()[0]}
    val musicDay         : String       = remember {viewModel.getDayOfWeek().substring(0,3)}
    val totalArtists     : Int?     =     remember {viewModel.getTotalArtistCount()}
    val totalSongs       : Int?       =   remember {viewModel.getTotalListenCount()}
    val newArtisis       : Int? =         remember {viewModel.getTotalNewArtistsDiscovered()}
    val newArtistPercent : Int? =         remember {(newArtisis!! * 100/totalArtists!!)}
    val totaldays        : Double? =      remember {viewModel.getTotalListeningTime()!!/(60*60*24)}
    Row (modifier = Modifier
        .padding(start = 21.dp, end = 21.dp, top = 11.dp)
        .fillMaxWidth() , horizontalArrangement = Arrangement.SpaceBetween) {
        Column (horizontalAlignment = Alignment.CenterHorizontally ,
            modifier = Modifier.width(160.dp)) {
            Text(totalSongs!!.toString() , style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily(Font(R.font.roboto_bold)))  ,
                color = MaterialTheme.colorScheme.background)
            Divider(color = MaterialTheme.colorScheme.background , modifier = Modifier
                .width(80.dp)
                .padding(top = 5.dp, bottom = 5.dp))
            Text("songs    played" , textAlign = TextAlign.Center ,
                style = MaterialTheme.typography.bodyMedium ,
                color = MaterialTheme.colorScheme.background)
        }
        Column (horizontalAlignment = Alignment.CenterHorizontally ,
            modifier = Modifier.width(160.dp)) {
            Text(topGenre.genre.replaceFirstChar { char -> char.uppercase() } , style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily(Font(R.font.roboto_bold))) ,
                color = MaterialTheme.colorScheme.background)
            Divider(color = MaterialTheme.colorScheme.background , modifier = Modifier
                .width(80.dp)
                .padding(top = 5.dp, bottom = 5.dp))
            Text("was my\ntop genre" , textAlign = TextAlign.Center ,
                style = MaterialTheme.typography.bodyMedium ,
                color = MaterialTheme.colorScheme.background)
        }
    }
    Row (modifier = Modifier
        .padding(start = 21.dp, end = 21.dp, top = 11.dp)
        .fillMaxWidth() , horizontalArrangement = Arrangement.SpaceBetween) {
        Column (horizontalAlignment = Alignment.CenterHorizontally ,
            modifier = Modifier.width(160.dp)) {
            Text(totaldays!!.toInt().toString() , style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily(Font(R.font.roboto_bold))) ,
                color = MaterialTheme.colorScheme.background)
            Divider(color = MaterialTheme.colorScheme.background , modifier = Modifier
                .width(80.dp)
                .padding(top = 5.dp, bottom = 5.dp))
            Text("days\n(at least!)" , textAlign = TextAlign.Center ,
                style = MaterialTheme.typography.bodyMedium ,
                color = MaterialTheme.colorScheme.background)
        }
        Column (horizontalAlignment = Alignment.CenterHorizontally ,
            modifier = Modifier.width(160.dp)) {
            Text(musicDay , style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily(Font(R.font.roboto_bold))) ,
                color = MaterialTheme.colorScheme.background)
            Divider(color = MaterialTheme.colorScheme.background , modifier = Modifier
                .width(80.dp)
                .padding(top = 5.dp, bottom = 5.dp))
            Text("was my music day" , textAlign = TextAlign.Center ,
                style = MaterialTheme.typography.bodyMedium ,
                color = MaterialTheme.colorScheme.background)
        }
    }
    Row (modifier = Modifier
        .padding(start = 21.dp, end = 21.dp, top = 11.dp)
        .fillMaxWidth() , horizontalArrangement = Arrangement.SpaceBetween ,
        verticalAlignment = Alignment.CenterVertically) {
        Column (horizontalAlignment = Alignment.CenterHorizontally ,
            modifier = Modifier.width(160.dp)) {
            Text(newArtistPercent!!.toString() + "%" ,
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily(Font(R.font.roboto_bold))) ,
                color = MaterialTheme.colorScheme.background)
            Divider(color = MaterialTheme.colorScheme.background , modifier = Modifier
                .width(80.dp)
                .padding(top = 5.dp, bottom = 5.dp))
            Text("new artists discovered" , textAlign = TextAlign.Center ,
                style = MaterialTheme.typography.bodyMedium ,
                color = MaterialTheme.colorScheme.background)
        }
        Column (horizontalAlignment = Alignment.CenterHorizontally ,
            modifier = Modifier.width(160.dp)) {
            Text(totalArtists!!.toString() , style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily(Font(R.font.roboto_bold))) ,
                color = MaterialTheme.colorScheme.background)
            Divider(color = MaterialTheme.colorScheme.background , modifier = Modifier
                .width(80.dp)
                .padding(top = 5.dp, bottom = 5.dp))
            Text("different artists" , textAlign = TextAlign.Center ,
                style = MaterialTheme.typography.bodyMedium ,
                color = MaterialTheme.colorScheme.background)
        }
    }
}