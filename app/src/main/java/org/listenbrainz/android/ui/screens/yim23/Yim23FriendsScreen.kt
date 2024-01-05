package org.listenbrainz.android.ui.screens.yim23

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.model.SocialData
import org.listenbrainz.android.model.yimdata.Yim23Screens
import org.listenbrainz.android.ui.components.Yim23Footer
import org.listenbrainz.android.ui.components.Yim23Header
import org.listenbrainz.android.ui.theme.Yim23Theme
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.viewmodel.SocialViewModel
import org.listenbrainz.android.viewmodel.Yim23ViewModel


@Composable
@ExperimentalFoundationApi
fun Yim23FriendsScreen (
    viewModel: Yim23ViewModel,
    socialViewModel: SocialViewModel,
    navController: NavController
) {
    val username by viewModel.getUsernameFlow().collectAsState(initial = "")
    Yim23Theme(themeType = viewModel.themeType.value) {
        Column (modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onBackground),
            verticalArrangement = Arrangement.SpaceBetween) {
            Yim23Header(username = username, navController = navController)
            Column (modifier = Modifier , horizontalAlignment = Alignment.CenterHorizontally) {
                Text("VISIT SOME FRIENDS" , style = MaterialTheme.typography.titleLarge ,
                    color = MaterialTheme.colorScheme.background , textAlign = TextAlign.Center)
            }
            Yim23Friends(username = username, socialViewModel = socialViewModel)
            Yim23Footer(footerText = username, isUsername = true, navController = navController,
                downScreen = Yim23Screens.YimLastScreen)
        }
    }
}





@ExperimentalFoundationApi
@Composable
private fun Yim23Friends (username : String, socialViewModel: SocialViewModel) {
    var followers : Resource<SocialData> = remember {socialViewModel.getFollowers(username)}
    val animationScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    if(followers != null){
        val pagerState = rememberPagerState {
            followers.data!!.followers!!.size
        }
        Box {
            HorizontalPager(state = pagerState) {
                page ->
                Surface (modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 30.dp, end = 30.dp) ,
                    color = MaterialTheme.colorScheme.background
                , onClick = {
                        uriHandler.openUri("https://beta.listenbrainz.org/user/${followers.data!!.followers!![page]}/year-in-music/2023/")
                    }
                ) {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(
                            RoundedCornerShape(10.dp)
                        )
                        .background(MaterialTheme.colorScheme.background) ,
                        horizontalAlignment = Alignment.CenterHorizontally ,
                        verticalArrangement = Arrangement.Center) {
                        Text(followers.data!!.followers!![page] ,
                            color = MaterialTheme.colorScheme.onBackground ,
                            style = MaterialTheme.typography.bodyLarge)
                        Divider(modifier = Modifier.width(60.dp) ,
                            color = MaterialTheme.colorScheme.onBackground)
                        Row (modifier = Modifier.fillMaxWidth() ,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            IconButton(onClick = {
                                animationScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            } , modifier = Modifier
                                .size(26.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(MaterialTheme.colorScheme.onBackground)) {
                                Image(imageVector = ImageVector.vectorResource(R.drawable.yim23_arrow_left),
                                    contentDescription = "Up arrow" , colorFilter = ColorFilter.tint(
                                    MaterialTheme.colorScheme.background) ,
                                    modifier = Modifier.zIndex(1f))
                            }
                            IconButton(onClick = {
                                animationScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } ,  modifier = Modifier
                                .size(26.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(MaterialTheme.colorScheme.onBackground)) {
                                Image(imageVector = ImageVector.vectorResource(R.drawable.yim23_right_arrow),
                                    contentDescription = "Up arrow" , colorFilter = ColorFilter.tint(
                                    MaterialTheme.colorScheme.background) ,  modifier = Modifier.zIndex(1f))
                            }
                        }

                    }
                }

            }
        }
        }
    else{
        CircularProgressIndicator()
    }


}


