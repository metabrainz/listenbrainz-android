package org.listenbrainz.android.ui.screens.yim23

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.listenbrainz.android.model.yimdata.TopGenre
import org.listenbrainz.android.model.yimdata.Yim23Screens
import org.listenbrainz.android.ui.components.Yim23Footer
import org.listenbrainz.android.ui.components.Yim23Header
import org.listenbrainz.android.ui.components.Yim23ShareButton
import org.listenbrainz.android.ui.theme.Yim23Theme
import org.listenbrainz.android.viewmodel.Yim23ViewModel

@Composable
fun Yim23StatsScreen (
    viewModel: Yim23ViewModel,
    navController: NavController
) {
    val username by viewModel.getUsernameFlow().collectAsState(initial = "")
    Yim23Theme(themeType = viewModel.themeType.value) {
        Column (modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onBackground) , verticalArrangement = Arrangement.SpaceBetween) {
            Yim23Header(username = username, navController = navController)
            Yim23Stats(viewModel = viewModel)
            Spacer(modifier = Modifier.padding(bottom = 20.dp))
            Yim23Footer(footerText = "MY        STATS", isUsername = false , navController = navController, downScreen = Yim23Screens.YimStatsHeatMapScreen)
        }
    }
}

@Composable
private fun Yim23Stats (viewModel: Yim23ViewModel) {
    val topGenre : TopGenre = viewModel.getTopGenres()[0]
    val musicDay : String = viewModel.getDayOfWeek().substring(0,3)
    val totalArtists : Int? = viewModel.getTotalArtistCount()
    val totalSongs : Int?  = viewModel.getTotalListenCount()
    val newArtisis : Int? = viewModel.getTotalNewArtistsDiscovered()
    val newArtistPercent : Int? = (newArtisis!! * 100/totalArtists!!)
    val totaldays : Double? = viewModel.getTotalListeningTime()!!/(60*60*24)
    Row (modifier = Modifier
        .padding(start = 11.dp, end = 11.dp , top = 11.dp)
        .fillMaxWidth() , horizontalArrangement = Arrangement.SpaceBetween) {
        Column (horizontalAlignment = Alignment.CenterHorizontally ,modifier = Modifier.width(160.dp)) {
            Text(totalSongs!!.toString() , style = MaterialTheme.typography.bodyLarge , color = MaterialTheme.colorScheme.background)
            Divider(color = MaterialTheme.colorScheme.background , modifier = Modifier.width(80.dp).padding(top = 5.dp , bottom = 5.dp))
            Text("songs    played" , textAlign = TextAlign.Center , style = MaterialTheme.typography.bodyMedium ,  color = MaterialTheme.colorScheme.background)
        }
        Column (horizontalAlignment = Alignment.CenterHorizontally , modifier = Modifier.width(160.dp)) {
            Text(topGenre.genre , style = MaterialTheme.typography.bodyLarge , color = MaterialTheme.colorScheme.background)
            Divider(color = MaterialTheme.colorScheme.background , modifier = Modifier.width(80.dp).padding(top = 5.dp , bottom = 5.dp))
            Text("was my top genre" , textAlign = TextAlign.Center , style = MaterialTheme.typography.bodyMedium ,  color = MaterialTheme.colorScheme.background)
        }
    }
    Row (modifier = Modifier
        .padding(start = 11.dp, end = 11.dp , top = 11.dp)
        .fillMaxWidth() , horizontalArrangement = Arrangement.SpaceBetween) {
        Column (horizontalAlignment = Alignment.CenterHorizontally , modifier = Modifier.width(160.dp)) {
            Text(totaldays!!.toInt().toString() , style = MaterialTheme.typography.bodyLarge , color = MaterialTheme.colorScheme.background)
            Divider(color = MaterialTheme.colorScheme.background , modifier = Modifier.width(80.dp).padding(top = 5.dp , bottom = 5.dp))
            Text("days (atleast!)" , textAlign = TextAlign.Center , style = MaterialTheme.typography.bodyMedium ,  color = MaterialTheme.colorScheme.background)
        }
        Column (horizontalAlignment = Alignment.CenterHorizontally , modifier = Modifier.width(160.dp)) {
            Text(musicDay , style = MaterialTheme.typography.bodyLarge , color = MaterialTheme.colorScheme.background)
            Divider(color = MaterialTheme.colorScheme.background , modifier = Modifier.width(80.dp).padding(top = 5.dp , bottom = 5.dp))
            Text("was my music day" , textAlign = TextAlign.Center , style = MaterialTheme.typography.bodyMedium ,  color = MaterialTheme.colorScheme.background)
        }
    }
    Row (modifier = Modifier
        .padding(start = 11.dp, end = 11.dp , top = 11.dp)
        .fillMaxWidth() , horizontalArrangement = Arrangement.SpaceBetween , verticalAlignment = Alignment.CenterVertically) {
        Column (horizontalAlignment = Alignment.CenterHorizontally , modifier = Modifier.width(160.dp)) {
            Text(newArtistPercent!!.toString() + "%" , style = MaterialTheme.typography.bodyLarge , color = MaterialTheme.colorScheme.background)
            Divider(color = MaterialTheme.colorScheme.background , modifier = Modifier.width(80.dp).padding(top = 5.dp , bottom = 5.dp))
            Text("new artists discovered" , textAlign = TextAlign.Center , style = MaterialTheme.typography.bodyMedium ,  color = MaterialTheme.colorScheme.background)
        }
        Column (horizontalAlignment = Alignment.CenterHorizontally , modifier = Modifier.width(160.dp)) {
            Text(totalArtists!!.toString() , style = MaterialTheme.typography.bodyLarge , color = MaterialTheme.colorScheme.background)
            Divider(color = MaterialTheme.colorScheme.background , modifier = Modifier.width(80.dp).padding(top = 5.dp , bottom = 5.dp))
            Text("different artists" , textAlign = TextAlign.Center , style = MaterialTheme.typography.bodyMedium ,  color = MaterialTheme.colorScheme.background)
        }
    }
}