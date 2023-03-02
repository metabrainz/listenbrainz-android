package org.listenbrainz.android.ui.components

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.navigation.AppNavigationItem
import org.listenbrainz.android.ui.screens.brainzplayer.navigation.BrainzPlayerNavigationItem
import org.listenbrainz.android.ui.screens.brainzplayer.ui.components.SeekBar
import org.listenbrainz.android.ui.screens.brainzplayer.ui.components.basicMarquee
import org.listenbrainz.android.ui.screens.login.LoginActivity
import org.listenbrainz.android.util.BrainzPlayerExtensions.toSong
import org.listenbrainz.android.viewmodel.BrainzPlayerViewModel

@ExperimentalPagerApi
@Composable
fun BottomNavigationBar(
    navController: NavController = rememberNavController(),
    activity: Activity
) {
    val items = listOf(
        AppNavigationItem.Home,
        AppNavigationItem.BrainzPlayer,
        AppNavigationItem.Listens,
        AppNavigationItem.Profile,
    )
    BottomNavigation(
        backgroundColor = MaterialTheme.colorScheme.background,
    ) {
        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(painterResource(id = item.icon),
                    modifier = Modifier.size(28.dp), contentDescription = item.title, tint = Color.Unspecified) },
                label = { Text(text = item.title, fontSize = 11.sp) },
                selectedContentColor = MaterialTheme.colorScheme.onSurface,
                unselectedContentColor = colorResource(id = R.color.gray),      // TODO : Fix this color with consent.
                alwaysShowLabel = true,
                selected = true,
                onClick = {
                    when(item.route){
                        "home" -> {
                            navController.navigate(AppNavigationItem.Home.route){
                                launchSingleTop = true
                                popUpTo(AppNavigationItem.Home.route)
                            }
                        }
                        "brainzplayer" -> {
                            navController.navigate(AppNavigationItem.BrainzPlayer.route){
                                launchSingleTop = true
                                popUpTo(AppNavigationItem.Home.route)
                            }
                        }
                        "listens" -> {
                            navController.navigate(AppNavigationItem.Listens.route){
                                launchSingleTop = true
                                popUpTo(AppNavigationItem.Home.route)
                            }
                        }
                        "profile" -> {
                            val nextActivity = LoginActivity::class.java
                            if(nextActivity != activity::class.java){
                                activity.startActivity(Intent(activity, LoginActivity::class.java))
                            }
                            /*navController.navigate(AppNavigationItem.Profile.route){
                                launchSingleTop = true
                            }*/
                        }
                    }
                }
            )
        }
    }
}

@ExperimentalPagerApi
@Composable
fun SongViewPager( modifier: Modifier = Modifier, viewModel: BrainzPlayerViewModel = hiltViewModel()) {
    val songList = viewModel.mediaItem.collectAsState().value.data ?: listOf()
    val currentlyPlayingSong = viewModel.currentlyPlayingSong.collectAsState().value.toSong
    val pagerState = viewModel.pagerState.collectAsState().value
    val pageState = rememberPagerState(initialPage = pagerState)
    HorizontalPager(count = songList.size, state = pageState, modifier = modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box {
                val progress by viewModel.progress.collectAsState()
                SeekBar(
                    modifier = Modifier
                        .height(10.dp)
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    progress = progress,
                    onValueChange = viewModel::onSeek,
                    onValueChanged = viewModel::onSeeked
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Row {
                    Box(
                        modifier = Modifier
                            .padding(start = 5.dp, end = 5.dp)
                            .height(45.dp)
                            .width(45.dp)
                    ) {
                        AsyncImage(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(shape = RoundedCornerShape(8.dp))
                                .graphicsLayer { clip = true },
                            model = currentlyPlayingSong.albumArt,
                            contentDescription = "",
                            error = painterResource(
                                id = R.drawable.ic_erroralbumart
                            ),
                            contentScale = ContentScale.Crop
                        )
                    }
                    val playIcon by viewModel.playButton.collectAsState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 35.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipPrevious,
                                contentDescription = "",
                                Modifier
                                    .size(35.dp)
                                    .clickable { viewModel.skipToPreviousSong() },
                                tint = MaterialTheme.colorScheme.onTertiary
                            )
                            Box(
                                modifier = Modifier
                                    .size(35.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onTertiary)
                            ) {
                                PlayPauseIcon(
                                    playIcon,
                                    viewModel,
                                    Modifier.size(35.dp),
                                    tint = MaterialTheme.colorScheme.tertiaryContainer
                                )
                            }
                            Icon(
                                imageVector = Icons.Rounded.SkipNext,
                                contentDescription = "",
                                Modifier
                                    .size(35.dp)
                                    .clickable { viewModel.skipToNextSong() },
                                tint = MaterialTheme.colorScheme.onTertiary
                            )
                        }
                        Text(
                            text = currentlyPlayingSong.artist + " - " + currentlyPlayingSong.title,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.basicMarquee()
                        )
                    }
                }
            }
        }
      //  TODO("Fix View Pager changing pages")
    }
}

@Composable
fun BrainzPlayerBottomBar( navController: NavController) {
    val items = listOf(
        BrainzPlayerNavigationItem.Home,
        BrainzPlayerNavigationItem.Songs,
        BrainzPlayerNavigationItem.Artists,
        BrainzPlayerNavigationItem.Albums,
        BrainzPlayerNavigationItem.Playlists,

        )
    val backStackEntry by navController.currentBackStackEntryAsState()
    BottomNavigation(
        backgroundColor = MaterialTheme.colorScheme.background,
    ) {
        items.forEach { item ->
            val selected = item.route == backStackEntry?.destination?.route
            BottomNavigationItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(text = item.title, fontSize = 11.sp) },
                selectedContentColor = MaterialTheme.colorScheme.onSurface,
                unselectedContentColor = MaterialTheme.colorScheme.onBackground,
                alwaysShowLabel = true,
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}