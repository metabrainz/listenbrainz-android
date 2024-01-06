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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import org.listenbrainz.android.R
import org.listenbrainz.android.model.yimdata.TopReleaseYim23
import org.listenbrainz.android.model.yimdata.Yim23Screens
import org.listenbrainz.android.ui.components.Yim23Footer
import org.listenbrainz.android.ui.components.Yim23Header
import org.listenbrainz.android.ui.components.Yim23ShareButton
import org.listenbrainz.android.ui.theme.Yim23Theme
import org.listenbrainz.android.util.Utils
import org.listenbrainz.android.viewmodel.Yim23ViewModel


@Composable
fun Yim23TopAlbumsScreen (
    viewModel: Yim23ViewModel,
    navController: NavController,
) {
    val topReleases : List<TopReleaseYim23>? = remember {
        viewModel.getTopReleases()?.toList()
    }
    Yim23BaseScreen(
        viewModel     = viewModel,
        navController = navController,
        footerText    = "MY TOP ALBUMS",
        isUsername    = false,
        downScreen    = Yim23Screens.YimAlbumsListScreen,
    ){
        Row (modifier = Modifier.fillMaxWidth() , horizontalArrangement = Arrangement.Center) {
            Box  {
                Image(painter = painterResource(id = R.drawable.yim23_left_curtain),
                    contentDescription = "Left curtain" ,
                    Modifier
                        .width(200.dp)
                        .height(275.dp)
                        .offset(x = -75.dp, y = -10.dp)
                        .zIndex(1f), alignment = Alignment.TopStart)
                AlbumCoverPic(topReleases)
                Image(painter = painterResource(id = R.drawable.yim23_right_curtain),
                    contentDescription = "Right curtain" ,
                    Modifier
                        .width(200.dp)
                        .height(275.dp)
                        .offset(x = 115.dp, y = -10.dp)
                        .zIndex(1f) , alignment = Alignment.TopEnd)
            }
        }
    }
}
@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
@Composable
private fun AlbumCoverPic (list: List<TopReleaseYim23>?) {
    Column  {
        for (j in 1..3)
            Row () {
                for(i in 3*j-2..3*j){
                    GlideImage(
                        model = Utils.getCoverArtUrl(
                            caaReleaseMbid = list!![i-1].caaReleaseMbid,
                            caaId = list[i-1].caaId,
                            size = 500,
                        ),
                        modifier = Modifier
                            .size(80.dp),
                        contentDescription = "Album Poster",
                    )
                    {
                        it.override(300).placeholder(R.drawable.yim_album_placeholder)
                    }
                }
            }
    }
}