package org.listenbrainz.android.ui.screens.yim23


import android.text.TextUtils.TruncateAt
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.listenbrainz.android.model.yimdata.TopReleaseYim23
import org.listenbrainz.android.model.yimdata.Yim23Screens
import org.listenbrainz.android.viewmodel.Yim23ViewModel


@Composable
fun Yim23AlbumsListScreen (
    viewModel: Yim23ViewModel,
    navController: NavController
) {
    Yim23BaseScreen(
        viewModel     = viewModel,
        navController = navController,
        footerText    = "MY TOP ALBUMS",
        isUsername    = false,
        downScreen    = Yim23Screens.YimTopSongsScreen
    ) {
        Box(contentAlignment = Alignment.BottomCenter) {
            Column (verticalArrangement = Arrangement.spacedBy(-30.dp) ,
                horizontalAlignment = Alignment.Start , modifier = Modifier.fillMaxWidth()) {
                val topReleases : List<TopReleaseYim23> = remember {
                    viewModel.getTopReleases() ?: listOf()
                }
                for(i in 1..5)
                    Text(topReleases[i-1].releaseName.uppercase() ,
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState() , false),
                        color = MaterialTheme.colorScheme.background ,
                        overflow = TextOverflow.Visible,
                        style = MaterialTheme.typography.labelLarge , maxLines = 1)
            }
        }
    }
}