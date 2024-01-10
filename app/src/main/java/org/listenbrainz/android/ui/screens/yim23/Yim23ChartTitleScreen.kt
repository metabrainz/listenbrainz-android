package org.listenbrainz.android.ui.screens.yim23

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.listenbrainz.android.R
import org.listenbrainz.android.model.yimdata.Yim23Screens
import org.listenbrainz.android.viewmodel.Yim23ViewModel

@Composable
fun Yim23ChartTitleScreen (
    viewModel: Yim23ViewModel,
    navController: NavController
) {
    val username by viewModel.getUsernameFlow().collectAsState(initial = "")
    Yim23BaseScreen(
        viewModel     = viewModel,
        navController = navController,
        footerText    = username,
        isUsername    = true,
        downScreen    = Yim23Screens.YimTopAlbumScreen
    ) {
        Yim23AutomaticScroll(navController = navController, time = 3000,
            downScreen = Yim23Screens.YimTopAlbumScreen)
        Box(modifier = Modifier
            .height(400.dp) , contentAlignment = Alignment.BottomCenter){
            Box(){
                Column (modifier = Modifier.height(230.dp) ,
                        verticalArrangement = Arrangement.SpaceBetween) {
                    Row (modifier = Modifier.fillMaxWidth() ,
                         horizontalArrangement = Arrangement.Center) {
                        Text("CHARTS" , style = MaterialTheme.typography.titleLarge ,
                            color = MaterialTheme.colorScheme.background , textAlign = TextAlign.Center)
                    }
                    Box () {
                        val startPadding = 0.36*(LocalConfiguration.current.screenWidthDp)
                        val topPadding = 0.18*(LocalConfiguration.current.screenHeightDp)
                        Column {
                            Row (modifier = Modifier
                                .padding(start = startPadding.dp , top = topPadding.dp)
                                .fillMaxWidth() , horizontalArrangement = Arrangement.SpaceAround ,
                                verticalAlignment = Alignment.CenterVertically) {
                                Image(painter = painterResource(id = R.drawable.yim23_chart_people),
                                    contentDescription = "Yim23 charts icon" , modifier = Modifier
                                        .width(99.dp)
                                        .height(120.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}