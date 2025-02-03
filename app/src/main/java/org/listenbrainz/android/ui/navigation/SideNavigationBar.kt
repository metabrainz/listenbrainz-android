package org.listenbrainz.android.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BackdropScaffoldState
import androidx.compose.material.BackdropValue
import androidx.compose.material.Icon
import androidx.compose.material.NavigationRail
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.model.AppNavigationItem
import org.listenbrainz.android.model.Song
import org.listenbrainz.android.ui.components.CustomSeekBar
import org.listenbrainz.android.ui.components.PlayPauseIcon
import org.listenbrainz.android.ui.screens.brainzplayer.ui.components.basicMarquee
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.BrainzPlayerExtensions.toSong
import org.listenbrainz.android.viewmodel.BrainzPlayerViewModel

@Composable
fun SideNavigationBar(
    modifier: Modifier = Modifier, navController: NavController = rememberNavController(),
    backgroundColor: Color = ListenBrainzTheme.colorScheme.nav,
    backdropScaffoldState: BackdropScaffoldState = rememberBackdropScaffoldState(initialValue = BackdropValue.Revealed),
    scrollToTop: () -> Unit,
    username: String?,
    viewModel: BrainzPlayerViewModel = hiltViewModel()
) {
    val items = listOf(
        AppNavigationItem.Feed,
        AppNavigationItem.Explore,
        AppNavigationItem.BrainzPlayer,
        AppNavigationItem.Profile
    )

    val currentlyPlayingSong =
        viewModel.currentlyPlayingSong.collectAsStateWithLifecycle().value.toSong
    NavigationRail(
        modifier = modifier
            .width(200.dp)
            .background(backgroundColor)
            .statusBarsPadding(),
        backgroundColor = backgroundColor,
        contentColor = MaterialTheme.colorScheme.onSurface,
        elevation = 0.dp
    ) {
        val coroutineScope = rememberCoroutineScope()
        items.forEach { item ->
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val selected =
                currentDestination?.route?.startsWith("${item.route}/") == true || currentDestination?.route == item.route
            NavigationRailItem(
                modifier = Modifier,
                icon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.safeContent)
                            .fillMaxWidth()
                    ) {
                        Icon(
                            painterResource(id = selected
                                .takeIf { it }
                                ?.let { item.iconSelected }
                                ?: item.iconUnselected),
                            modifier = Modifier
                                .size(24.dp)
                                .padding(vertical = 4.dp),
                            contentDescription = item.title,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = item.title,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(),
                        )
                    }

                },
                alwaysShowLabel = false,
                selected = selected,
                colors = NavigationRailItemDefaults.colors(
                    indicatorColor = backgroundColor,
                ),
                onClick = {
                    coroutineScope.launch {
                        if (selected) {
                            scrollToTop()
                        }
                        backdropScaffoldState.reveal()

                        when (item.route) {
                            AppNavigationItem.Profile.route -> {
                                navController.navigate(AppNavigationItem.Profile.route + if (!username.isNullOrBlank()) "/${username}" else "") {
                                    // Avoid building large backstack
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        if (username.isNullOrBlank()) {
                                            inclusive = true
                                        }

                                        saveState = true
                                    }
                                    // Avoid copies
                                    launchSingleTop = true
                                    // Restore previous state
                                    restoreState = true
                                }
                            }

                            else -> navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid copies
                                launchSingleTop = true
                                // Restore previous state
                                restoreState = true
                            }
                        }
                    }
                }
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        BrainzNavigationMiniPlayer(
            backdropScaffoldState = backdropScaffoldState,
            currentlyPlayingSong = currentlyPlayingSong,
        )
    }
}

@Composable
fun BrainzNavigationMiniPlayer(
    modifier: Modifier = Modifier,
    backdropScaffoldState: BackdropScaffoldState,
    currentlyPlayingSong: Song,
    viewModel: BrainzPlayerViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                coroutineScope.launch {
                    // Click anywhere to open the front layer.
                    backdropScaffoldState.conceal()
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box {
            val progress by viewModel.progress.collectAsState()
            CustomSeekBar(
                modifier = Modifier
                    .height(10.dp)
                    .fillMaxWidth(),
                progress = progress,
                onValueChange = { newProgress ->
                    viewModel.onSeek(newProgress)
                    viewModel.onSeeked()
                },
                remainingProgressColor = ListenBrainzTheme.colorScheme.hint
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            Column {
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

                    Text(
                        text = when {
                            currentlyPlayingSong.artist == "null" && currentlyPlayingSong.title == "null" -> ""
                            currentlyPlayingSong.artist == "null" -> currentlyPlayingSong.title
                            currentlyPlayingSong.title == "null" -> currentlyPlayingSong.artist
                            else -> currentlyPlayingSong.artist + "  -  " + currentlyPlayingSong.title
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(start = 4.dp, end = 8.dp)
                            .basicMarquee()
                    )
                }
                val playIcon by viewModel.playButton.collectAsState()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "",
                        Modifier
                            .size(35.dp)
                            .clickable {
                                viewModel.skipToPreviousSong()
                            },
                        tint = MaterialTheme.colorScheme.onSurface
                    )

                    PlayPauseIcon(
                        playIcon,
                        viewModel,
                        Modifier.size(35.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )

                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "",
                        Modifier
                            .size(35.dp)
                            .clickable { viewModel.skipToNextSong() },
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }


        }
    }
}

@Preview
@Composable
private fun SideNavigationBarPreview() {
    SideNavigationBar(scrollToTop = {}, username = "")
}