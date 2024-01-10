package org.listenbrainz.android.ui.screens.yim23

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.listenbrainz.android.model.yimdata.TopRecording
import org.listenbrainz.android.model.yimdata.Yim23Screens
import org.listenbrainz.android.viewmodel.Yim23ViewModel

@Composable
fun Yim23TopSongsScreen (
    viewModel: Yim23ViewModel,
    navController: NavController
) {
    val username by viewModel.getUsernameFlow().collectAsState(initial = "")
    Yim23BaseScreen(
        viewModel     = viewModel,
        navController = navController,
        footerText    = "MY TOP SONGS",
        isUsername    = false,
        downScreen    = Yim23Screens.YimStatsTitleScreen
    ) {
        Box(contentAlignment = Alignment.BottomCenter , modifier = Modifier.padding(top = 5.dp)) {
            Column (verticalArrangement = Arrangement.spacedBy(0.dp) ,
                horizontalAlignment = Alignment.Start , modifier = Modifier.fillMaxWidth()) {
                val topRecordings : List<TopRecording>? = remember {
                    viewModel.getTopRecordings() ?: listOf()
                }
                for(i in 1..10)
                    Row () {
                        if(i == 10) Text("X" , style = MaterialTheme.typography.labelMedium
                            , color = MaterialTheme.colorScheme.background ,
                            modifier = Modifier.padding(start = 11.dp,end = 21.dp))
                        else Text((i).toString() ,  style = MaterialTheme.typography.labelMedium
                            , color = MaterialTheme.colorScheme.background ,
                            modifier = Modifier.padding(start = 11.dp,end = 21.dp))
                        Text(topRecordings!![i-1].trackName.uppercase(),
                            modifier = Modifier.horizontalScroll(rememberScrollState() , false),
                            style = MaterialTheme.typography.labelMedium ,
                            color = MaterialTheme.colorScheme.background , maxLines = 1 ,
                            overflow = TextOverflow.Clip)
                    }
            }
        }
    }
}