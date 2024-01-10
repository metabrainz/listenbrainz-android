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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.listenbrainz.android.model.yimdata.Yim23Screens
import org.listenbrainz.android.ui.components.Yim23Footer
import org.listenbrainz.android.ui.components.Yim23Header
import org.listenbrainz.android.ui.theme.Yim23Theme
import org.listenbrainz.android.viewmodel.Yim23ViewModel
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.components.Yim23ShareButton


@Composable
fun Yim23PlaylistTitleScreen (
    viewModel: Yim23ViewModel,
    navController: NavController
) {
    val username by viewModel.getUsernameFlow().collectAsState(initial = "")
    Yim23BaseScreen(
        viewModel     = viewModel,
        navController = navController,
        footerText    = username,
        isUsername    = true,
        downScreen    = Yim23Screens.YimDiscoveriesScreen
    ) {
        Yim23AutomaticScroll(navController = navController, time = 3000,
            downScreen = Yim23Screens.YimDiscoveriesScreen)
        Box (modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)){
            Image(painter = painterResource(id = R.drawable.yim23_ghost),
                contentDescription = "" , modifier = Modifier
                    .width(97.dp)
                    .height(103.dp)
                    .align(
                        Alignment.TopStart
                    ))
            Text("PLAYLISTS" , style = MaterialTheme.typography.titleLarge ,
                color = MaterialTheme.colorScheme.background , modifier = Modifier.align(
                    Alignment.Center))
        }
    }
}