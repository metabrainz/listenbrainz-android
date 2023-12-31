package org.listenbrainz.android.ui.screens.yim23

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.listenbrainz.android.model.yimdata.TopReleaseYim23
import org.listenbrainz.android.model.yimdata.Yim23Screens
import org.listenbrainz.android.ui.components.Yim23Footer
import org.listenbrainz.android.ui.components.Yim23Header
import org.listenbrainz.android.ui.components.Yim23ShareButton
import org.listenbrainz.android.ui.theme.Yim23Theme
import org.listenbrainz.android.viewmodel.Yim23ViewModel


@Composable
fun Yim23AlbumsListScreen (
    viewModel: Yim23ViewModel,
    navController: NavController
) {
    val username by viewModel.getUsernameFlow().collectAsState(initial = "")
    Yim23Theme(themeType = viewModel.themeType.value) {
        Column (modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onBackground),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Yim23Header(username = username, navController = navController, upperScreen = Yim23Screens.YimTopAlbumScreen)
            Box(contentAlignment = Alignment.BottomCenter) {
                Column (verticalArrangement = Arrangement.spacedBy(-30.dp) , horizontalAlignment = Alignment.Start , modifier = Modifier.fillMaxWidth()) {
                    val topReleases : List<TopReleaseYim23>? = viewModel.getTopReleases()?.toList()
                    for(i in 1..5)
                        Text(topReleases!![i].releaseName ,  color = MaterialTheme.colorScheme.background , style = MaterialTheme.typography.labelLarge , maxLines = 1)
                }
                Yim23ShareButton()
            }

            Yim23Footer(footerText = "MY TOP ALBUMS", isUsername = false, navController = navController, downScreen = Yim23Screens.YimTopSongsScreen)
        }
    }
}