package org.listenbrainz.android.ui.screens.yim23

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import org.listenbrainz.android.model.yimdata.Yim23Screens
import org.listenbrainz.android.viewmodel.Yim23ViewModel

@Composable
fun Yim23StatsTitleScreen (
    viewModel: Yim23ViewModel,
    navController: NavController
) {
    val username by viewModel.getUsernameFlow().collectAsState(initial = "")
    Yim23BaseScreen(
        viewModel     = viewModel,
        navController = navController,
        footerText    = username,
        isUsername    = true,
        downScreen    = Yim23Screens.YimStatsScreen
    ) {
        Yim23AutomaticScroll(navController = navController, time = 3000,
            downScreen = Yim23Screens.YimStatsScreen)
        Row (modifier = Modifier.fillMaxWidth() , horizontalArrangement = Arrangement.Center) {
            Text("STATS" , style = MaterialTheme.typography.titleLarge ,
                color = MaterialTheme.colorScheme.background)
        }
    }
}