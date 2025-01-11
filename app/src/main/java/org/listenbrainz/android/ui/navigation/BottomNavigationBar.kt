package org.listenbrainz.android.ui.navigation

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BackdropScaffoldState
import androidx.compose.material.BackdropValue
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.model.AppNavigationItem
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    backdropScaffoldState: BackdropScaffoldState = rememberBackdropScaffoldState(initialValue = BackdropValue.Revealed),
    scrollToTop: () -> Unit,
    username : String?,
) {
    val items = listOf(
        AppNavigationItem.Feed,
        AppNavigationItem.Explore,
        AppNavigationItem.BrainzPlayer,
        AppNavigationItem.Profile
    )
    BottomNavigation(
        modifier = modifier,
        backgroundColor = ListenBrainzTheme.colorScheme.nav,
        elevation = 0.dp
    ) {
        val coroutineScope = rememberCoroutineScope()
        items.forEach { item ->
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val selected = currentDestination?.route?.startsWith("${item.route}/") == true || currentDestination?.route == item.route
            BottomNavigationItem(
                modifier = Modifier.navigationBarsPadding(),
                icon = {
                    Icon(
                        painterResource(id = selected
                            .takeIf { it }
                            ?.let { item.iconSelected }
                            ?: item.iconUnselected),
                        modifier = Modifier
                            .size(24.dp)
                            .padding(vertical = 4.dp), contentDescription = item.title, tint = MaterialTheme.colorScheme.onSurface
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(),
                    )
                },
                selectedContentColor = MaterialTheme.colorScheme.onSurface,
                unselectedContentColor = colorResource(id = R.color.gray),
                alwaysShowLabel = true,
                selected = selected,
                onClick = {
                    coroutineScope.launch {
                        if (selected) {
                            scrollToTop()
                        }

                        // A quick way to navigate to back layer content.
                        backdropScaffoldState.reveal()

                        when (item.route) {
                            AppNavigationItem.Profile.route -> {
                                navController.navigate("profile/${username}"){
                                    // Avoid building large backstack
                                    popUpTo(AppNavigationItem.Feed.route){
                                        saveState = true
                                    }
                                    // Avoid copies
                                    launchSingleTop = true
                                    // Restore previous state
                                    restoreState = true
                                }
                            }
                            AppNavigationItem.Feed.route -> {
                                navController.navigate(AppNavigationItem.Feed.route)
                            }
                            else -> navController.navigate(item.route){
                                    // Avoid building large backstack
                                    popUpTo(AppNavigationItem.Feed.route){
                                        saveState = true
                                    }
                                    // Avoid copies
                                    launchSingleTop = false
                                    // Restore previous state
                                    restoreState = true
                                }
                        }

                    }
                    
                }
            )
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun BottomNavigationBarPreview() {
    BottomNavigationBar(navController = rememberNavController() , scrollToTop = {} ,username = "pranavkonidena")
}