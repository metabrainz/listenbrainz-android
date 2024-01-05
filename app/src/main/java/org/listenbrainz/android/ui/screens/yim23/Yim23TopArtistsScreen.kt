package org.listenbrainz.android.ui.screens.yim23

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.listenbrainz.android.model.yimdata.TopArtist
import org.listenbrainz.android.model.yimdata.TopRecording
import org.listenbrainz.android.model.yimdata.TopReleaseYim23
import org.listenbrainz.android.model.yimdata.Yim23Screens
import org.listenbrainz.android.ui.components.Yim23Footer
import org.listenbrainz.android.ui.components.Yim23Header
import org.listenbrainz.android.ui.components.Yim23ShareButton
import org.listenbrainz.android.ui.theme.Yim23Theme
import org.listenbrainz.android.viewmodel.Yim23ViewModel

@Composable
fun Yim23TopArtistsScreen (
    viewModel: Yim23ViewModel,
    navController: NavController
) {
    val username by viewModel.getUsernameFlow().collectAsState(initial = "")
    Yim23Theme(themeType = viewModel.themeType.value) {
        Column (modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onBackground) ,
            verticalArrangement = Arrangement.SpaceBetween)
        {
            Yim23Header(username = username, navController = navController)
            Box(contentAlignment = Alignment.BottomCenter , modifier = Modifier.padding(top = 5.dp)) {
                Column (verticalArrangement = Arrangement.spacedBy(0.dp) ,
                    horizontalAlignment = Alignment.Start , modifier = Modifier.fillMaxWidth()) {
                    val topArtists : List<TopArtist>? = remember {
                        viewModel.getTopArtists()!!.toList()
                    }
                    for(i in 1..10)
                        Row () {
                            if(i == 10) Text("X" , style = MaterialTheme.typography.labelMedium ,
                                color = MaterialTheme.colorScheme.background ,
                                modifier = Modifier.padding(start = 11.dp,end = 21.dp))
                            else Text((i).toString() ,  style = MaterialTheme.typography.labelMedium ,
                                color = MaterialTheme.colorScheme.background ,
                                modifier = Modifier.padding(start = 11.dp,end = 21.dp))
                            Text(topArtists!![i-1].artistName!!,
                                style = MaterialTheme.typography.labelMedium ,
                                color = MaterialTheme.colorScheme.background ,
                                maxLines = 1 , overflow = TextOverflow.Clip)
                        }
                }
            }


            Spacer(modifier = Modifier.padding(vertical = 11.dp))
            Yim23Footer(footerText = "MY TOP SONGS", isUsername = false,
                navController = navController, downScreen = Yim23Screens.YimStatsTitleScreen)
        }
    }

}