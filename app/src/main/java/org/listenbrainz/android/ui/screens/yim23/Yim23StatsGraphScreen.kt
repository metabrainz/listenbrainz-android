package org.listenbrainz.android.ui.screens.yim23

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.listenbrainz.android.model.yimdata.Yim23Screens
import org.listenbrainz.android.ui.components.YimGraph
import org.listenbrainz.android.viewmodel.Yim23ViewModel

@Composable
fun Yim23StatsGraphScreen (
    viewModel: Yim23ViewModel,
    navController: NavController
) {
    val mostListenedYear = remember { viewModel.getMostListenedYear() }
    val yearListens      = remember {viewModel.getYearListens().toList()}
    Yim23BaseScreen(
        viewModel     = viewModel,
        navController = navController,
        footerText    = "MY STATS",
        isUsername    = true,
        downScreen    = Yim23Screens.YimPlaylistsTitleScreen
    ) {
        Spacer(modifier = Modifier.padding(top = 11.dp))
        Text(
            "Most of the songs I listened to were from ${mostListenedYear!!.key} " +
                    "(${mostListenedYear.value} songs)",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.background,
            textAlign = TextAlign.Center
        )
        YimGraph(yearListens)
    }
}


