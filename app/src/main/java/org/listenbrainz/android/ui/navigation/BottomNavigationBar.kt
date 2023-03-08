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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.navigation.AppNavigationItem
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
        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
        elevation = 0.dp
    ) {
        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(painterResource(id = item.icon),
                    modifier = Modifier.size(28.dp).padding(top = 4.dp), contentDescription = item.title, tint = Color.Unspecified) },
                label = { Text(text = item.title, fontSize = 11.sp) },
                selectedContentColor = MaterialTheme.colorScheme.onSurface,
                unselectedContentColor = colorResource(id = R.color.gray),
                alwaysShowLabel = true,
                selected = true,
                onClick = {
                    // TODO: remove this after profile page has been converted to composable
                    if (item == AppNavigationItem.Profile){
                        activity.startActivity(Intent(activity,LoginActivity::class.java))
                    }else{
                        navController.navigate(item.route){
                            // Avoid building large backstack
                            popUpTo(AppNavigationItem.Home.route){
                                saveState = true
                            }
                            // Avoid copies
                            launchSingleTop = true
                            // Restore previous state
                            restoreState = true
                            // TODO: Implement refresh for listens Screen.
                        }
                    }
                }
            )
        }
    }
}

@ExperimentalPagerApi
@Composable
fun SongViewPager(modifier: Modifier = Modifier, viewModel: BrainzPlayerViewModel = hiltViewModel()) {
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
                        .padding(top = 12.dp, start = 5.dp, end = 5.dp),
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
                            text = when {
                                currentlyPlayingSong.artist == "null" && currentlyPlayingSong.title == "null"-> ""
                                currentlyPlayingSong.artist == "null" -> currentlyPlayingSong.title
                                currentlyPlayingSong.title == "null" -> currentlyPlayingSong.artist
                                else -> currentlyPlayingSong.artist + "  -  " + currentlyPlayingSong.title
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
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