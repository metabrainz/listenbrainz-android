package org.listenbrainz.android.ui.screens.yim23

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import org.listenbrainz.android.R
import org.listenbrainz.android.model.yimdata.Yim23Screens
import org.listenbrainz.android.model.yimdata.Yim23TopDiscoveries
import org.listenbrainz.android.model.yimdata.Yim23Track
import org.listenbrainz.android.ui.components.Yim23Footer
import org.listenbrainz.android.ui.components.Yim23Header
import org.listenbrainz.android.ui.components.Yim23ShareButton
import org.listenbrainz.android.ui.theme.Yim23Theme
import org.listenbrainz.android.util.Utils
import org.listenbrainz.android.viewmodel.Yim23ViewModel

@Composable
fun Yim23DiscoveriesScreen (
    viewModel: Yim23ViewModel,
    navController: NavController
) {
    val username by viewModel.getUsernameFlow().collectAsState(initial = "")
    Yim23BaseScreen(
        viewModel      = viewModel,
        navController  = navController,
        footerText     = "DISCOVERIES OF 2023",
        isUsername     = false,
        downScreen     = Yim23Screens.YimDiscoveriesListScreen
    ) {
        Row (modifier = Modifier.fillMaxWidth() , horizontalArrangement = Arrangement.Center){
            Box (contentAlignment = Alignment.BottomCenter) {
                Image(painter = painterResource(id = R.drawable.yim23_hug) ,
                    contentDescription = "" , modifier = Modifier
                        .zIndex(1f)
                        .align(
                            Alignment.BottomCenter
                        )
                        .width(550.dp)
                        .height(300.dp)
                        .offset(x = 15.dp))
                Yim23DiscoveriesArt(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
@Composable
private fun Yim23DiscoveriesArt(
    viewModel: Yim23ViewModel
) {
    val yimTopDiscoveries : Yim23TopDiscoveries = remember {viewModel.getTopDiscoveries()}
    val tracks            : List<Yim23Track>    = remember {yimTopDiscoveries.playlist.tracks.toList()}

    Column  {
        for (j in 1..3)
            Row () {
                for(i in 3*j-2..3*j){
                    if(tracks[i-1].extension.extensionData.additionalMetadata.caaReleaseMbid != "" &&
                        tracks[i-1].extension.extensionData.additionalMetadata.caaId != 0L)
                    GlideImage(
                        model = Utils.getCoverArtUrl(
                            caaReleaseMbid = tracks[i-1].extension.extensionData.additionalMetadata.caaReleaseMbid,
                            caaId = tracks[i-1].extension.extensionData.additionalMetadata.caaId.toLong(),
                            size = 250,
                        ),
                        modifier = Modifier
                            .size(80.dp),
                        contentDescription = "Album Poster",
                    )
                    {
                        it.override(300).placeholder(R.drawable.yim_album_placeholder)
                    }
                    else{
                        Image(painter = painterResource(id = R.drawable.yim_album_placeholder) ,
                            contentDescription = "LB logo placeholder" ,
                            modifier = Modifier.size(80.dp))
                    }
                }
            }
        }
    }