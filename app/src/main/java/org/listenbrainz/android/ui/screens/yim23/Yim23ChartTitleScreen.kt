package org.listenbrainz.android.ui.screens.yim23

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.listenbrainz.android.R
import org.listenbrainz.android.model.yimdata.Yim23Screens
import org.listenbrainz.android.ui.components.Yim23Footer
import org.listenbrainz.android.ui.components.Yim23ShareButton
import org.listenbrainz.android.ui.theme.Yim23Theme
import org.listenbrainz.android.viewmodel.Yim23ViewModel
import org.listenbrainz.android.ui.components.Yim23Header

@Composable
fun Yim23ChartTitleScreen (
    viewModel: Yim23ViewModel,
    navController: NavController
) {
    val username by viewModel.getUsernameFlow().collectAsState(initial = "")
    Yim23Theme(themeType = viewModel.themeType.value) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onBackground)){
            Column (modifier = Modifier.fillMaxSize() , verticalArrangement = Arrangement.SpaceBetween) {
                Yim23Header(username = username, navController = navController , upperScreen = Yim23Screens.YimLandingScreen)
                Row (modifier = Modifier.fillMaxWidth() , horizontalArrangement = Arrangement.Center) {
                    Text("CHARTS" , style = MaterialTheme.typography.titleLarge , color = MaterialTheme.colorScheme.background , textAlign = TextAlign.Center)
                }
                Box () {
                    val padding = 0.36*(LocalConfiguration.current.screenWidthDp)
                    Column {
                        Row (modifier = Modifier
                            .padding(start = padding.dp)
                            .fillMaxWidth() , horizontalArrangement = Arrangement.SpaceAround , verticalAlignment = Alignment.CenterVertically) {
                            Yim23ShareButton()
                            Image(painter = painterResource(id = R.drawable.yim23_chart_people), contentDescription = "Yim23 charts icon" , modifier = Modifier
                                .width(99.dp)
                                .height(120.dp))
                        }
                        Yim23Footer(footerText = username, navController = navController, isUsername = true, downScreen = Yim23Screens.YimTopAlbumScreen)
                    }
                }



            }
        }
    }
}