package org.listenbrainz.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.listenbrainz.android.ui.navigation.TopBarActions
import org.listenbrainz.android.ui.screens.explore.ExploreScreen
import org.listenbrainz.android.ui.screens.feed.FeedScreen
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.shared.model.AppNavigationItem
import org.listenbrainz.shared.viewmodel.DashBoardViewModel

private val homeTabs = listOf(AppNavigationItem.Feed, AppNavigationItem.Explore)

/** Stands in for HomeScreen, with only the tabs whose screens are common so far. */
@Composable
fun IosHomeScreen(
    dashBoardViewModel: DashBoardViewModel,
    onNewsClick: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var scrollToTopState by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    // Search, Settings, profiles and artists aren't on iOS yet
    val topBarActions = remember { TopBarActions() }

    Scaffold(
        containerColor = ListenBrainzTheme.colorScheme.background,
        // The screens pad for the status bar themselves
        contentWindowInsets = WindowInsets.captionBar,
        bottomBar = {
            BottomNavigation(
                backgroundColor = ListenBrainzTheme.colorScheme.nav,
                elevation = 0.dp
            ) {
                homeTabs.forEachIndexed { index, item ->
                    val selected = index == selectedTab
                    BottomNavigationItem(
                        icon = {
                            Icon(
                                painter = painterResource(if (selected) item.iconSelected else item.iconUnselected),
                                modifier = Modifier.size(24.dp).padding(vertical = 4.dp),
                                contentDescription = item.title,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        label = { Text(text = item.title, color = MaterialTheme.colorScheme.onSurface) },
                        selectedContentColor = MaterialTheme.colorScheme.onSurface,
                        unselectedContentColor = Color(0xFF263238),
                        alwaysShowLabel = true,
                        selected = selected,
                        onClick = {
                            if (selected) scrollToTopState = true
                            selectedTab = index
                        },
                        modifier = Modifier.navigationBarsPadding()
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (homeTabs[selectedTab]) {
                AppNavigationItem.Feed -> FeedScreen(
                    scrollToTopState = scrollToTopState,
                    topAppBarActions = topBarActions,
                    onScrollToTop = { scrollToTop ->
                        scope.launch {
                            if (scrollToTopState) {
                                scrollToTop()
                                scrollToTopState = false
                            }
                        }
                    },
                    goToUserPage = {},
                    goToArtistPage = {}
                )

                else -> {
                    val dashBoardUiState by dashBoardViewModel.uiState.collectAsState()
                    ExploreScreen(
                        topBarActions = topBarActions,
                        username = dashBoardUiState.username,
                        onNewsClick = onNewsClick
                    )
                }
            }
        }
    }
}
