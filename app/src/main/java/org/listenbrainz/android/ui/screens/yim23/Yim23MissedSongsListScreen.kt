package org.listenbrainz.android.ui.screens.yim23

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.listenbrainz.android.model.yimdata.Yim23Screens
import org.listenbrainz.android.model.yimdata.Yim23TopDiscoveries
import org.listenbrainz.android.model.yimdata.Yim23Track
import org.listenbrainz.android.ui.components.Yim23Footer
import org.listenbrainz.android.ui.components.Yim23Header
import org.listenbrainz.android.ui.components.Yim23ShareButton
import org.listenbrainz.android.ui.components.YimListenCard
import org.listenbrainz.android.ui.theme.Yim23Theme
import org.listenbrainz.android.util.Utils
import org.listenbrainz.android.viewmodel.Yim23ViewModel

@Composable
fun Yim23MissedSongsListScreen (
    viewModel: Yim23ViewModel,
    navController: NavController
) {
    val username by viewModel.getUsernameFlow().collectAsState(initial = "")
    Yim23BaseScreen(
        viewModel     = viewModel,
        navController = navController,
        footerText    = "MISSED SONGS 2023",
        isUsername    = false,
        downScreen    = Yim23Screens.YimDiscoverTitleScreen
    ) {
        Yim23MissedSongs(viewModel = viewModel)
    }
}


@Composable
private fun Yim23MissedSongs (viewModel: Yim23ViewModel) {
    val topMissedSongs : List<Yim23Track> = remember {viewModel.getMissedSongs().playlist.tracks.toList()}
    Box (modifier = Modifier
        .fillMaxWidth()
        .padding(start = 11.dp, end = 11.dp)
        .clip(
            RoundedCornerShape(10.dp)
        )
        .height(300.dp)
        .background(
            Color(0xFFe0e5de)
        )
    ) {
        LazyColumn (state = rememberLazyListState()) {
            items(topMissedSongs) {
                YimListenCard(releaseName = it.title, artistName = it.creator, coverArtUrl =
                Utils.getCoverArtUrl(
                    caaId = it.extension.extensionData.additionalMetadata.caaId.toLong(),
                    caaReleaseMbid = it.extension.extensionData.additionalMetadata.caaReleaseMbid,
                    size = 500),)
            }
        }
    }
}