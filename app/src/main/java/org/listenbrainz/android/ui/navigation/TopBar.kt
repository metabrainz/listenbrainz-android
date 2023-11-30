package org.listenbrainz.android.ui.navigation

import android.content.Context
import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.Uri
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.listenbrainz.android.R
import org.listenbrainz.android.model.AppNavigationItem
import org.listenbrainz.android.ui.screens.search.SearchBarState
import org.listenbrainz.android.ui.screens.search.rememberSearchBarState
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@Composable
fun TopBar(
    navController: NavController = rememberNavController(),
    searchBarState: SearchBarState,
    context: Context = LocalContext.current
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val title: String = currentDestination?.route?.let {
        when (it) {
            AppNavigationItem.Feed.route -> AppNavigationItem.Feed.title
            AppNavigationItem.BrainzPlayer.route -> AppNavigationItem.BrainzPlayer.title
            AppNavigationItem.Explore.route -> AppNavigationItem.Explore.title
            AppNavigationItem.Profile.route -> AppNavigationItem.Profile.title
            AppNavigationItem.Settings.route -> AppNavigationItem.Settings.title
            AppNavigationItem.About.route -> AppNavigationItem.About.title
            else -> ""
        }
    } ?: "ListenBrainz"
    
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon =  {
            IconButton(onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://listenbrainz.org")))
            }) {
                Icon(painterResource(id = R.drawable.ic_listenbrainz_logo_icon),
                    "ListenBrainz",
                    tint = Color.Unspecified)
            }
        },
        backgroundColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        elevation = 0.dp,
        actions = {
            IconButton(onClick = { searchBarState.activate() }) {
                Icon(painterResource(id = R.drawable.ic_search), contentDescription = "Search users")
            }

            IconButton(onClick = {
                if (navBackStackEntry?.destination?.route == AppNavigationItem.Settings.route){
                    navController.popBackStack()
                } else {
                    navController.navigate(AppNavigationItem.Settings.route) {
                        // Avoid building large backstack
                        popUpTo(AppNavigationItem.Feed.route) {
                            saveState = true
                        }
                        // Avoid copies
                        launchSingleTop = true
                        // Restore previous state
                        restoreState = true
                    }
                }
            }) {
                Icon(painterResource(id = R.drawable.ic_settings),"Settings")
            }
        }
    )
    
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun TopBarPreview() {
    ListenBrainzTheme {
        TopBar(
            navController = rememberNavController(),
            searchBarState = rememberSearchBarState()
        )
    }
}