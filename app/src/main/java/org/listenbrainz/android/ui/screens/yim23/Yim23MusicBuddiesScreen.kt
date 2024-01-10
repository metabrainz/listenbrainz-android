package org.listenbrainz.android.ui.screens.yim23

import android.widget.Toast
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.listenbrainz.android.model.SocialData
import org.listenbrainz.android.model.yimdata.Yim23Screens
import org.listenbrainz.android.model.yimdata.Yim23TopDiscoveries
import org.listenbrainz.android.model.yimdata.Yim23Track
import org.listenbrainz.android.ui.components.SimilarUserCard
import org.listenbrainz.android.ui.components.Yim23Footer
import org.listenbrainz.android.ui.components.Yim23Header
import org.listenbrainz.android.ui.components.Yim23ShareButton
import org.listenbrainz.android.ui.theme.Yim23Theme
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.viewmodel.SocialViewModel
import org.listenbrainz.android.viewmodel.Yim23ViewModel

@Composable
fun Yim23MusicBuddiesScreen (
    viewModel: Yim23ViewModel,
    navController: NavController
) {
    val followers = remember { viewModel.followers }
    val context = LocalContext.current
    if(followers.value?.status == Resource.Status.SUCCESS){
        Yim23BaseScreen(
            viewModel     = viewModel,
            navController = navController,
            footerText    = "MUSIC BUDDIES",
            isUsername    = false,
            downScreen    = when (followers.value?.data?.followers!!.size){
                0 -> Yim23Screens.YimLastScreen
                else -> Yim23Screens.YimFriendsScreen
            }
        ) {
            Yim23MusicBuddies(viewModel = viewModel)
        }
    }
    else{
        if(followers.value?.status == Resource.Status.LOADING)
        Toast.makeText(context , "Loading" , Toast.LENGTH_SHORT).show()
        else{
            Toast.makeText(context , "Error occoured" , Toast.LENGTH_SHORT).show()
        }
    }


}


@Composable
private fun Yim23MusicBuddies (viewModel: Yim23ViewModel) {
    val musicBuddies  = remember {
        viewModel.getSimilarUsers() ?: listOf()
    }
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
            itemsIndexed(musicBuddies.toList()) {index , it ->
                SimilarUserCard(uiModeIsDark = false,index = index, userName = it.first,
                    similarity = it.second.toFloat() , cardBackGround = Color(0xFFe0e5de))
            }
        }
    }
}