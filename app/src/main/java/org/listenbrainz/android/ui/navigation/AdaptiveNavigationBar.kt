package org.listenbrainz.android.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.BackdropScaffoldState
import androidx.compose.material.BackdropValue
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.NavigationRail
import androidx.compose.material.Text
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.model.AppNavigationItem
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.BrainzPlayerExtensions.toSong
import org.listenbrainz.android.util.SongViewPager
import org.listenbrainz.android.viewmodel.BrainzPlayerViewModel

@Composable
fun AdaptiveNavigationBar(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    backgroundColor: Color = ListenBrainzTheme.colorScheme.nav,
    backdropScaffoldState: BackdropScaffoldState = rememberBackdropScaffoldState(initialValue = BackdropValue.Revealed),
    scrollToTop: () -> Unit,
    username: String?,
    isLandscape: Boolean,
    brainzPlayerViewModel: BrainzPlayerViewModel = hiltViewModel()
) {
    val items = listOf(
        AppNavigationItem.Feed,
        AppNavigationItem.Explore,
        AppNavigationItem.BrainzPlayer,
        AppNavigationItem.Profile
    )
    val coroutineScope = rememberCoroutineScope()

    @Composable
    fun NavigationContent(
        item: AppNavigationItem,
        selected: Boolean,
        onItemClick: () -> Unit,
        isLandscape: Boolean,
        scope: RowScope?
    ) {
        val navIcon = @Composable {
            Icon(
                painter = painterResource(
                    id = if (selected) item.iconSelected else item.iconUnselected
                ),
                modifier = Modifier
                    .size(24.dp)
                    .padding(vertical = 4.dp),
                contentDescription = item.title,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        val navLabel = @Composable {
            Text(
                text = item.title,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding()
            )
        }

        if (isLandscape) {
            NavigationRailItem(
                icon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.safeContent)
                            .fillMaxWidth()
                    ) {
                        navIcon()
                        navLabel()
                    }
                },
                alwaysShowLabel = false,
                selected = selected,
                colors = NavigationRailItemDefaults.colors(
                    indicatorColor = backgroundColor
                ),
                onClick = onItemClick
            )
        } else {
            scope?.let {
                it.BottomNavigationItem(
                    icon = { navIcon() },
                    label = { navLabel() },
                    selectedContentColor = MaterialTheme.colorScheme.onSurface,
                    unselectedContentColor = colorResource(id = R.color.gray),
                    alwaysShowLabel = true,
                    selected = selected,
                    onClick = onItemClick,
                    modifier = Modifier.navigationBarsPadding()
                )
            }

        }
    }

    //composable with common navigation logic
    @Composable
    fun CommonNavigationLogic(scope: RowScope? = null) {
        items.forEach { item ->
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val selected = currentDestination?.route?.startsWith("${item.route}/") == true ||
                    currentDestination?.route == item.route

            NavigationContent(
                item = item,
                selected = selected,
                scope = scope,
                isLandscape = isLandscape,
                onItemClick = {
                    coroutineScope.launch {
                        if (selected) {
                            scrollToTop()
                        }
                        // A quick way to navigate to back layer content.
                        backdropScaffoldState.reveal()

                        when (item.route) {
                            AppNavigationItem.Profile.route -> {
                                val profileRoute = AppNavigationItem.Profile.route +
                                        if (!username.isNullOrBlank()) "/${username}" else ""
                                navController.navigate(profileRoute) {
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
    }

    if (isLandscape) {
        val currentlyPlayingSong by brainzPlayerViewModel.currentlyPlayingSong.collectAsStateWithLifecycle()
        NavigationRail(
            modifier = modifier
                .widthIn(max = 200.dp)
                .background(backgroundColor)
                .statusBarsPadding(),
            backgroundColor = backgroundColor,
            contentColor = MaterialTheme.colorScheme.onSurface,
            elevation = 0.dp
        ) {
            CommonNavigationLogic()
            Spacer(modifier = Modifier.weight(1f))
            if (currentlyPlayingSong.toSong.mediaID != 0L)
                SongViewPager(
                    modifier = modifier,
                    songList = brainzPlayerViewModel.appPreferences.currentPlayable?.songs
                        ?: emptyList(),
                    backdropScaffoldState = backdropScaffoldState,
                    currentlyPlayingSong = currentlyPlayingSong.toSong,
                    isLandscape = true
                )
        }
    } else {
        BottomNavigation(
            modifier = modifier,
            backgroundColor = backgroundColor,
            elevation = 0.dp
        ) {
            CommonNavigationLogic(scope = this)
        }
    }
}

@Preview
@Composable
fun AdaptiveNavigationBarPreview() {
    AdaptiveNavigationBar(
        navController = rememberNavController(),
        scrollToTop = {},
        username = "pranavkonidena",
        isLandscape = false
    )
}